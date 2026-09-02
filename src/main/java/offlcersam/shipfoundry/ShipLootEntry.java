package offlcersam.shipfoundry;

import offlcersam.shipfoundry.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * One entry in a ship's optional "lootTable" - makes this ship's item droppable from rogue drones of a
 * given tier, on top of whatever generic loot they'd already drop.
 * <p>
 * Currently the only ship-drop pool that exists in the game's data-driven loot_tables.sc is the rogue
 * drone one ("Tier 0-4 Rogue Drones Loot", table indices 200-204) - see ShipLootTablePatcher. There's no
 * table for shard/blob/wraith/ancient drops; those are all hardcoded item spawns in
 * _database.Unique_NPC_Drops instead, so this only covers rogue drones for now.
 * <p>
 * weight is treated directly as a percent chance (1-100), matching WeaponFoundry's LootEntry - divided
 * down to the 0.0-1.0 probability the table format expects.
 */
public record ShipLootEntry(int tier, int weight) {

    /** Rogue drone loot tables only go up to tier 4 (table 204) in this game version. */
    private static final int MAX_TIER = 4;

    /**
     * Parses the optional "lootTable" array off the given root object.
     * Returns an empty list if the field is absent, meaning the ship isn't added to any rogue drone drop pool.
     */
    public static List<ShipLootEntry> parseList(JsonValue root) {
        List<ShipLootEntry> entries = new ArrayList<>();

        JsonValue lootTableValue = root.getOrNull("lootTable");
        if (lootTableValue == null || lootTableValue.isNull()) {
            return entries;
        }

        for (JsonValue entry : lootTableValue.asArray()) {
            int tier = entry.get("tier").asInt();
            int weight = entry.getInt("weight", 1);

            if (tier < 0 || tier > MAX_TIER) {
                throw new JsonValue.JsonException("lootTable tier must be between 0 and " + MAX_TIER);
            }

            if (weight < 1) {
                throw new JsonValue.JsonException("lootTable weight must be at least 1");
            }

            entries.add(new ShipLootEntry(tier, weight));
        }

        return entries;
    }
}