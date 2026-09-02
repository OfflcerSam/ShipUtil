package offlcersam.shipfoundry;

import com.sector.bridge.SSFMLLogger;
import game.world.SectorGenerator;
import illuminatus.core.tools.util.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Populated from the ship JSON's "registration" section via ShipRegistrar.registerSpawnSettings() - see ShipDefinition.
*/
public final class NPCRegistrar {
    // tier (0-5) -> pool of custom ship base IDs
    private static final Map<Integer, List<Integer>> MOB_POOL = new HashMap<>();

    // sector tier (0-6+) -> pool of custom ship base IDs
    private static final Map<Integer, List<Integer>> BOSS_POOL = new HashMap<>();

    // Vanilla candidate count per tier, counted directly from the rngSelection() lists in
    // _database.SpawnNPC.spawnTieredMob's tier switch.
    // tier: 0, 1, 2, 3, 4, 5
    private static final int[] VANILLA_MOB_POOL_SIZE   = { 19, 19, 14, 12, 8, 8 };

    // Vanilla candidate count per sector tier, counted from spawnBoss(Sector,int) switch case.
    // sector tier: 0, 1, 2, 3, 4, 5, 6+
    private static final int[] VANILLA_BOSS_POOL_SIZE  = { 5, 6, 8, 15, 15, 13, 15 };

    // Pool of custom ship base IDs eligible to replace vanilla police spawns (not tiered).
    private static final List<Integer> POLICE_POOL = new ArrayList<>();

    private static final int VANILLA_POLICE_POOL_SIZE = 1;

    private static final ThreadLocal<Integer> STASHED_TIER = ThreadLocal.withInitial(() -> 0);

    private NPCRegistrar() { }

    public static void stashTier(int tier) {
        STASHED_TIER.set(tier);
    }

    public static int consumeStashedTier() {
        return STASHED_TIER.get();
    }

    /**
     * Makes a ship eligible to appear as a normal tiered NPC.
     * Weight is "tickets" relative to ONE vanilla-roll ticket for that tier.
     * Weight 1 makes it roughly as common as whichever single ship vanilla would have rolled.
     * Weight 2 makes it about twice as likely, etc.
     * Tune by testing in-game.
     */
    public static void registerTieredMob(int tier, int shipBaseId, int weight) {
        List<Integer> pool = MOB_POOL.computeIfAbsent(tier, t -> new ArrayList<>());
        for (int i = 0; i < Math.max(1, weight); i++) {
            pool.add(shipBaseId);
        }
        SSFMLLogger.log("[ShipFoundry] Registered ship " + shipBaseId + " as tiered NPC (tier " + tier + ", weight " + weight + ")");
    }

    /**
     * Makes a ship eligible to appear as an elite/boss spawn for the given sector tier.
     * Same weighting rule as registerTieredMob.
     */
    public static void registerBoss(int sectorTier, int shipBaseId, int weight) {
        List<Integer> pool = BOSS_POOL.computeIfAbsent(sectorTier, t -> new ArrayList<>());
        for (int i = 0; i < Math.max(1, weight); i++) {
            pool.add(shipBaseId);
        }
        SSFMLLogger.log("[ShipFoundry] Registered ship " + shipBaseId + " as boss spawn (sector tier " + sectorTier + ", weight " + weight + ")");
    }

    // Called from SpawnNPCMixin right after the vanilla tier switch in spawnTieredMob.
    public static int rollTieredMob(int tier, int vanillaShipId) {
        List<Integer> pool = MOB_POOL.get(tier);
        if (pool == null || pool.isEmpty()) {
            return vanillaShipId;
        }

        int vanillaTickets = VANILLA_MOB_POOL_SIZE[
                Math.max(0, Math.min(tier, VANILLA_MOB_POOL_SIZE.length - 1))
                ];

        int totalTickets = vanillaTickets + pool.size();
        int roll = rng().nextInt(totalTickets);

        return roll < vanillaTickets ? vanillaShipId : pool.get(roll - vanillaTickets);
    }

    // Called from SpawnNPCMixin right after the vanilla sector-tier switch in spawnBoss(Sector,int).
    public static int rollBoss(int sectorTier, int vanillaShipId) {
        List<Integer> pool = BOSS_POOL.get(sectorTier);
        if (pool == null || pool.isEmpty()) {
            return vanillaShipId;
        }

        int vanillaTickets = VANILLA_BOSS_POOL_SIZE[
                Math.max(0, Math.min(sectorTier, VANILLA_BOSS_POOL_SIZE.length - 1))
                ];

        int totalTickets = vanillaTickets + pool.size();
        int roll = rng().nextInt(totalTickets);

        return roll < vanillaTickets ? vanillaShipId : pool.get(roll - vanillaTickets);
    }

    /**
     * Makes a ship eligible to appear as police spawn (Police Cruiser/Carrier/Corvette are all unified through
     * one spawnPolice(...) in 0.6.0.0, so this single pool covers all of them - see rollPolice).
     * Not tiered, as police spawns are not tier-gated, but same weighting mechanic as the registerTieredMob/registerBoss.
     */
    public static void registerPolice(int shipBaseId, int weight) {
        for (int i = 0; i < Math.max(1, weight); i++) {
            POLICE_POOL.add(shipBaseId);
        }
        SSFMLLogger.log("[ShipFoundry] Registered ship " + shipBaseId + " as police spawn (weight " + weight + ")");
    }

    // Called from SpawnNPCMixin right after spawnPolice resolves which literal ship id (Cruiser/Carrier/Corvette)
    // it was going to spawn - see the VANILLA_POLICE_POOL_SIZE comment above for why that's just 1 now.
    public static int rollPolice(int vanillaShipId) {
        if (POLICE_POOL.isEmpty()) {
            return vanillaShipId;
        }
        int totalTickets = VANILLA_POLICE_POOL_SIZE + POLICE_POOL.size();
        int roll = rng().nextInt(totalTickets);

        return roll < VANILLA_POLICE_POOL_SIZE ? vanillaShipId : POLICE_POOL.get(roll - VANILLA_POLICE_POOL_SIZE);
    }

    private static Random rng() {
        if (SectorGenerator.rng == null) {
            SectorGenerator.rng = new Random(false);
            SectorGenerator.rng.setSeed(game.world.WorldGenerator.seed);
        }
        return SectorGenerator.rng;
    }
}