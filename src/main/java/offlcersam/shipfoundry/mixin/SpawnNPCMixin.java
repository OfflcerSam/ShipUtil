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

@Mixin(value = SpawnNPC.class, remap = false)
public class SpawnNPCMixin {

    // TIERED NPC SPAWNING
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

    // BOSS SPAWNING
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

    // ROGUE DRONE SPAWNING
    @Redirect(
            method = "spawnRogueDrones(Lgame/world/Sector;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectRogueDroneShip(int xPos, int yPos, Sector sector,
                                                                int hostilityConstant, int spawnIndex, int factionIndex,
                                                                Sector origSector, int origX, int origY, int tier) {
        int rolled = NPCRegistrar.rollRogueDrone(tier, spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnRogueDrones(Lgame/world/Sector;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnNPC;configRogueDrone(ILgame/objects/SpaceShip;)V"
            )
    )
    private static void shipfoundry$redirectRogueDroneConfig(int shipId, SpaceShip tempShip) {
        if (NPCRegistrar.isCustomRogueDrone(shipId)) {
            NPCRegistrar.configureCustomRogueDrone(shipId, tempShip);
        } else {
            SpawnNPCAccessor.invokeConfigRogueDrone(shipId, tempShip);
        }
    }

    @Redirect(
            method = "spawnTempRogueDrones(Lgame/world/Sector;IIIIIZ)Lilluminatus/core/datastructures/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectTempRogueDroneShip(int xPos, int yPos, Sector sector,
                                                                    int hostilityConstant, int spawnIndex, int factionIndex,
                                                                    Sector origSector, int spawnPosSpread, int numberOf,
                                                                    int origX, int origY, int origHostility, boolean spawnLowTiers) {
        int sectorTier = spawnLowTiers ? sector.getSectorTier() / 2 : sector.getSectorTier();
        int rolled = NPCRegistrar.rollRogueDrone(sectorTier, spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnTempRogueDrones(Lgame/world/Sector;IIIIIZ)Lilluminatus/core/datastructures/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnNPC;configRogueDrone(ILgame/objects/SpaceShip;)V"
            )
    )
    private static void shipfoundry$redirectTempRogueDroneConfig(int shipId, SpaceShip tempShip) {
        if (NPCRegistrar.isCustomRogueDrone(shipId)) {
            NPCRegistrar.configureCustomRogueDrone(shipId, tempShip);
        } else {
            SpawnNPCAccessor.invokeConfigRogueDrone(shipId, tempShip);
        }
    }

    // BLOB SPAWNING
    @Redirect(
            method = "spawnBlobs(Lgame/world/Sector;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectBlobShip(int xPos, int yPos, Sector sector,
                                                          int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollBlob(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnBlobsGuarding(IILilluminatus/core/tools/util/Random;Lgame/world/Sector;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectBlobGuardingShip(int xPos, int yPos, Sector sector,
                                                                  int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollBlob(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnTempBlobSwarm(Lgame/world/Sector;IIIIIZ)Lilluminatus/core/datastructures/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectTempBlobSwarmShip(int xPos, int yPos, Sector sector,
                                                                   int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollBlob(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnBlobBoss(Lgame/world/Sector;IIIZ)Lilluminatus/core/datastructures/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectBlobBossShip(int xPos, int yPos, Sector sector,
                                                              int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollBlobBoss(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    // SHARD SPAWNING
    @Redirect(
            method = "spawnTieredShard(Lgame/world/Sector;IZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectShardShip(int xPos, int yPos, Sector sector,
                                                           int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollShard(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnShardBoss(Lgame/world/Sector;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectShardBossShip(int xPos, int yPos, Sector sector,
                                                               int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollShardBoss(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnTempShardBoss(Lgame/world/Sector;IIIZ)Lilluminatus/core/datastructures/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectTempShardBossShip(int xPos, int yPos, Sector sector,
                                                                   int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollShardBoss(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    // BROODLING SPAWNING
    @Redirect(
            method = "spawnBrood(Lgame/world/Sector;IILilluminatus/core/tools/util/Random;ZZZZ)Lgame/objects/SpaceShip;",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectBroodlingShip(int xPos, int yPos, Sector sector,
                                                               int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollBroodling(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    // LURKER SPAWNING
    @Redirect(
            method = "spawnLurker(Lgame/world/Sector;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectLurkerShip(int xPos, int yPos, Sector sector,
                                                            int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollLurker(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnLurkers(Lgame/world/Sector;IIIIZZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectLurkersShip(int xPos, int yPos, Sector sector,
                                                             int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollLurker(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnTempLurkers(Lgame/world/Sector;IIIIZ)Lilluminatus/core/datastructures/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectTempLurkersShip(int xPos, int yPos, Sector sector,
                                                                 int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollLurker(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnLurkerCarrier(Lgame/world/Sector;IIZZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/spawns/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shipfoundry$redirectLurkerCarrierShip(int xPos, int yPos, Sector sector,
                                                                   int hostilityConstant, int spawnIndex, int factionIndex) {
        int rolled = NPCRegistrar.rollLurker(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }
}