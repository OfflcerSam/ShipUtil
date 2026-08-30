package offlcersam.shipfoundry;

import game.Player;
import mods.ModLogger;

/*
    TODO: Make this read from the JSON files instead and give every registered ship of the folder name, or just "ShipTest" for all.
    Currently this grants every successfully registered ShipFoundry JSON ship.
*/
public final class DebugItemGrant {

    // Set to true to automatically deposit the ships when loading your character.
    // Maybe make into config option if a config manager is made.
    private static final boolean ENABLE_DEBUG_GRANT = true;

    private static final String DEBUG_CHARACTER_NAME = "STEST";

    /*
     * Prevents the ships from being granted repeatedly if the player-loading
     * code calls this method more than once during the same game session.
     */
    private static boolean shipsGranted;

    private DebugItemGrant() {
    }

    public static void grantShipsToDebugCharacter() {
        if (!ENABLE_DEBUG_GRANT) {
            return;
        }

        if (shipsGranted) {
            return;
        }

        if (!DEBUG_CHARACTER_NAME.equalsIgnoreCase(Player.currentName)) {
            return;
        }

        if (Player.ship == null || Player.ship.cargo == null) {
            ModLogger.log("[ShipFoundry] Could not grant ships: player cargo is not loaded.");
            return;
        }

        int[] ships = ShipRegistrar.getShipDatabaseIDs();

        if (ships.length == 0) {
            ModLogger.log("[ShipFoundry] No registered ships available to grant.");
            return;
        }

        int granted = 0;

        for (int shipID : ships) {
            Player.ship.cargo.add(shipID, 1);
            granted++;
        }

        shipsGranted = true;

        ModLogger.log(
                "[ShipFoundry] Granted "
                        + granted
                        + " registered ship(s) to "
                        + DEBUG_CHARACTER_NAME
                        + "'s cargo hold successfully."
        );
    }
}
