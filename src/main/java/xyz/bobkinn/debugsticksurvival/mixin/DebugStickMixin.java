package xyz.bobkinn.debugsticksurvival.mixin;

import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemDebugStick;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bobkinn.debugsticksurvival.Config;

import java.util.Collection;

@Mixin(value = ItemDebugStick.class, remap = false)
public class DebugStickMixin {
    @Shadow
    private static void a(EntityHuman player, IChatBaseComponent message) {}

    @Contract(pure = true)
    @Shadow
    private static <T extends Comparable<T>> @Nullable String a(IBlockData state, IBlockState<T> property) {return null;}

    @Contract(pure = true)
    @Shadow
    private static <T extends Comparable<T>> IBlockData a(IBlockData state, IBlockState<T> property, boolean inverse) {
        return null;
    }

    @Inject(at = @At("HEAD"), method = "a(Lnet/minecraft/world/entity/player/EntityHuman;Lnet/minecraft/world/level/block/state/IBlockData;Lnet/minecraft/world/level/GeneratorAccess;Lnet/minecraft/core/BlockPosition;ZLnet/minecraft/world/item/ItemStack;)Z", cancellable = true)
    private void onUSE(@NotNull EntityHuman player, IBlockData state, GeneratorAccess world, BlockPosition pos, boolean update, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // if the player already does have the rights to use Debug Stick, the mod should not interfere
        if (player.gg()) {return;}

        Block block = state.b();
        BlockStateList<Block, IBlockData> stateManager = block.n();
        Collection<IBlockState<?>> collection = stateManager.d();

        // check if block is modifiable by the config
        if (!debugStickSurvival$isBlockAllowedToModify(block) || collection.isEmpty()) {
            a(player, IChatBaseComponent.a(Config.MESSAGE_nomodify));
            cir.setReturnValue(false);
            return;
        }

        // https://minecraft.fandom.com/wiki/Debug_Stick#Item_data
        // to remember the data of which property for which block is chosen,
        // Minecraft Devs decided to use NBT data for Debug Stick.
        // Who am I to disagree?
        NBTTagCompound nbtCompound = stack.a("DebugProperty");

        String blockName = BuiltInRegistries.f.b(block).toString();
        String propertyName = nbtCompound.l(blockName);

        IBlockState<?> property = stateManager.a(propertyName);

        if (!update) {
            // select next property
            property = debugStickSurvival$getNextProperty(collection, property, block);
            // save chosen property in the NBT data of Debug Stick
            nbtCompound.a(blockName, property.f());

            if (!debugStickSurvival$isPropertyModifiable(property, block)){
                a(player, IChatBaseComponent.a(Config.MESSAGE_nomodify));
                cir.setReturnValue(false);
                return;
            }

            // send the player a message of successful selecting
            a(player, IChatBaseComponent.a(
                            String.format(
                                    Config.MESSAGE_select,
                                    property.f(),
                                    a(state, property)
                            )
                    )
            );
        } else {
            // change value of property
            if (property == null) {
                property = debugStickSurvival$getNextProperty(collection, null, block);
            }
            if (debugStickSurvival$isPropertyModifiable(property, block)) {
                // generate new state of chosen block with modified property
                IBlockData newState = a(state, property, false);
                // update chosen block with its new state

                world.a(pos, newState, 18);
                // send the player a message of successful modifying
                a(player, IChatBaseComponent.a(
                                String.format(
                                        Config.MESSAGE_change,
                                        property.f(),
                                        a(newState, property)
                                )
                        )
                );
            } else {
                a(player, IChatBaseComponent.a(Config.MESSAGE_nomodify));
                cir.setReturnValue(false);
                return;
            }
        }
        cir.setReturnValue(true);
    }

    /**
     * Choose next property that is appropriate for the configuration file
     * */
    @Unique
    private IBlockState<?> debugStickSurvival$getNextProperty(@NotNull Collection<IBlockState<?>> collection, @Nullable IBlockState<?> property, @Nullable Block block) {
        int len = collection.size();
        do { // simply scrolling through the list of properties until suitable is found
            property = SystemUtils.a(collection, property);
            len--;
        } while (len > 0 && !debugStickSurvival$isPropertyModifiable(property, block));
        return property;
    }

    /**
     * Check via config if chosen block is able to be modified in survival
     * */
    @Unique
    private boolean debugStickSurvival$isBlockAllowedToModify(Block block) {
        return Config.isBlockAllowed(block);
    }

    /**
     * Check via config if chosen property is able to be modified in survival
     * */
    @Unique
    private boolean debugStickSurvival$isPropertyModifiable(@NotNull IBlockState<?> property, @Nullable Block block) {
        return Config.isPropertyAllowed(property.f(), block);
    }
}
