package offlcersam.shipfoundry;

import items.lists.ShipList;
import mods.ModLogger;
import net.fabricmc.loader.api.FabricLoader;
import offlcersam.shipfoundry.json.JsonParser;
import offlcersam.shipfoundry.json.JsonValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Scans <gameDir>/ships/<modName>/*.json to register each one as a ship.
 */
public final class ShipUtilLoader {
    private static final String SHIPS_FOLDER_NAME = "ships";

    // Track which shipID came from which mod folder, two same IDs will get an error.
    // For now there is now error detection for existing mods.
    private static final Map<Integer, String> CLAIMED_IDS = new HashMap<>();

    private static boolean loaded;

    private ShipUtilLoader() {
    }

    public static void load() {
        if (loaded) {
            return;
        }
        loaded = true;

        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path shipsRoot = gameDir.resolve(SHIPS_FOLDER_NAME);

        if (!Files.isDirectory(shipsRoot)) {
            ModLogger.log(
                    "[ShipFoundry] No \"" + SHIPS_FOLDER_NAME
                            + "\" folder found at " + shipsRoot
                            + " - nothing to load."
            );
            return;
        }

        List<ShipDefinition> loadedShips = new ArrayList<>();
        int totalLoaded = 0;

        try (Stream<Path> modFolders = Files.list(shipsRoot)) {
            for (Path modFolder : modFolders.filter(Files::isDirectory).toList()) {
                totalLoaded += loadModFolder(modFolder, loadedShips);
            }
        } catch (IOException e) {
            ModLogger.log("[ShipFoundry] Failed to list " + shipsRoot + ": " + e);
            return;
        }

        /*
         * This must happen after every custom ship has been written.
         * NPC Spawning and Market Registration depends on the ship exiting, obviously.
         */
        if (totalLoaded > 0) {
            ShipList.loadShipStatsFromItems(_database.ItemDatabase.itemDataFile);

            /*
             * Register NPC, boss and police spawn settings after all ships
             * have been written and their stats have been loaded.
             */
            for (ShipDefinition ship : loadedShips) {
                ShipRegistrar.registerSpawnSettings(ship);
            }
        }

        ModLogger.log(
                "[ShipFoundry] Loaded " + totalLoaded
                        + " ship(s) total from " + shipsRoot
        );
    }

    private static int loadModFolder(Path modFolder, List<ShipDefinition> loadedShips) {
        String modName = modFolder.getFileName().toString();
        int loaded = 0;

        try (Stream<Path> jsonFiles = Files.list(modFolder)) {
            for (Path file : jsonFiles
                    .filter(p -> p.toString().toLowerCase().endsWith(".json"))
                    .toList()) {

                if (loadShipFile(modName, file, loadedShips)) {
                    loaded++;
                }
            }
        } catch (IOException e) {
            ModLogger.log("[ShipFoundry] Failed to list " + modFolder + ": " + e);
        }

        ModLogger.log(
                "[ShipFoundry] Loaded " + loaded
                        + " ship(s) from ship pack folder \"" + modName + "\""
        );

        return loaded;
    }

    private static boolean loadShipFile(String modName, Path file, List<ShipDefinition> loadedShips) {
        String text;

        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            ModLogger.log("[ShipFoundry] Could not read " + file + ": " + e);
            return false;
        }

        ShipDefinition def;

        try {
            JsonValue root = JsonParser.parse(text);
            def = ShipDefinition.fromJson(root);
        } catch (JsonValue.JsonException e) {
            ModLogger.log(
                    "[ShipFoundry] Invalid ship JSON in " + file
                            + " (mod \"" + modName + "\"): "
                            + e.getMessage()
            );
            return false;
        }

        String owner = CLAIMED_IDS.get(def.id());

        if (owner != null) {
            ModLogger.log(
                    "[ShipFoundry] Skipping " + file
                            + ": ship id " + def.id()
                            + " is already claimed by mod \"" + owner
                            + "\" - ids must be unique across all ships/ folders."
            );
            return false;
        }

        try {
            ShipRegistrar.registerShip(def);
        } catch (Exception e) {
            ModLogger.log(
                    "[ShipFoundry] Failed to register ship id "
                            + def.id() + " from " + file + ": " + e
            );
            return false;
        }

        ShipGraphicsLoader.loadShipTexture(file.getParent(), def);

        CLAIMED_IDS.put(def.id(), modName);
        loadedShips.add(def);
        return true;
    }
}