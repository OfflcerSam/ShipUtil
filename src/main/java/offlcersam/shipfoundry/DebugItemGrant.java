package offlcersam.shipfoundry;

import com.sector.bridge.SSFMLLogger;

import game.Player;

/*
    Grants ships to a debug character's cargo hold on load, driven off the JSON registry:
        - character name matching config's debugItemGrantCharacterName (case-insensitive, "STEST" by
          default) grants every successfully registered ShipFoundry JSON ship.
        - any other character name that matches a ships/<name>/ folder name (case-insensitive) grants only
          that folder's registered ships.
        - any other character name does nothing (duh).
*/
public final class DebugItemGrant {

    private DebugItemGrant() {
    }

    public static void grantShipsToDebugCharacter() {
        if (!ShipFoundryConfig.debugItemGrantEnabled()) {
            return;
        }

        String characterName = Player.currentName;

        if (characterName == null) {
            return;
        }

        int[] databaseIds;
        String grantLabel;

        if (ShipFoundryConfig.debugItemGrantCharacterName().equalsIgnoreCase(characterName)) {
            databaseIds = ShipRegistrar.getShipDatabaseIDs();
            grantLabel = "all registered ships";
        } else {
            databaseIds = ShipUtilLoader.getShipIdsForModName(characterName)
                    .stream()
                    .mapToInt(ShipRegistrar::toDatabaseID)
                    .toArray();
            grantLabel = "ship pack \"" + characterName + "\"";

            // Not a debug character - no ship pack folder matches this character's name.
            if (databaseIds.length == 0) {
                return;
            }
        }

        if (Player.ship == null || Player.ship.cargo == null) {
            SSFMLLogger.log("[ShipFoundry] DebugItemGrant: player ship/cargo not loaded; cannot grant ships.");
            return;
        }

        if (databaseIds.length == 0) {
            SSFMLLogger.log("[ShipFoundry] No registered ships available to grant.");
            return;
        }

        for (int databaseId : databaseIds) {
            Player.ship.cargo.add(databaseId, 1);
        }

        SSFMLLogger.log(
                "[ShipFoundry] Granted " + databaseIds.length
                        + " ship(s) from " + grantLabel
                        + " to \"" + characterName + "\"'s cargo hold successfully."
        );
    }
}