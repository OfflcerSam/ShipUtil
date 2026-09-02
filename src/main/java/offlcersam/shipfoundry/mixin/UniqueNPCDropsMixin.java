package offlcersam.shipfoundry.mixin;

import _database.Unique_NPC_Drops;
import game.objects.FloatingItem;
import game.objects.SpaceShip;
import game.spawns.SpawnMacro;
import game.world.Sector;
import illuminatus.core.datastructures.List;
import illuminatus.core.tools.util.Utils;
import offlcersam.shipfoundry.ShipDefinition;
import offlcersam.shipfoundry.ShipRegistrar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Unique_NPC_Drops.class, remap = false)
public class UniqueNPCDropsMixin {

    @Inject(method = "dropSpecial", at = @At("HEAD"))
    private static void shipfoundry$dropUniqueLoot(List<FloatingItem> addTo, int x, int y, Sector sector, SpaceShip ship, boolean isBoss, int lootTier, CallbackInfoReturnable<Boolean> cir) {
        if (ship == null) {
            return;
        }

        int shipIndex = ship.getSpawnIndex();

        for (ShipDefinition.UniqueLootDrop drop : ShipRegistrar.getUniqueLoot(shipIndex)) {
            if (drop.chance() < 100 && !Utils.prob(drop.chance() / 100.0)) {
                continue;
            }

            SpawnMacro.spawnItem(x, y, drop.id(), drop.amount(), false);
        }
    }
}