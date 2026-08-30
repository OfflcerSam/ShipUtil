package offlcersam.shipfoundry;

import game.weapons.WeaponSlotLayoutList;
import illuminatus.core.graphics.Color;
import items.ItemTypeConstantsInterface;
import items.TypeTag;
import items.lists.ShipList;
import mods.ModLogger;

import java.util.ArrayList;
import java.util.List;

import static offlcersam.shipfoundry.NPCRegistrar.*;
import static offlcersam.shipfoundry.WeaponLayoutList.*;

/*
TODO: Edit this to be more JSON data magic
 */
public final class ShipRegistrar {
    private static boolean registered;

    /** Stores the base IDs of every weapon we add. */
    private static final List<Integer> REGISTERED_SHIP_IDS = new ArrayList<>();

    private ShipRegistrar() { }

    /** Registers a ship ID and remembers it for later use. */
    private static int registerShipID(int id) {
        REGISTERED_SHIP_IDS.add(id);
        ModLogger.log("[ShipTest] Added ship ID to registry: " + id);
        return id;
    }

    /** Returns database ID for all ships. */
    public static int[] getShipDatabaseIDs() {
        int[] ids = new int[REGISTERED_SHIP_IDS.size()];

        for (int i = 0; i < REGISTERED_SHIP_IDS.size(); i++) {
            ids[i] = ItemTypeConstantsInterface.SHIP * 10000 + REGISTERED_SHIP_IDS.get(i);
        }
        return ids;
    }

    /** Custom ship registration helper. */
    private static void writeShip(int id, int icon, Color color, String name, String description, int tier, TypeTag rarity, int renderIndex, int engineDisplacement, float hull, float cargo, int weaponLayout, int energySlots, int armorSlots, int shieldSlots, int deviceSlots, int moduleSlots, int engineSlots)
    {
        ShipList.write(
                registerShipID(id),
                icon,
                color,
                name,
                description,
                tier,
                rarity,
                renderIndex,
                engineDisplacement,
                hull,
                cargo,
                weaponLayout,
                energySlots,
                armorSlots,
                shieldSlots,
                deviceSlots,
                moduleSlots,
                engineSlots
        );
    }



    /** Ship registering. */
    public static void registerShips() {
        if (registered) { return; }
        registered = true;

        // Uses default cargoMod from ShipList.
        float cargoMod = 0.75F;
        float integ = 200.0F;
        float carg = 75.0F * cargoMod;

        writeShip(
                350,                         // Int: ID, unique ship ID
                30,                             // Int: Icon, sets Icon according to sprite sheet.
                Color.AZURE,                    // Color: Color, unsure what exactly this affects.
                "Arrowhead",                    // String: Display name
                "Maybe one day you could be a real arrow.", // String: Display description
                0,                              // Int: Tier, affects spawning and what level it's usable at.
                TypeTag.UNCOMMON,               // TypeTag, Affects spawning and loot drop, I think.
                350,                            // Int: Render Index, the ship's sprite, currently there is a index limit at 376 in vanilla.
                37,                             // Int: Engine Position glow in pixels
                integ * 1.50F,                  // Float: Hull HP (integ * multiplier), somewhat based off ShipList style of doing it.
                carg * 1.10F,                   // Float: Cargo (carg * multiplier), also based off ShipList style of doing it.
                ARROWHEAD_LAYOUT,               // WeaponSlotLayoutList: Weapon Layout, see WeaponSlotLayoutList for full list or make your own.
                2,                              // Int: Energy slots, unsure what the UI limit for slots are but base game doesn't go above 8 currently.
                1,                              // Int: Armor slots
                1,                              // Int: Shield slots
                0,                              // Int: Device slots
                1,                              // Int: Module slots
                1                               // Int: Engine slots
        );

        // ID testing, Max ID is 1999. 2000 it will be equipped as null.
        integ = 1000.0F;
        carg = 1000.0F * cargoMod * 5.0F;
        writeShip(1999, 216, Color.PURPLE, "GodShip", "What the hell!", 0, TypeTag.EXOTIC, 1999, 64, integ * 1.5F, carg * 1.5F, WeaponSlotLayoutList.S_10_T, 9, 9, 9, 9, 9, 9);

        integ = 225.0F;
        carg = 350.0F * cargoMod * 2.0F;
        writeShip(40, 158, Color.WHITE, "Foundry", "Build an even bigger megastructure.", 4, TypeTag.RARE, 349, 64, integ * 1.20F, carg * 1.3F, FOUNDRY_LAYOUT, 6, 5, 4, 2, 5, 4);
        writeShip(41, 216, Color.PURPLE, "Foundry+", "Build an even bigger megastructure+.", 5, TypeTag.EXOTIC, 349, 64, integ * 1.5F, carg * 1.5F, FOUNDRY_PLUS_LAYOUT, 6, 6, 5, 3, 6, 5);


        ShipList.loadShipStatsFromItems(_database.ItemDatabase.itemDataFile);

        /*
         * Opt individual ships into NPC/boss spawn pools here.
         * registerTieredMob(tier, shipBaseId, weight)
         * registerBoss(sectorTier, shipBaseId, weight)
         * registerPolice(shipBaseId, weight)
         * registerRogueDrone(tier, shipBaseId, weight, RogueDroneGear) - disabled for the moment
         * <p>
         * Weight is denominated in "vanilla ships' worth of likelihood" for that tier.
         * Vanilla's tier lists hold 8-19 roughly-equal-weight candidates (see VANILLA_MOB_POOL_SIZE), so:
         * weight 1                             - as rare as any single vanilla ship in that tier
         * weight 3-5                           - noticeably more common, still a minority overall
         * weight = VANILLA_MOB_POOL_SIZE[tier] - roughly 50/50 vs the entire vanilla list
         * weight above that                    - starts crowding vanilla ships out of that tier
         */
        NPCRegistrar.registerTieredMob(0, 350, 5); // Arrowhead can appear as a tier-0 NPC
        NPCRegistrar.registerPolice(350, 1); // Arrowhead as police spawn example.
        //NPCRegistrar.registerRogueDrone(1, 350, 1, ROGUE_GEAR_TIER1_A); // Arrowhead as tier 1 rogue drone with the loadout Tier1A
        NPCRegistrar.registerTieredMob(4, 40, 4);
        NPCRegistrar.registerTieredMob(5, 41, 3);
        NPCRegistrar.registerBoss(4, 41, 2);   // Foundry+ can appear as a tier-4 sector boss

        ModLogger.log("[ShipTest] Registered " + REGISTERED_SHIP_IDS.size() + " ships");
    }
}