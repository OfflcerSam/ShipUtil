package offlcersam.shipfoundry.mixin;

import game.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Same injection points ShipTest's own MainSetupMixin uses for writeShip(...) calls - weapon
// layouts must exist before ships reference them, and ships should be registered before the
// item database is considered fully loaded. Multiple mods injecting their own @Inject at the
// same vanilla target method is normal and doesn't conflict with ShipTest's own hooks.
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
