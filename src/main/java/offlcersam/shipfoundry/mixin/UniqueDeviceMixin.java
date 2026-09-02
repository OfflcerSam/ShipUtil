package offlcersam.shipfoundry.mixin;

import com.sector.bridge.SSFMLLogger;
import game.objects.SpaceShip;
import game.shiputils.DeviceUpdater;
import items.actions.UniqueDevice;
import offlcersam.shipfoundry.ShipRegistrar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = UniqueDevice.class, remap = false)
public class UniqueDeviceMixin {

    @Inject(method = "compile", at = @At("HEAD"))
    private static void shipfoundry$applyBuiltInDevices(SpaceShip ship, DeviceUpdater devUpdater, CallbackInfo ci) {
        if (ship == null || ship.hull == null) {
            return;
        }

        int shipIndex = ship.hull.shipStats.shipSpawnIndex;

        for (int deviceId : ShipRegistrar.getBuiltInDevices(shipIndex)) {
            switch (deviceId) {
                // Platform Overclock
                case 920: UniqueDevice.addUniquePlatformOverclock(1, null); break;
                case 921: UniqueDevice.addUniquePlatformOverclock(2, null); break;
                case 922: UniqueDevice.addUniquePlatformOverclock(3, null); break;
                case 923: UniqueDevice.addUniquePlatformOverclock(3, null); break;
                case 924: UniqueDevice.addUniquePlatformOverclock(4, null); break;
                case 925: UniqueDevice.addUniquePlatformOverclock(4, null); break;
                // Engine Overclock
                case 960: UniqueDevice.addUniqueEngineOverclock(1, null); break;
                case 961: UniqueDevice.addUniqueEngineOverclock(2, null); break;
                case 962: UniqueDevice.addUniqueEngineOverclock(3, null); break;
                case 963: UniqueDevice.addUniqueEngineOverclock(3, null); break;
                case 964: UniqueDevice.addUniqueEngineOverclock(4, null); break;
                case 965: UniqueDevice.addUniqueEngineOverclock(4, null); break;
                // Offensive Overclock
                case 970: case 720: UniqueDevice.addUniqueOffOverclock(1, null); break;
                case 971: case 721: UniqueDevice.addUniqueOffOverclock(2, null); break;
                case 972: case 722: UniqueDevice.addUniqueOffOverclock(3, null); break;
                case 973: case 723: UniqueDevice.addUniqueOffOverclock(3, null); break;
                case 974: case 975: case 976: UniqueDevice.addUniqueOffOverclock(4, null); break;
                // Defensive Overclock
                case 980: case 730: UniqueDevice.addUniqueDefOverclock(1, null); break;
                case 981: case 731: UniqueDevice.addUniqueDefOverclock(2, null); break;
                case 982: case 732: UniqueDevice.addUniqueDefOverclock(3, null); break;
                case 983: case 733: UniqueDevice.addUniqueDefOverclock(3, null); break;
                case 984: case 985: UniqueDevice.addUniqueDefOverclock(4, null); break;
                // Fighter Overclock
                case 990: UniqueDevice.addUniqueFighterOverclock(1, null); break;
                case 991: UniqueDevice.addUniqueFighterOverclock(2, null); break;
                case 992: UniqueDevice.addUniqueFighterOverclock(3, null); break;
                case 993: UniqueDevice.addUniqueFighterOverclock(3, null); break;
                case 994: UniqueDevice.addUniqueFighterOverclock(4, null); break;
                case 995: UniqueDevice.addUniqueFighterOverclock(4, null); break;
                default: {
                    SSFMLLogger.log(
                            "[ShipFoundry] Unknown built-in device id " + deviceId + " on ship " + shipIndex
                                    + " - see DeviceList's overclock-granting cases for the current valid id list "
                                    + "(920-925 platform, 960-965 engine, 970-976/720-723 offensive, "
                                    + "980-985/730-733 defensive, 990-995 fighter)."
                    );
                }
            }
        }
    }
}