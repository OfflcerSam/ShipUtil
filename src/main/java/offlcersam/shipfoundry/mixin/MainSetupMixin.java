package offlcersam.shipfoundry.mixin;

import game.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Main.class, remap = false)
public class MainSetupMixin {

    @Inject(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/weapons/WeaponSlotLayoutList;init()V",
                    shift = At.Shift.AFTER
            )
    )
    private void shipfoundry$loadShips(CallbackInfo ci) {
        offlcersam.shipfoundry.ShipUtilLoader.load();
    }
}
