package offlcersam.shipfoundry;

import offlcersam.shipfoundry.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Required ShipDefinitions for a ship.
 */
public record ShipDefinition(int id, int icon, String color, String name, String description, int tier, String rarity,
                             int renderIndex, int engineDisplacement, float hull, float cargo,
                             List<TurretSlot> weaponLayout, int energySlots, int armorSlots, int shieldSlots,
                             int deviceSlots, int moduleSlots, int engineSlots,
                             Registration registration, Recipe recipe, List<ShipStat> shipStats, List<Integer> builtInDevices,
                             boolean isStation, boolean isPlatform) {

    public record TurretSlot(double angle, double distance) {
    }

    /**
     * Optional registration settings for a ship.
     * Sections that are not present in the JSON are simply not registered.
     */
    public record Registration(boolean market, List<NpcSpawn> npc, List<BossSpawn> boss, PoliceSpawn police,
                               List<UniqueLootDrop> uniqueLoot) {

        // Returns an empty registration configuration.
        public static Registration empty() {
            return new Registration(false, List.of(), List.of(), null, List.of());
        }
    }

    /**
     * Registers this ship as a normal tiered NPC.
     */
    public record NpcSpawn(int tier, int weight) {
    }

    /**
     * Registers this ship as a boss for a sector tier.
     */
    public record BossSpawn(int sectorTier, int weight) {
    }

    /**
     * Registers this ship as a police spawn.
     */
    public record PoliceSpawn(int weight) {
    }

    /**
     * One extra item this ship can drop when destroyed as an NPC/boss, on top of whatever generic loot it would already drop.
     */
    public record UniqueLootDrop(int id, int amount, int chance) {
    }

    /**
     * Optional crafting recipe for this ship.
     * Mirrors crafting.CraftingTable#addRecipe(String, int, int, int, int, int, int, int, int, int),
     * which always takes exactly a blueprint slot plus 3 fixed ingredient slots.
     */
    public record Recipe(String label, int blueprintId, int blueprintAmount, List<Ingredient> ingredients) {
    }

    /**
     * One ingredient slot in a Recipe - an item id and the amount of it consumed.
     */
    public record Ingredient(int id, int amount) {
    }

    /**
     * One or more entries in the optional "shipStats" section - a permanent bonus applied to ship.
     */
    public record ShipStat(String stat, Float flat, Float percent) {
    }

    /**
     * Parses and validates one ship JSON object.
     * Throws JsonValue.JsonException with a specific field name on any missing/malformed required field.
     */
    public static ShipDefinition fromJson(JsonValue root) {
        int id = root.get("id").asInt();
        int icon = root.get("icon").asInt();
        String color = root.get("color").asString();
        String name = root.get("name").asString();
        String description = root.getString("description", "");
        int tier = root.get("tier").asInt();
        String rarity = root.get("rarity").asString();
        int renderIndex = root.get("renderIndex").asInt();
        int engineDisplacement = root.getInt("engineDisplacement", 0);
        float hull = root.get("hull").asFloat();
        float cargo = root.get("cargo").asFloat();

        List<TurretSlot> layout = new ArrayList<>();
        for (JsonValue slot : root.getArray("weaponLayout")) {
            double angle = slot.get("angle").asDouble();
            double distance = slot.get("distance").asDouble();
            layout.add(new TurretSlot(angle, distance));
        }

        if (layout.isEmpty()) {
            throw new JsonValue.JsonException("weaponLayout must have at least one slot");
        }

        JsonValue slots = root.get("slots");
        int energySlots = slots.getInt("energy", 0);
        int armorSlots = slots.getInt("armor", 0);
        int shieldSlots = slots.getInt("shield", 0);
        int deviceSlots = slots.getInt("device", 0);
        int moduleSlots = slots.getInt("module", 0);
        int engineSlots = slots.getInt("engine", 0);

        Registration registration = parseRegistration(root);
        Recipe recipe = parseRecipe(root);
        List<ShipStat> shipStats = parseShipStats(root);
        List<Integer> builtInDevices = parseBuiltInDevices(root);

        boolean isStation = root.getBoolean("isStation", false);
        boolean isPlatform = root.getBoolean("isPlatform", false);

        if (isStation && isPlatform) {
            throw new JsonValue.JsonException("a ship cannot have both \"isStation\" and \"isPlatform\" set to true");
        }

        return new ShipDefinition(
                id,
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
                layout,
                energySlots,
                armorSlots,
                shieldSlots,
                deviceSlots,
                moduleSlots,
                engineSlots,
                registration,
                recipe,
                shipStats,
                builtInDevices,
                isStation,
                isPlatform
        );
    }

    /**
     * Parses optional registration settings.
     * If the "registration" object does not exist,
     * the ship is only registered as a usable ship and will not be added to markets or spawn pools.
     */
    private static Registration parseRegistration(JsonValue root) {
        JsonValue registrationValue = root.getOrNull("registration");

        if (registrationValue == null || registrationValue.isNull()) {
            return Registration.empty();
        }

        boolean market = registrationValue.getBoolean("market", false);

        List<NpcSpawn> npc = new ArrayList<>();

        JsonValue npcValue = registrationValue.getOrNull("npc");
        if (npcValue != null && !npcValue.isNull()) {
            for (JsonValue entry : npcValue.asArray()) {
                int tier = entry.get("tier").asInt();
                int weight = entry.getInt("weight", 1);

                if (weight < 1) {
                    throw new JsonValue.JsonException("npc weight must be at least 1");
                }
                npc.add(new NpcSpawn(tier, weight));
            }
        }

        List<BossSpawn> boss = new ArrayList<>();

        JsonValue bossValue = registrationValue.getOrNull("boss");
        if (bossValue != null && !bossValue.isNull()) {
            for (JsonValue entry : bossValue.asArray()) {
                int sectorTier = entry.get("sectorTier").asInt();
                int weight = entry.getInt("weight", 1);

                if (weight < 1) {
                    throw new JsonValue.JsonException("boss weight must be at least 1");
                }
                boss.add(new BossSpawn(sectorTier, weight));
            }
        }

        PoliceSpawn police = null;

        JsonValue policeValue = registrationValue.getOrNull("police");
        if (policeValue != null && !policeValue.isNull()) {
            int weight = policeValue.getInt("weight", 1);

            if (weight < 1) {
                throw new JsonValue.JsonException("police weight must be at least 1");
            }

            police = new PoliceSpawn(weight);
        }

        List<UniqueLootDrop> uniqueLoot = new ArrayList<>();

        JsonValue uniqueLootValue = registrationValue.getOrNull("uniqueLoot");
        if (uniqueLootValue != null && !uniqueLootValue.isNull()) {
            for (JsonValue entry : uniqueLootValue.asArray()) {
                int id = entry.get("id").asInt();
                int amount = entry.getInt("amount", 1);
                int chance = entry.getInt("chance", 100);

                if (amount < 1) {
                    throw new JsonValue.JsonException("uniqueLoot amount must be at least 1");
                }

                if (chance < 1 || chance > 100) {
                    throw new JsonValue.JsonException("uniqueLoot chance must be between 1 and 100");
                }

                uniqueLoot.add(new UniqueLootDrop(id, amount, chance));
            }
        }

        return new Registration(
                market,
                List.copyOf(npc),
                List.copyOf(boss),
                police,
                List.copyOf(uniqueLoot)
        );
    }

    /**
     * Parses the optional "recipe" section.
     * If it does not exist, the ship simply isn't craftable, CraftingTableMixin skips ships with a null recipe.
     */
    private static Recipe parseRecipe(JsonValue root) {
        JsonValue recipeValue = root.getOrNull("recipe");

        if (recipeValue == null || recipeValue.isNull()) {
            return null;
        }

        String label = recipeValue.get("label").asString();
        int blueprintId = recipeValue.get("blueprintId").asInt();
        int blueprintAmount = recipeValue.getInt("blueprintAmount", 1);

        List<Ingredient> ingredients = new ArrayList<>();
        for (JsonValue entry : recipeValue.getArray("ingredients")) {
            ingredients.add(new Ingredient(entry.get("id").asInt(), entry.get("amount").asInt()));
        }

        // CraftingTable#addRecipe always takes exactly 3 ingredient slots - no vanilla overload takes fewer or more.
        if (ingredients.size() != 3) {
            throw new JsonValue.JsonException(
                    "recipe.ingredients must have exactly 3 entries, found " + ingredients.size()
            );
        }

        return new Recipe(label, blueprintId, blueprintAmount, List.copyOf(ingredients));
    }

    /**
     * Parses the optional "shipStats" section.
     */
    private static List<ShipStat> parseShipStats(JsonValue root) {
        List<ShipStat> shipStats = new ArrayList<>();

        JsonValue shipStatsValue = root.getOrNull("shipStats");
        if (shipStatsValue == null || shipStatsValue.isNull()) {
            return shipStats;
        }

        for (JsonValue entry : shipStatsValue.asArray()) {
            String stat = entry.get("stat").asString();
            Float flat = entry.has("flat") ? entry.get("flat").asFloat() : null;
            Float percent = entry.has("percent") ? entry.get("percent").asFloat() : null;

            if (flat == null && percent == null) {
                throw new JsonValue.JsonException(
                        "shipStats entry for \"" + stat + "\" needs at least one of \"flat\" or \"percent\""
                );
            }

            shipStats.add(new ShipStat(stat, flat, percent));
        }

        return shipStats;
    }

    /**
     * Parses the optional "builtInDevices" section.
     */
    private static List<Integer> parseBuiltInDevices(JsonValue root) {
        List<Integer> builtInDevices = new ArrayList<>();

        JsonValue builtInDevicesValue = root.getOrNull("builtInDevices");
        if (builtInDevicesValue == null || builtInDevicesValue.isNull()) {
            return builtInDevices;
        }

        for (JsonValue entry : builtInDevicesValue.asArray()) {
            builtInDevices.add(entry.asInt());
        }

        return builtInDevices;
    }
}