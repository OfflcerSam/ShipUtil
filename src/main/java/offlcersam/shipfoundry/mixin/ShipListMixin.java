package offlcersam.shipfoundry.mixin;

import items.Stat;
import items.lists.ShipList;
import offlcersam.shipfoundry.ShipRegistrar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShipList.class, remap = false)
public class ShipListMixin {

    // Mimics the giant case switch in ShipList that gives ships their inherent bonuses.
    // But now applies it using data driven methods.
    // Uses BaseID of ship.
    @Inject(method = "compile", at = @At("RETURN"))
    private static void shipfoundry$applyShipStats(boolean isPlatform, int shipIndex, CallbackInfo ci) {
        for (ShipRegistrar.StatBonus bonus : ShipRegistrar.getShipStats(shipIndex)) {
            Stat stat = bonus.stat();

            if (bonus.flat() != null) {
                stat.flat(bonus.flat());
            }
            if (bonus.percent() != null) {
                stat.percent(bonus.percent());
            }
        }
    }
}