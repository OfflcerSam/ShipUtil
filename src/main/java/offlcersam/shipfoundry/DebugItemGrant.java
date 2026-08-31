package offlcersam.shipfoundry;

import game.Player;
import mods.ModLogger;

/*
    Grants ships to a debug character's cargo hold on load, driven off the JSON registry:
    - character name "STEST" (case-insensitive) grants every successfully registered ShipFoundry JSON ship.
    - any other character name that matches a ships/<name>/ folder name (case-insensitive) grants only
      that folder's registered ships.
    - any other character name does nothing (duh).
*/
public final class DebugItemGrant {

    // Set to true to automatically deposit the ships when loading your character.
    // Maybe make into config option if a config manager is made.
    private static final boolean ENABLE_DEBUG_GRANT = true;

    private static final String DEBUG_CHARACTER_NAME_ALL = "STEST";

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

        String characterName = Player.currentName;

        if (characterName == null) {
            return;
        }

        int[] databaseIds;
        String grantLabel;

        if (DEBUG_CHARACTER_NAME_ALL.equalsIgnoreCase(characterName)) {
            databaseIds = ShipRegistrar.getShipDatabaseIDs();
            grantLabel = "all registered ships";
        } else {
            databaseIds = ShipUtilLoader.getShipIdsForModName(characterName)
                    .stream()
                    .mapToInt(ShipRegistrar::toDatabaseID)
                    .toArray();
            grantLabel = "ship pack \"" + characterName + "\"";

            if (databaseIds.length == 0) {
                // Not the "grant everything" name, and no ships/<characterName>/ folder was ever loaded - not a debug character.
                return;
            }
        }

        if (Player.ship == null || Player.ship.cargo == null) {
            ModLogger.log("[ShipFoundry] Could not grant ships: player cargo is not loaded.");
            return;
        }

        if (databaseIds.length == 0) {
            ModLogger.log("[ShipFoundry] No registered ships available to grant.");
            return;
        }

        int granted = 0;

        for (int databaseId : databaseIds) {
            Player.ship.cargo.add(databaseId, 1);
            granted++;
        }

        shipsGranted = true;

        ModLogger.log(
                "[ShipFoundry] Granted "
                        + granted
                        + " ship(s) from "
                        + grantLabel
                        + " to \""
                        + characterName
                        + "\"'s cargo hold successfully."
        );
    }
}