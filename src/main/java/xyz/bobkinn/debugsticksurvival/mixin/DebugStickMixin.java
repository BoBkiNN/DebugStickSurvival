package xyz.bobkinn.debugsticksurvival.mixin;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DebugStickItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
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

@Mixin(value = DebugStickItem.class)
public class DebugStickMixin {
    @Shadow
    private static void message(Player player, Component message) {}

    @Contract(pure = true)
    @Shadow
    private static <T extends Comparable<T>> @Nullable String getNameHelper(BlockState state, Property<T> property) {return null;}

    @Contract(pure = true)
    @Shadow
    private static <T extends Comparable<T>> BlockState cycleState(BlockState state, Property<T> property, boolean inverse) {
        return null;
    }

    @Inject(at = @At("HEAD"), method = "handleInteraction", cancellable = true)
    private void onUSE(Player player, BlockState state, LevelAccessor world, BlockPos pos, boolean update, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // if the player already does have the rights to use Debug Stick, the mod should not interfere
        if (player.canUseGameMasterBlocks()) {return;}

        Block block = state.getBlock();
        StateDefinition<Block, BlockState> stateManager = block.getStateDefinition();
        Collection<Property<?>> collection = stateManager.getProperties();

        // check if block is modifiable by the config
        if (!debugStickSurvival$isBlockAllowedToModify(block) || collection.isEmpty()) {
            message(player, Component.literal(Config.MESSAGE_nomodify));
            cir.setReturnValue(false);
            return;
        }

        var debugStickState = stack.getComponents().getOrDefault(DataComponents.DEBUG_STICK_STATE, DebugStickState.EMPTY);
        var property = debugStickState.properties().get(Holder.direct(block));

        if (!update) {
            // select next property
            property = debugStickSurvival$getNextProperty(collection, property, block);
            // save chosen property in the NBT data of Debug Stick
            var newDataComp = debugStickState.withProperty(Holder.direct(block), property);
            stack.set(DataComponents.DEBUG_STICK_STATE, newDataComp);

            if (!debugStickSurvival$isPropertyModifiable(property, block)){
                message(player, Component.literal(Config.MESSAGE_nomodify));
                cir.setReturnValue(false);
                return;
            }

            // send the player a message of successful selecting
            message(player, Component.literal(
                            String.format(
                                    Config.MESSAGE_select,
                                    property.getName(),
                                    getNameHelper(state, property)
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
                BlockState newState = cycleState(state, property, false);
                // update chosen block with its new state

                world.setBlock(pos, newState, 18);
                // send the player a message of successful modifying
                message(player, Component.literal(
                                String.format(
                                        Config.MESSAGE_change,
                                        property.getName(),
                                        getNameHelper(newState, property)
                                )
                        )
                );
            } else {
                message(player, Component.literal(Config.MESSAGE_nomodify));
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
    private Property<?> debugStickSurvival$getNextProperty(@NotNull Collection<Property<?>> collection, @Nullable Property<?> property, @Nullable Block block) {
        int len = collection.size();
        do { // simply scrolling through the list of properties until suitable is found
            property = Util.findNextInIterable(collection, property);
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
    private boolean debugStickSurvival$isPropertyModifiable(@NotNull Property<?> property, @Nullable Block block) {
        return Config.isPropertyAllowed(property.getName(), block);
    }
}
