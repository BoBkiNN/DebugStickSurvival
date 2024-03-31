package xyz.bobkinn.debugsticksurvival.mixin;

import joptsimple.OptionSet;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn.debugsticksurvival.SDSMod;

@Mixin(Main.class)
public class MixinMain {
    @Inject(method = "main", at = @At("HEAD"))
    private static void onMain(OptionSet optionset, CallbackInfo ci){
        new SDSMod();
    }
}
