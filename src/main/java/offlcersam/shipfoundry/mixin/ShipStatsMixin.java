package offlcersam.shipfoundry.mixin;

import game.shiputils.ShipStats;
import offlcersam.shipfoundry.ShipRegistrar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShipStats.class, remap = false)
public abstract class ShipStatsMixin extends ShipStats {

    @Inject(method = "determineHullObjectType", at = @At("HEAD"), cancellable = true)
    private void shipfoundry$overrideHullObjectType(CallbackInfoReturnable<Integer> cir) {
        Integer override = ShipRegistrar.getHullTypeOverride(this.shipSpawnIndex);

        if (override != null) {
            cir.setReturnValue(override);
        }
    }
}