package offlcersam.shipfoundry.mixin;

import game.objects.SpaceShip;
import game.shiputils.UniqueDeviceUpdater;
import mods.ModLogger;
import offlcersam.shipfoundry.ShipRegistrar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = UniqueDeviceUpdater.class, remap = false)
public class UniqueDeviceUpdaterMixin {

    @Inject(method = "updateOnInstall", at = @At("HEAD"))
    private static void shipfoundry$applyBuiltInDevices(SpaceShip ship, CallbackInfo ci) {
        int shipIndex = ship.hull.shipStats.shipSpawnIndex;

        for (int deviceId : ShipRegistrar.getBuiltInDevices(shipIndex)) {
            switch (deviceId) {
                case 920: {
                    UniqueDeviceUpdater.hasDroneBoost1 = true;
                    break;
                }
                case 921: {
                    UniqueDeviceUpdater.hasDroneBoost2 = true;
                    break;
                }
                case 922: {
                    UniqueDeviceUpdater.hasDroneBoost3 = true;
                    break;
                }
                case 923: {
                    UniqueDeviceUpdater.hasDroneBoost4 = true;
                    break;
                }
                case 960: {
                    UniqueDeviceUpdater.hasBooster = true;
                    break;
                }
                case 961: {
                    UniqueDeviceUpdater.hasBetterBooster = true;
                    break;
                }
                case 962: {
                    UniqueDeviceUpdater.hasBestBooster = true;
                    break;
                }
                case 963: {
                    UniqueDeviceUpdater.hasBestestBooster = true;
                    break;
                }
                case 970: {
                    UniqueDeviceUpdater.hasOffLevel1 = true;
                    break;
                }
                case 971: {
                    UniqueDeviceUpdater.hasOffLevel2 = true;
                    break;
                }
                case 972: {
                    UniqueDeviceUpdater.hasOffLevel3 = true;
                    break;
                }
                case 973: {
                    UniqueDeviceUpdater.hasOffLevel4 = true;
                    break;
                }
                case 980: {
                    UniqueDeviceUpdater.hasDefLevel1 = true;
                    break;
                }
                case 981: {
                    UniqueDeviceUpdater.hasDefLevel2 = true;
                    break;
                }
                case 982: {
                    UniqueDeviceUpdater.hasDefLevel3 = true;
                    break;
                }
                case 983: {
                    UniqueDeviceUpdater.hasDefLevel4 = true;
                    break;
                }
                default: {
                    ModLogger.log(
                            "[ShipFoundry] Unknown built-in device id " + deviceId + " on ship " + shipIndex
                                    + " - only the 16 UniqueDeviceUpdater device ids are supported "
                                    + "(920-923 drone boost, 960-963 booster, 970-973 offensive coprocessor, 980-983 defensive coprocessor)."
                    );
                }
            }
        }
    }
}