package offlcersam.shipfoundry.mixin;

import game.spawns.SpawnMacro;
import game.spawns.SpawnNPC;
import game.objects.SpaceShip;
import game.world.Sector;
import illuminatus.core.datastructures.List;
import offlcersam.shipfoundry.NPCRegistrar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Sector Space 0.6.0.0 moved SpawnNPC and SpawnMacro from _database to game.spawns as part of the
// "Encounters spawning system rewrite" - see MainSetupMixin/ShipRegistrar for the other 0.6.0.0 breakages.
@Mixin(value = SpawnNPC.class, remap = false)
public class SpawnNPCMixin {

    // TIERED NPC SPAWNING - spawnTieredMob's own signature is unchanged in 0.6.0.0, only the package moved.
    @Inject(
            method = "spawnTieredMob(IIILgame/world/Sector;ZIIZII)Lilluminatus/core/datastructures/List;",
            at = @At("HEAD")
    )
    private static void shipfoundry$captureTier(int x, int y, int spawnPosSpread, Sector sector,
                                                boolean orphan, int mobSize, int hostilityConstant,
                                                boolean stayAtSpawn, int forceFaction, int tier,
                                                CallbackInfoReturnable<List<SpaceShip>> cir) {
        NPCRegistrar.stashTier(tier);
    }

    @Redirect(
            method = "spawnTieredMob(IIILgame/world/Sector;ZIIZII)Lilluminatus/core/datastructures/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectTieredMobShip(int xPos, int yPos, Sector sector,
                                                               int hostilityConstant, int spawnIndex,
                                                               int factionIndex) {
        int rolled = NPCRegistrar.rollTieredMob(NPCRegistrar.consumeStashedTier(), spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    // POLICE SPAWNING
    @Redirect(
            method = "spawnPolice(Lgame/world/Sector;IIIIZZZ)Lgame/objects/SpaceShip;",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectPoliceShip(int xPos, int yPos, Sector sector,
                                                            int hostilityConstant, int spawnIndex,
                                                            int factionIndex) {
        int rolled = NPCRegistrar.rollPolice(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    // BOSS SPAWNING - spawnBoss's own signature is unchanged in 0.6.0.0, only the package moved.
    @Redirect(
            method = "spawnBoss(Lgame/world/Sector;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnNPC;spawnBoss(IILgame/world/Sector;I)V"
            )
    )
    private static void shipfoundry$redirectBoss(int shipIndex, int faction, Sector sector, int bossSlot) {
        int rolled = NPCRegistrar.rollBoss(sector.getSectorTier(), shipIndex);
        SpawnNPC.spawnBoss(rolled, faction, sector, bossSlot);
    }
}