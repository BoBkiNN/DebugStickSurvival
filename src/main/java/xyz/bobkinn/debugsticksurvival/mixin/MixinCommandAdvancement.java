package xyz.bobkinn.debugsticksurvival.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.AdvancementCommands;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn.debugsticksurvival.Config;

@Mixin(value = AdvancementCommands.class)
public class MixinCommandAdvancement {

    @Inject(method = "register", at = @At("HEAD"))
    private static void onReg(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci){
        dispatcher.register(Commands.literal("sdsreload")
                .requires((source) -> source.hasPermission(4))
                .executes(MixinCommandAdvancement::debugStickSurvival$sdsReload));
    }

    @Unique
    private static int debugStickSurvival$sdsReload(CommandContext<CommandSourceStack> ctx){
        if (Config.configFile != null){
            Config.reload(Config.configFile);
            ctx.getSource().sendSuccess(() -> Component.literal("Reloaded"), false);
            return 1;
        }
        return 0;
    }

}
