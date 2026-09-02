package offlcersam.shipfoundry;

import com.sector.bridge.SSFMLLogger;
import _database.NameDatabase;
import game.objects.SpaceShip;
import game.world.SectorGenerator;
import illuminatus.core.tools.util.Random;
import offlcersam.shipfoundry.mixin.SpawnNPCAccessor;

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

    // bucket (0-4, where 4 covers tier 4 or higher) -> pool of custom ship base IDs
    private static final Map<Integer, List<Integer>> ROGUE_DRONE_POOL = new HashMap<>();

    // bucket -> the gear preset to use for whichever custom ship gets rolled from that bucket
    private static final Map<Integer, ShipDefinition.RogueDroneSpawn> ROGUE_DRONE_GEAR = new HashMap<>();

    // Range sizes (highestShipIndex - lowestShipIndex + 1) counted from both spawnRogueDrones and
    // spawnTempRogueDrones (they use identical range tables). Only 5 buckets exist because vanilla's own
    // Utils.constrain(0, tier, 5) switch only has explicit cases 0-3; tiers 4 and 5 fall into the same
    // default case.
    // bucket: 0, 1, 2, 3, 4+
    private static final int[] VANILLA_ROGUE_DRONE_POOL_SIZE = { 4, 4, 5, 5, 5 };

    // Every pool below competes 1 vanilla ticket against the custom pool for that exact vanilla ship id,
    // same rule as POLICE_POOL. Gear for all of these is applied procedurally (by tier/type/flag) rather
    // than dispatched off the literal ship id the way rogue drones are, so a substituted custom ship gets
    // appropriate gear automatically - no gear preset needed, unlike ROGUE_DRONE_GEAR.
    private static final int VANILLA_SINGLE_TICKET = 1;

    // vanilla blob id (190 + tier, 0-5) -> pool of custom ship base IDs
    private static final Map<Integer, List<Integer>> BLOB_POOL = new HashMap<>();

    // Pool of custom ship base IDs eligible to replace the fixed "blob boss" (vanilla id 196).
    private static final List<Integer> BLOB_BOSS_POOL = new ArrayList<>();

    // vanilla shard id (171 + nebula type, 0-5) -> pool of custom ship base IDs
    private static final Map<Integer, List<Integer>> SHARD_POOL = new HashMap<>();

    // vanilla shard boss id (197 normal, 198 darker) -> pool of custom ship base IDs
    private static final Map<Integer, List<Integer>> SHARD_BOSS_POOL = new HashMap<>();

    // vanilla broodling-family id (223 broodling, 224 regular, 226 queen, 227 darkQueen) -> pool
    private static final Map<Integer, List<Integer>> BROODLING_POOL = new HashMap<>();

    private static final Map<String, Integer> BROODLING_TYPE_TO_ID = Map.of(
            "broodling", 223,
            "queen", 226,
            "darkQueen", 227,
            "regular", 224
    );

    // Single pool shared across every lurker sub-variant (normal 177/179, lurkerling 178, carrier 45) and
    // every spawn method that can produce one - same "one pool covers several vanilla variants" precedent
    // as POLICE_POOL covering Cruiser/Carrier/Corvette.
    private static final List<Integer> LURKER_POOL = new ArrayList<>();

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

    /**
     * Makes a ship eligible to appear as a rogue drone, using the given gear preset for its bucket.
     * Same weighting rule as registerTieredMob/registerBoss.
     */
    public static void registerRogueDrone(ShipDefinition.RogueDroneSpawn spawn, int shipBaseId) {
        int bucket = rogueDroneBucket(spawn.tier());
        List<Integer> pool = ROGUE_DRONE_POOL.computeIfAbsent(bucket, b -> new ArrayList<>());

        for (int i = 0; i < Math.max(1, spawn.weight()); i++) {
            pool.add(shipBaseId);
        }

        ROGUE_DRONE_GEAR.put(shipBaseId, spawn);
        SSFMLLogger.log(
                "[ShipFoundry] Registered ship " + shipBaseId
                        + " as rogue drone (tier " + spawn.tier() + " -> bucket " + bucket
                        + ", weight " + spawn.weight() + ")"
        );
    }

    // Maps a tier value to the 0-4 bucket vanilla's Utils.constrain(0,tier,5) switch collapses tiers 4/5 into.
    private static int rogueDroneBucket(int tier) {
        return Math.max(0, Math.min(tier, 4));
    }

    // Called from SpawnNPCMixin right after the vanilla range roll in spawnRogueDrones/spawnTempRogueDrones.
    public static int rollRogueDrone(int tier, int vanillaShipId) {
        int bucket = rogueDroneBucket(tier);
        List<Integer> pool = ROGUE_DRONE_POOL.get(bucket);
        if (pool == null || pool.isEmpty()) {
            return vanillaShipId;
        }

        int vanillaTickets = VANILLA_ROGUE_DRONE_POOL_SIZE[
                Math.max(0, Math.min(bucket, VANILLA_ROGUE_DRONE_POOL_SIZE.length - 1))
                ];

        int totalTickets = vanillaTickets + pool.size();
        int roll = rng().nextInt(totalTickets);

        return roll < vanillaTickets ? vanillaShipId : pool.get(roll - vanillaTickets);
    }

    public static boolean isCustomRogueDrone(int shipId) {
        return ROGUE_DRONE_GEAR.containsKey(shipId);
    }

    // Called from SpawnNPCMixin instead of vanilla configRogueDrone() when shipId is one of ours - reproduces
    // configRogueDrone's tail with our own gear preset instead of vanilla's per-id switch.
    public static void configureCustomRogueDrone(int shipId, SpaceShip tempShip) {
        ShipDefinition.RogueDroneSpawn gear = ROGUE_DRONE_GEAR.get(shipId);
        if (gear == null) {
            return;
        }

        SpawnNPCAccessor.invokePopulateShipGear(tempShip, gear.tier(), 6, false);

        tempShip.hull.energySlots.removeAll();
        tempShip.hull.equipEnergy(gear.energyFullID(), tempShip.hull.energySlots.numberOf());

        tempShip.hull.weaponSlots.removeAll();
        for (int i = 0; i < tempShip.hull.weaponSlots.numberOf(); i++) {
            if (rng().nextInt(2) == 1) {
                tempShip.hull.equipWeapon(gear.weaponBay(), 1);
            } else {
                tempShip.hull.equipWeapon(gear.weaponLaser(), 1);
            }
        }

        int level = gear.levelMin() + rng().nextInt(gear.levelMax() - gear.levelMin() + 1);
        switch (rng().nextInt(9)) {
            case 0 -> tempShip.classSkill.set(level, 2);
            case 1 -> tempShip.classSkill.set(level, 1);
            case 2 -> tempShip.classSkill.set(level, 4);
            case 3 -> tempShip.classSkill.set(level, 3);
            default -> tempShip.classSkill.set(level, 0);
        }

        long creditDrop = gear.creditMin() + rng().nextInt((int) (gear.creditMax() - gear.creditMin() + 1));
        tempShip.cargo.setCurrency(creditDrop);
        tempShip.setCustomTag(NameDatabase.getRandomMachineShipName());
    }

    /**
     * Makes a ship eligible to appear as a candidate blob for the given tier.
     * Same weighting rule as registerTieredMob/registerBoss.
     */
    public static void registerBlob(int tier, int shipBaseId, int weight) {
        int vanillaShipId = 190 + Math.max(0, Math.min(tier, 5));
        addToPool(BLOB_POOL, vanillaShipId, shipBaseId, weight);
        SSFMLLogger.log("[ShipFoundry] Registered ship " + shipBaseId + " as blob (tier " + tier + ", weight " + weight + ")");
    }

    // Called from SpawnNPCMixin right after the vanilla shipId resolves in spawnBlobs/spawnBlobsGuarding/spawnTempBlobSwarm.
    public static int rollBlob(int vanillaShipId) {
        return rollSingleTicket(BLOB_POOL, vanillaShipId);
    }

    /**
     * Makes a ship eligible to replace the fixed "blob boss" (vanilla id 196).
     */
    public static void registerBlobBoss(int shipBaseId, int weight) {
        for (int i = 0; i < Math.max(1, weight); i++) {
            BLOB_BOSS_POOL.add(shipBaseId);
        }
        SSFMLLogger.log("[ShipFoundry] Registered ship " + shipBaseId + " as blob boss (weight " + weight + ")");
    }

    // Called from SpawnNPCMixin right after the vanilla generateShip call in spawnBlobBoss.
    public static int rollBlobBoss(int vanillaShipId) {
        return rollFlatSingleTicket(BLOB_BOSS_POOL, vanillaShipId);
    }

    /**
     * Makes a ship eligible to appear as a candidate shard mob for the given nebula type.
     * Same weighting rule as registerTieredMob/registerBoss.
     */
    public static void registerShard(int type, int shipBaseId, int weight) {
        int vanillaShipId = 171 + Math.max(0, Math.min(type, 5));
        addToPool(SHARD_POOL, vanillaShipId, shipBaseId, weight);
        SSFMLLogger.log("[ShipFoundry] Registered ship " + shipBaseId + " as shard (type " + type + ", weight " + weight + ")");
    }

    // Called from SpawnNPCMixin right after the vanilla shipId resolves in spawnTieredShard.
    public static int rollShard(int vanillaShipId) {
        return rollSingleTicket(SHARD_POOL, vanillaShipId);
    }

    /**
     * Makes a ship eligible to replace a shard boss. darker picks which of vanilla's two shard boss ids
     * (197 normal, 198 darker) this entry competes against.
     */
    public static void registerShardBoss(boolean darker, int shipBaseId, int weight) {
        int vanillaShipId = darker ? 198 : 197;
        addToPool(SHARD_BOSS_POOL, vanillaShipId, shipBaseId, weight);
        SSFMLLogger.log("[ShipFoundry] Registered ship " + shipBaseId + " as shard boss (darker " + darker + ", weight " + weight + ")");
    }

    // Called from SpawnNPCMixin right after the vanilla generateShip call in spawnShardBoss/spawnTempShardBoss.
    public static int rollShardBoss(int vanillaShipId) {
        return rollSingleTicket(SHARD_BOSS_POOL, vanillaShipId);
    }

    /**
     * Makes a ship eligible to replace a broodling-family spawn. type selects which of vanilla's four
     * fixed ids this entry competes against - see ShipDefinition.BroodlingSpawn for the mapping.
     */
    public static void registerBroodling(String type, int shipBaseId, int weight) {
        Integer vanillaShipId = BROODLING_TYPE_TO_ID.get(type);
        if (vanillaShipId == null) {
            return;
        }
        addToPool(BROODLING_POOL, vanillaShipId, shipBaseId, weight);
        SSFMLLogger.log("[ShipFoundry] Registered ship " + shipBaseId + " as broodling (type " + type + ", weight " + weight + ")");
    }

    // Called from SpawnNPCMixin right after the vanilla generateShip call in spawnBrood.
    public static int rollBroodling(int vanillaShipId) {
        return rollSingleTicket(BROODLING_POOL, vanillaShipId);
    }

    /**
     * Makes a ship eligible to appear anywhere a lurker (normal, lurkerling, or lurker carrier) would -
     * one shared pool, competing 1 vanilla ticket at a time against whichever lurker spawn method rolls.
     */
    public static void registerLurker(int shipBaseId, int weight) {
        for (int i = 0; i < Math.max(1, weight); i++) {
            LURKER_POOL.add(shipBaseId);
        }
        SSFMLLogger.log("[ShipFoundry] Registered ship " + shipBaseId + " as lurker (weight " + weight + ")");
    }

    // Called from SpawnNPCMixin right after the vanilla generateShip call in spawnLurker/spawnLurkers/spawnTempLurkers/spawnLurkerCarrier.
    public static int rollLurker(int vanillaShipId) {
        return rollFlatSingleTicket(LURKER_POOL, vanillaShipId);
    }

    private static void addToPool(Map<Integer, List<Integer>> pool, int key, int shipBaseId, int weight) {
        List<Integer> list = pool.computeIfAbsent(key, k -> new ArrayList<>());
        for (int i = 0; i < Math.max(1, weight); i++) {
            list.add(shipBaseId);
        }
    }

    private static int rollSingleTicket(Map<Integer, List<Integer>> pool, int vanillaShipId) {
        List<Integer> list = pool.get(vanillaShipId);
        if (list == null || list.isEmpty()) {
            return vanillaShipId;
        }
        int totalTickets = VANILLA_SINGLE_TICKET + list.size();
        int roll = rng().nextInt(totalTickets);
        return roll < VANILLA_SINGLE_TICKET ? vanillaShipId : list.get(roll - VANILLA_SINGLE_TICKET);
    }

    private static int rollFlatSingleTicket(List<Integer> pool, int vanillaShipId) {
        if (pool.isEmpty()) {
            return vanillaShipId;
        }
        int totalTickets = VANILLA_SINGLE_TICKET + pool.size();
        int roll = rng().nextInt(totalTickets);
        return roll < VANILLA_SINGLE_TICKET ? vanillaShipId : pool.get(roll - VANILLA_SINGLE_TICKET);
    }

    private static Random rng() {
        if (SectorGenerator.rng == null) {
            SectorGenerator.rng = new Random(false);
            SectorGenerator.rng.setSeed(game.world.WorldGenerator.seed);
        }
        return SectorGenerator.rng;
    }
}