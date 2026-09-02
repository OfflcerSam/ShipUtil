package offlcersam.shipfoundry.mixin;

import game.objects.SpaceShip;
import game.spawns.SpawnNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// populateShipGear() and configRogueDrone() are both PRIVATE static methods on SpawnNPC, so calling code
// (like NPCRegistrar) can't reach them directly. This interface mixin lets Mixin generate bridge methods
// into SpawnNPC at weave time so they can be called from outside the class. The method bodies below are
// never actually run, Mixin replaces them.
@Mixin(value = SpawnNPC.class, remap = false)
public interface SpawnNPCAccessor {

    @Invoker("populateShipGear")
    static void invokePopulateShipGear(SpaceShip spaceship, int tier, int pilotBehaviour, boolean forceEquipTether) {
        throw new AssertionError();
    }

    @Invoker("configRogueDrone")
    static void invokeConfigRogueDrone(int shipId, SpaceShip tempShip) {
        throw new AssertionError();
    }
}