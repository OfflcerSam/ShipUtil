package offlcersam.shipfoundry;

import game.weapons.WeaponSlotLayoutList;
import game.weapons.WeaponTurretPlacement;
import illuminatus.core.graphics.Color;
import items.ItemTypeConstantsInterface;
import items.TypeTag;
import items.lists.ShipList;
import mods.ModLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON ship registration.
 */
public final class ShipRegistrar {

    /** Stores the base IDs of every ship we add. */
    private static final List<Integer> REGISTERED_SHIP_IDS = new ArrayList<>();

    /** Stores the base IDs of ships that should be added to markets. */
    private static final List<Integer> MARKET_SHIP_IDS = new ArrayList<>();

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

    /** Converts a ship base ID into the game's ship database ID. */
    private static int toDatabaseID(int shipBaseId) {
        return ItemTypeConstantsInterface.SHIP * 10000 + shipBaseId;
    }

    /**
     * Registers a ship from its JSON definition.
     */
    public static void registerShip(ShipDefinition def) {
        WeaponTurretPlacement placement = new WeaponTurretPlacement();

        for (ShipDefinition.TurretSlot slot : def.weaponLayout()) {
            placement.addSlot(slot.angle(), slot.distance());
        }

        int weaponLayoutIndex = WeaponSlotLayoutList.layouts.add(placement);

        Color color = resolveConstant(Color.class, def.color(), "color");
        TypeTag rarity = resolveConstant(TypeTag.class, def.rarity(), "rarity");

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

        registerNPCSpawns(def);

        ModLogger.log("[ShipFoundry] Registered ship " + def.name() + " (id: " + def.id() + ")");
    }

    /**
     * Registers optional NPC, boss and police spawning from the ship JSON.
     */
    private static void registerNPCSpawns(ShipDefinition def) {
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

    /** Returns the number of registered ships. */
    public static int getRegisteredShipCount() {
        return REGISTERED_SHIP_IDS.size();
    }
}
