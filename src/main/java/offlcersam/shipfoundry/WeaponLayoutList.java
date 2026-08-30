package offlcersam.shipfoundry;

import game.weapons.WeaponSlotLayoutList;
import illuminatus.core.datastructures.List;
import mods.ModLogger;
import game.weapons.WeaponTurretPlacement;

/*
TODO: Edit this to be more JSON data magic
 */
public class WeaponLayoutList {
    // Add layout variables up here.
    public static int ARROWHEAD_LAYOUT;
    public static int FOUNDRY_LAYOUT;
    public static int FOUNDRY_PLUS_LAYOUT;


    // Trying to mimic vanilla WeaponSlotLayoutList for fun
    private static WeaponTurretPlacement placement;

    public static void init() {
        ModLogger.log("[ShipTest] Loading custom weapon layouts...");

        placement = new WeaponTurretPlacement(); // Make variable new.
        placement.addSlot(32.5, -9.2); // Add a slot with angle and distance from center. Treat it as 0,0 on a graph.
        placement.addSlot(-32.5, -9.2);
        placement.addSlot(0.0, -6.0);
        ARROWHEAD_LAYOUT = WeaponSlotLayoutList.layouts.add(placement); // Add placements to vanilla layout list.

        //these are a bit lazied but its whatever
        placement = new WeaponTurretPlacement();
        placement.addSlot(49.0, 49.5);
        placement.addSlot(-49.5, 50.0);
        placement.addSlot(20.5, 34.5);
        placement.addSlot(-21.5, 35.0);
        placement.addSlot(20.7, 52.5);
        placement.addSlot(-20.0, 51.5);
        FOUNDRY_LAYOUT = WeaponSlotLayoutList.layouts.add(placement);

        placement = new WeaponTurretPlacement();
        placement.addSlot(49.0, 49.5);
        placement.addSlot(-49.5, 50.0);
        placement.addSlot(20.5, 34.5);
        placement.addSlot(-21.5, 35.0);
        placement.addSlot(20.7, 52.5);
        placement.addSlot(-20.0, 51.5);
        placement.addSlot(-0.2, 50.0);
        FOUNDRY_PLUS_LAYOUT = WeaponSlotLayoutList.layouts.add(placement);


        ModLogger.log("[ShipTest] Loaded custom weapon layouts, next index: " + WeaponSlotLayoutList.layouts.size());
    }
}