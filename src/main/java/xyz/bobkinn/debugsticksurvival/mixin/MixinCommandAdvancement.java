package xyz.bobkinn.debugsticksurvival.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.commands.CommandAdvancement;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn.debugsticksurvival.Config;

@Mixin(value = CommandAdvancement.class, remap = false)
public class MixinCommandAdvancement {

    @Inject(method = "a(Lcom/mojang/brigadier/CommandDispatcher;)V", at = @At("HEAD"))
    private static void onReg(@NotNull CommandDispatcher<CommandListenerWrapper> dispatcher, CallbackInfo ci){
        dispatcher.register(net.minecraft.commands.CommandDispatcher.a("sdsreload")
                .requires((source) -> source.c(4))
                .executes(MixinCommandAdvancement::debugStickSurvival$sdsReload));
    }

    @Unique
    private static int debugStickSurvival$sdsReload(CommandContext<CommandListenerWrapper> ctx){
        if (Config.configFile != null){
            Config.reload(Config.configFile);
            ctx.getSource().a(IChatBaseComponent.a("Reloaded"), false);
            return 1;
        }
        return 0;
    }

}
