package offlcersam.shipfoundry;

import com.sector.bridge.SSFMLLogger;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Patches a ship's "lootTable" entries directly into the game's own data-driven loot_tables.sc, the same
 * way WeaponFoundry's LootTablePatcher does for weapons/ammo.
 * <p>
 * loot_tables.sc is read by the game from disk at "<gameDir>/resources/data/loot_tables.sc" first, only
 * falling back to the copy embedded in Sector_Space.jar if that disk file is missing. Since that disk file
 * persists across launches and normal (non-dev) play never refreshes it from the jar, this patcher wraps
 * every row it inserts in "// SHIPFOUNDRY:BEGIN/END" marker comments and strips any previously-inserted
 * block before re-inserting fresh ones each boot, so repeated launches don't accumulate duplicates and
 * removed/renamed ships don't leave stale rows behind.
 */
public final class ShipLootTablePatcher {

    private static final String LOOT_TABLE_FILE = "loot_tables.sc";

    /** "Tier 0 Rogue Drones Loot" is table 200, add the tier (0-4) to get the exact table. */
    private static final int ROGUE_DRONE_TABLE_BASE = 200;

    private static final String MARKER_BEGIN_PREFIX = "// SHIPFOUNDRY:BEGIN";
    private static final String MARKER_END = "// SHIPFOUNDRY:END";

    // Matches a table header line like "{ 200,  Tier 0 Rogue Drones Loot" and captures the index.
    private static final Pattern HEADER_PATTERN = Pattern.compile("^\\{\\s*(-?\\d+)\\s*,");

    private ShipLootTablePatcher() {
    }

    /** One row to insert into a specific table index, tagged with the source ship id for the marker comment. */
    private record Insertion(int sourceId, String row) {
    }

    public static void patch() {
        patchFile(LOOT_TABLE_FILE, buildInsertions());
    }

    /** table index -> rows to insert, built from every loaded ship's lootTable entries. */
    private static Map<Integer, List<Insertion>> buildInsertions() {
        Map<Integer, List<Insertion>> insertions = new LinkedHashMap<>();

        for (ShipDefinition def : ShipRegistrar.getLoadedShips()) {
            if (def.lootTable().isEmpty()) {
                continue;
            }

            for (ShipLootEntry entry : def.lootTable()) {
                int tableIndex = ROGUE_DRONE_TABLE_BASE + entry.tier();
                String row = formatRow(def.name(), entry);
                insertions.computeIfAbsent(tableIndex, k -> new ArrayList<>()).add(new Insertion(def.id(), row));
            }
        }

        return insertions;
    }

    /**
     * Formats one data row in the "Name, minQty, maxQty, probability" form loot_tables.sc uses.
     * minQty/maxQty are always 1/1 - a ship drop here is always a single-unit item.
     * <p>
     * Uses the ship's name rather than a numeric id: the file's own header comment documents a numeric
     * column as a "Short ID" that TableReader.findById() reconstructs by multiplying by 10000
     * (ITEM_ID_RANGE) - that only round-trips correctly for item ids that are themselves a clean multiple
     * of 10000 (true for most weapon/gear ids, e.g. 302370000). Ship ids are
     * ItemTypeConstantsInterface.SHIP * 10000 + baseId (see ShipRegistrar.toDatabaseID), which is NOT a
     * multiple of 10000 in general, so the numeric short-id path would silently resolve to the wrong item.
     * Name-based lookup (TableReader.findByName) has no such restriction, so it's used here - matching
     * WeaponFoundry's LootTablePatcher, which uses the same name-based approach for the same reason.
     * <p>
     * weight -> probability mapping: weight is treated directly as a percent chance (1-100), divided down
     * to the 0.0-1.0 range the game's RandomizedItemTable expects - same interpretation as WeaponFoundry's
     * LootEntry uses for weapons/ammo.
     */
    private static String formatRow(String name, ShipLootEntry entry) {
        double probability = Math.min(1.0, entry.weight() / 100.0);
        return name + ", 1, 1, " + String.format(Locale.ROOT, "%.4f", probability);
    }

    private static void patchFile(String filename, Map<Integer, List<Insertion>> insertions) {
        Path diskPath = FabricLoader.getInstance().getGameDir()
                .resolve("resources").resolve("data").resolve(filename);

        if (!Files.isRegularFile(diskPath) && !extractBaseFile(filename, diskPath)) {
            SSFMLLogger.log(
                    "[ShipFoundry] Could not find or extract " + filename
                            + " - skipping lootTable patch for it."
            );
            return;
        }

        String original;

        try {
            original = Files.readString(diskPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            SSFMLLogger.log("[ShipFoundry] Could not read " + diskPath + ": " + e);
            return;
        }

        String lineSeparator = original.contains("\r\n") ? "\r\n" : "\n";
        List<String> lines = new ArrayList<>(List.of(original.split("\r\n|\n", -1)));

        // A trailing empty element from a final newline shouldn't get its own line on write-back.
        boolean trailingNewline = !lines.isEmpty() && lines.get(lines.size() - 1).isEmpty();
        if (trailingNewline) {
            lines.remove(lines.size() - 1);
        }

        List<String> stripped = stripPreviousMarkers(lines);
        List<String> patched = insertRows(stripped, insertions);

        String result = String.join(lineSeparator, patched) + (trailingNewline ? lineSeparator : "");

        if (result.equals(original)) {
            return;
        }

        try {
            Files.writeString(diskPath, result, StandardCharsets.UTF_8);
        } catch (IOException e) {
            SSFMLLogger.log("[ShipFoundry] Could not write " + diskPath + ": " + e);
            return;
        }

        int rowCount = insertions.values().stream().mapToInt(List::size).sum();
        SSFMLLogger.log("[ShipFoundry] Patched " + rowCount + " lootTable row(s) into " + filename);
    }

    /** Copies the game's own embedded copy of the file to disk so we have a base to patch. */
    private static boolean extractBaseFile(String filename, Path diskPath) {
        try (InputStream in = game.Main.class.getResourceAsStream("/data/" + filename)) {
            if (in == null) {
                return false;
            }

            Files.createDirectories(diskPath.getParent());
            Files.copy(in, diskPath);
            return true;
        } catch (IOException e) {
            SSFMLLogger.log("[ShipFoundry] Could not extract base " + filename + ": " + e);
            return false;
        }
    }

    /** Removes any "// SHIPFOUNDRY:BEGIN" . . . "// SHIPFOUNDRY:END" span from a previous patch pass. */
    private static List<String> stripPreviousMarkers(List<String> lines) {
        List<String> stripped = new ArrayList<>(lines.size());
        boolean inBlock = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith(MARKER_BEGIN_PREFIX)) {
                inBlock = true;
                continue;
            }

            if (trimmed.equals(MARKER_END)) {
                inBlock = false;
                continue;
            }

            if (!inBlock) {
                stripped.add(line);
            }
        }

        return stripped;
    }

    /** Walks the file tracking which table index is currently open, inserting rows just before its closing "}". */
    private static List<String> insertRows(List<String> lines, Map<Integer, List<Insertion>> insertions) {
        if (insertions.isEmpty()) {
            return lines;
        }

        List<String> output = new ArrayList<>(lines.size());
        int currentTable = Integer.MIN_VALUE;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.equals("}")) {
                List<Insertion> toInsert = insertions.remove(currentTable);

                if (toInsert != null) {
                    for (Insertion insertion : toInsert) {
                        output.add(MARKER_BEGIN_PREFIX + " " + insertion.sourceId());
                        output.add(insertion.row());
                        output.add(MARKER_END);
                    }
                }

                currentTable = Integer.MIN_VALUE;
                output.add(line);
                continue;
            }

            Matcher header = HEADER_PATTERN.matcher(trimmed);
            if (header.find()) {
                currentTable = Integer.parseInt(header.group(1));
            }

            output.add(line);
        }

        for (Integer missingTable : insertions.keySet()) {
            SSFMLLogger.log(
                    "[ShipFoundry] Table index " + missingTable
                            + " not found - " + insertions.get(missingTable).size()
                            + " lootTable row(s) could not be placed."
            );
        }

        return output;
    }
}