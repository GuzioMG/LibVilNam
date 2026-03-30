package hub.guzio.lvn.integrations.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import hub.guzio.lvn.integrations.BlaystoneNameGenerator;
import net.blay09.mods.waystones.worldgen.namegen.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NameGeneratorManager.class)
class Blaystone {
    @Inject(at = @At("RETURN"), method="getNameGenerator", cancellable = true)
    private void getNameGenerator(NameGenerationMode nameGenerationMode, @NotNull CallbackInfoReturnable<NameGenerator> cir) {
        cir.setReturnValue(new BlaystoneNameGenerator(cir.getReturnValue()));
    }
}