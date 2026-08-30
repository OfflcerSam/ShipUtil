package offlcersam.shipfoundry;

import game.weapons.WeaponSlotLayoutList;
import game.weapons.WeaponTurretPlacement;
import illuminatus.core.graphics.Color;
import items.TypeTag;
import items.lists.ShipList;
import mods.ModLogger;
import net.fabricmc.loader.api.FabricLoader;
import offlcersam.shipfoundry.json.JsonParser;
import offlcersam.shipfoundry.json.JsonValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Scans <gameDir>/ships/<modName>/*.json to register each one as a ship via ShipList.write()
 */
public final class ShipUtilLoader {
    private static final String SHIPS_FOLDER_NAME = "ships";

    // Track which shipID came from which mod folder, two same IDs will get an error.
    // For now there is now error detection for existing mods.
    private static final Map<Integer, String> CLAIMED_IDS = new HashMap<>();

    private ShipUtilLoader() { }

    public static void load() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path shipsRoot = gameDir.resolve(SHIPS_FOLDER_NAME);

        if (!Files.isDirectory(shipsRoot)) {
            ModLogger.log("[ShipFoundry] No \"" + SHIPS_FOLDER_NAME + "\" folder found at " + shipsRoot + " - nothing to load.");
            return;
        }

        int totalLoaded = 0;
        try (Stream<Path> modFolders = Files.list(shipsRoot)) {
            for (Path modFolder : modFolders.filter(Files::isDirectory).toList()) {
                totalLoaded += loadModFolder(modFolder);
            }
        } catch (IOException e) {
            ModLogger.log("[ShipFoundry] Failed to list " + shipsRoot + ": " + e);
            return;
        }

        ShipList.loadShipStatsFromItems(_database.ItemDatabase.itemDataFile);
        ModLogger.log("[ShipFoundry] Loaded " + totalLoaded + " ship(s) total from " + shipsRoot);
    }

    private static int loadModFolder(Path modFolder) {
        String modName = modFolder.getFileName().toString();
        int loaded = 0;

        try (Stream<Path> jsonFiles = Files.list(modFolder)) {
            for (Path file : jsonFiles.filter(p -> p.toString().endsWith(".json")).toList()) {
                if (loadShipFile(modName, file)) {
                    loaded++;
                }
            }
        } catch (IOException e) {
            ModLogger.log("[ShipFoundry] Failed to list " + modFolder + ": " + e);
        }

        ModLogger.log("[ShipFoundry] Loaded " + loaded + " ship(s) from mod folder \"" + modName + "\"");
        return loaded;
    }

    private static boolean loadShipFile(String modName, Path file) {
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
            ModLogger.log("[ShipFoundry] Invalid ship JSON in " + file + " (mod \"" + modName + "\"): " + e.getMessage());
            return false;
        }

        String owner = CLAIMED_IDS.get(def.id());
        if (owner != null) {
            ModLogger.log("[ShipFoundry] Skipping " + file + ": ship id " + def.id()
                    + " is already claimed by mod \"" + owner + "\" - ids must be unique across all ships/ folders.");
            return false;
        }

        try {
            registerShip(def);
        } catch (Exception e) {
            ModLogger.log("[ShipFoundry] Failed to register ship id " + def.id() + " from " + file + ": " + e);
            return false;
        }

        CLAIMED_IDS.put(def.id(), modName);
        return true;
    }

    private static void registerShip(ShipDefinition def) {
        WeaponTurretPlacement placement = new WeaponTurretPlacement();
        for (ShipDefinition.TurretSlot slot : def.weaponLayout()) {
            placement.addSlot(slot.angle(), slot.distance());
        }
        int weaponLayoutIndex = WeaponSlotLayoutList.layouts.add(placement);

        Color color = resolveConstant(Color.class, def.color(), "color");
        TypeTag rarity = resolveConstant(TypeTag.class, def.rarity(), "rarity");

        ShipList.write(
                def.id(),
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

        ModLogger.log("[ShipFoundry] Registered ship \"" + def.name() + "\" (id " + def.id() + ")");
    }

    private static <T> T resolveConstant(Class<T> type, String name, String fieldLabel) {
        try {
            return type.cast(type.getField(name.toUpperCase()).get(null));
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unknown " + fieldLabel + " \"" + name + "\" - check the exact constant name on " + type.getName());
        }
    }
}
