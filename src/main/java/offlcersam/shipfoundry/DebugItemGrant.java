package offlcersam.shipfoundry;

import com.sector.bridge.SSFMLLogger;

import game.Player;
import offlcersam.shipfoundry.ShipRegistrar;
import offlcersam.shipfoundry.ShipUtilLoader;

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

    private DebugItemGrant() {

    }

    public static void grantShipsToDebugCharacter() {

        SSFMLLogger.log("[ShipFoundry] DebugItemGrant: grantShipsToDebugCharacter() called.");

        if (!ENABLE_DEBUG_GRANT) {
            SSFMLLogger.log("[ShipFoundry] DebugItemGrant: debug grant is disabled.");
            return;
        }

        String characterName = Player.currentName;

        SSFMLLogger.log(
                "[ShipFoundry] DebugItemGrant: Player.currentName = "
                        + (characterName == null ? "<null>" : "\"" + characterName + "\"")
        );

        if (characterName == null) {
            SSFMLLogger.log("[ShipFoundry] DebugItemGrant: character name is null; aborting.");
            return;
        }

        int[] databaseIds;

        String grantLabel;

        if (DEBUG_CHARACTER_NAME_ALL.equalsIgnoreCase(characterName)) {

            SSFMLLogger.log(
                    "[ShipFoundry] DebugItemGrant: character matches \""
                            + DEBUG_CHARACTER_NAME_ALL
                            + "\"; collecting all registered ships."
            );

            databaseIds = ShipRegistrar.getShipDatabaseIDs();

            grantLabel = "all registered ships";

            SSFMLLogger.log(
                    "[ShipFoundry] DebugItemGrant: ShipRegistrar.getShipDatabaseIDs() returned "
                            + databaseIds.length
                            + " ship ID(s)."
            );

        } else {

            SSFMLLogger.log(
                    "[ShipFoundry] DebugItemGrant: character does not match \""
                            + DEBUG_CHARACTER_NAME_ALL
                            + "\"; looking for ship pack folder matching \""
                            + characterName
                            + "\"."
            );

            databaseIds = ShipUtilLoader.getShipIdsForModName(characterName)
                    .stream()
                    .mapToInt(ShipRegistrar::toDatabaseID)
                    .toArray();

            grantLabel = "ship pack \"" + characterName + "\"";

            SSFMLLogger.log(
                    "[ShipFoundry] DebugItemGrant: ship pack lookup returned "
                            + databaseIds.length
                            + " database ID(s)."
            );

            if (databaseIds.length == 0) {

                SSFMLLogger.log(
                        "[ShipFoundry] DebugItemGrant: no registered ships found for character \""
                                + characterName
                                + "\"; not a debug character."
                );
                return;
            }
        }

        if (Player.ship == null) {

            SSFMLLogger.log(
                    "[ShipFoundry] DebugItemGrant: Player.ship is null; cannot grant ships."
            );
            return;
        }

        if (Player.ship.cargo == null) {

            SSFMLLogger.log(
                    "[ShipFoundry] DebugItemGrant: Player.ship.cargo is null; cannot grant ships."
            );
            return;
        }

        SSFMLLogger.log(
                "[ShipFoundry] DebugItemGrant: player ship and cargo are loaded; attempting to grant "
                        + databaseIds.length
                        + " ship(s)."
        );

        if (databaseIds.length == 0) {
            SSFMLLogger.log("[ShipFoundry] No registered ships available to grant.");
            return;
        }

        int granted = 0;

        for (int databaseId : databaseIds) {

            SSFMLLogger.log(
                    "[ShipFoundry] DebugItemGrant: adding database ID "
                            + databaseId
                            + " to player cargo."
            );

            Player.ship.cargo.add(databaseId, 1);

            granted++;

            SSFMLLogger.log(
                    "[ShipFoundry] DebugItemGrant: database ID "
                            + databaseId
                            + " added successfully."
            );
        }

        SSFMLLogger.log(
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