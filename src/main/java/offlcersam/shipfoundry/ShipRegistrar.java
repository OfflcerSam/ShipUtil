package offlcersam.shipfoundry;

import game.weapons.WeaponSlotLayoutList;
import game.weapons.WeaponTurretPlacement;
import illuminatus.core.graphics.Color;
import items.ItemTypeConstantsInterface;
import items.Stat;
import items.TypeTag;
import items.lists.ShipList;
import mods.ModLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON ship registration.
 */
public final class ShipRegistrar {

    /** Stores the base IDs of every ship we add. */
    private static final List<Integer> REGISTERED_SHIP_IDS = new ArrayList<>();

    /** Stores the base IDs of ships that should be added to markets. */
    private static final List<Integer> MARKET_SHIP_IDS = new ArrayList<>();

    /** Stores every successfully registered ship's full definition, for anything that needs more than just an id (e.g. recipes). */
    private static final List<ShipDefinition> LOADED_SHIPS = new ArrayList<>();

    /** Per-ship id -> resolved Stat bonuses to reapply every time ShipList.compile() runs for that ship id. */
    private static final Map<Integer, List<StatBonus>> SHIP_STATS = new HashMap<>();

    /** Per-ship id -> device item ids to install into a free device slot every time that ship's stats compile. */
    private static final Map<Integer, List<Integer>> BUILT_IN_DEVICES = new HashMap<>();

    /** Per-ship id -> extra items it can drop when destroyed as an NPC/boss, on top of the normal generic loot. */
    private static final Map<Integer, List<ShipDefinition.UniqueLootDrop>> UNIQUE_LOOT = new HashMap<>();

    /**
     * Per-ship id -> forced hull object type (8 station / 9 platform / 10 normal ship, matching
     * game.shiputils.ShipStats's own type constants). Populated for every ship this mod registers, so a JSON
     * ship's classification always matches its "isStation"/"isPlatform" flags directly instead of depending on
     * which numeric bracket its id happens to land in - see ShipStatsMixin.
     */
    private static final Map<Integer, Integer> HULL_TYPE_OVERRIDES = new HashMap<>();

    private static final int HULL_TYPE_STATION = 8;
    private static final int HULL_TYPE_PLATFORM = 9;
    private static final int HULL_TYPE_SHIP = 10;

    /** A single resolved Stat.flatVal()/percentVal() call to make whenever the owning ship recompiles. */
    public record StatBonus(Stat stat, Float flat, Float percent) {
    }

    private ShipRegistrar() {
    }

    /** Registers a ship ID and remembers it for later use. */
    private static int registerShipID(int id) {
        REGISTERED_SHIP_IDS.add(id);
        ModLogger.log("[ShipFoundry] Added ship ID to registry: " + id);
        return id;
    }

    /** Returns database ID for all registered ships. */
    public static int[] getShipDatabaseIDs() {
        int[] ids = new int[REGISTERED_SHIP_IDS.size()];

        for (int i = 0; i < REGISTERED_SHIP_IDS.size(); i++) {
            ids[i] = toDatabaseID(REGISTERED_SHIP_IDS.get(i));
        }
        return ids;
    }

    /** Returns database IDs for ships that opted into market registration. */
    public static int[] getMarketShipDatabaseIDs() {
        int[] ids = new int[MARKET_SHIP_IDS.size()];

        for (int i = 0; i < MARKET_SHIP_IDS.size(); i++) {
            ids[i] = toDatabaseID(MARKET_SHIP_IDS.get(i));
        }
        return ids;
    }

    /**
     * Converts a ship base ID into the game's ship database/item ID
     */
    public static int toDatabaseID(int shipBaseId) {
        return ItemTypeConstantsInterface.SHIP * 10000 + shipBaseId;
    }

    /**
     * Registers a ship from its JSON definition.
     * NPC, boss and police spawning are registered later after every custom
     * ship has been written and ShipList has reloaded its ship stats.
     */
    public static void registerShip(ShipDefinition def) {
        int weaponLayoutIndex;

        if (def.vanillaWeaponLayout() != null) {
            weaponLayoutIndex = resolveVanillaWeaponLayout(def.vanillaWeaponLayout());
        } else {
            WeaponTurretPlacement placement = new WeaponTurretPlacement();

            for (ShipDefinition.TurretSlot slot : def.weaponLayout()) {
                placement.addSlot(slot.angle(), slot.distance());
            }

            weaponLayoutIndex = WeaponSlotLayoutList.layouts.add(placement);
        }

        Color color = resolveConstant(Color.class, def.color(), "color");
        TypeTag rarity = resolveConstant(TypeTag.class, def.rarity(), "rarity");

        int hullType = def.isStation() ? HULL_TYPE_STATION : def.isPlatform() ? HULL_TYPE_PLATFORM : HULL_TYPE_SHIP;
        HULL_TYPE_OVERRIDES.put(def.id(), hullType);

        if (hullType != HULL_TYPE_SHIP) {
            ModLogger.log(
                    "[ShipFoundry] Registered ship " + def.name()
                            + " as a " + (hullType == HULL_TYPE_STATION ? "station" : "platform")
                            + " - if this is the intent all is fine."
            );
        }

        if (!def.shipStats().isEmpty()) {
            List<StatBonus> statBonuses = new ArrayList<>();
            for (ShipDefinition.ShipStat shipStat : def.shipStats()) {
                Stat stat = resolveConstant(Stat.class, shipStat.stat(), "shipStats.stat");
                statBonuses.add(new StatBonus(stat, shipStat.flat(), shipStat.percent()));
            }
            SHIP_STATS.put(def.id(), List.copyOf(statBonuses));
            ModLogger.log("[ShipFoundry] Registered " + statBonuses.size() + " shipStats bonus(es) for ship " + def.name());
        }

        if (!def.builtInDevices().isEmpty()) {
            BUILT_IN_DEVICES.put(def.id(), def.builtInDevices());
            ModLogger.log("[ShipFoundry] Registered " + def.builtInDevices().size() + " built-in device(s) for ship " + def.name());
        }

        ShipList.write(
                registerShipID(def.id()),
                def.icon(),
                color,
                def.name(),
                def.description(),
                def.tier(),
                rarity,
                def.renderIndex(),
                def.engineDisplacement(),
                def.hull(),
                def.cargo(),
                weaponLayoutIndex,
                def.energySlots(),
                def.armorSlots(),
                def.shieldSlots(),
                def.deviceSlots(),
                def.moduleSlots(),
                def.engineSlots()
        );

        if (def.registration().market()) {
            MARKET_SHIP_IDS.add(def.id());
            ModLogger.log("[ShipFoundry] Registered ship " + def.name() + " for market listings");
        }

        LOADED_SHIPS.add(def);

        ModLogger.log("[ShipFoundry] Registered ship " + def.name() + " (id: " + def.id() + ")");
    }

    /**
     * Registers optional NPC, boss and police spawning from the ship JSON.
     */
    public static void registerSpawnSettings(ShipDefinition def) {
        ShipDefinition.Registration registration = def.registration();

        for (ShipDefinition.NpcSpawn npc : registration.npc()) {
            NPCRegistrar.registerTieredMob(
                    npc.tier(),
                    def.id(),
                    npc.weight()
            );
        }

        for (ShipDefinition.BossSpawn boss : registration.boss()) {
            NPCRegistrar.registerBoss(
                    boss.sectorTier(),
                    def.id(),
                    boss.weight()
            );
        }

        if (registration.police() != null) {
            NPCRegistrar.registerPolice(
                    def.id(),
                    registration.police().weight()
            );
        }

        if (!registration.uniqueLoot().isEmpty()) {
            UNIQUE_LOOT.put(def.id(), registration.uniqueLoot());
            ModLogger.log("[ShipFoundry] Registered " + registration.uniqueLoot().size() + " unique loot drop(s) for ship " + def.name());
        }
    }

    /**
     * Resolves game constants such as Color.AZURE and TypeTag.UNCOMMON
     * from their names stored in JSON.
     */
    private static <T> T resolveConstant(Class<T> type, String name, String fieldLabel) {
        try {
            return type.cast(type.getField(name.toUpperCase()).get(null));
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Unknown " + fieldLabel + " " + name
                            + " - check the exact constant name on "
                            + type.getName()
            );
        }
    }

    /**
     * Resolves a vanilla WeaponSlotLayoutList constant name (e.g. "S_10_T") to the layout index it was assigned at WeaponSlotLayoutList.init().
     * Case-insensitive, matching resolveConstant's convention.
     */
    private static int resolveVanillaWeaponLayout(String name) {
        try {
            return WeaponSlotLayoutList.class.getField(name.toUpperCase()).getInt(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Unknown vanilla weaponLayout \"" + name
                            + "\" - check the exact constant name on " + WeaponSlotLayoutList.class.getName()
                            + " (e.g. S_1_V .. S_10_V, S_7_T .. S_10_T)"
            );
        }
    }

    /** Returns the number of registered ships. */
    public static int getRegisteredShipCount() {
        return REGISTERED_SHIP_IDS.size();
    }

    /** Returns every successfully registered ship's full definition. */
    public static List<ShipDefinition> getLoadedShips() {
        return List.copyOf(LOADED_SHIPS);
    }

    /** Returns the resolved Stat bonuses to apply for a ship id, or an empty list if it has none. */
    public static List<StatBonus> getShipStats(int shipBaseId) {
        return SHIP_STATS.getOrDefault(shipBaseId, List.of());
    }

    /** Returns the built-in device item ids to apply for a ship id, or an empty list if it has none. */
    public static List<Integer> getBuiltInDevices(int shipBaseId) {
        return BUILT_IN_DEVICES.getOrDefault(shipBaseId, List.of());
    }

    /** Returns the extra unique loot drops for a ship id, or an empty list if it has none. */
    public static List<ShipDefinition.UniqueLootDrop> getUniqueLoot(int shipBaseId) {
        return UNIQUE_LOOT.getOrDefault(shipBaseId, List.of());
    }

    /** Returns the forced hull object type for a ship id this mod registered, or null if it's not one of ours. */
    public static Integer getHullTypeOverride(int shipBaseId) {
        return HULL_TYPE_OVERRIDES.get(shipBaseId);
    }
}