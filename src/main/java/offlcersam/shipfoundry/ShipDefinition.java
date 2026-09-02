package offlcersam.shipfoundry;

import offlcersam.shipfoundry.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Required ShipDefinitions for a ship.
 */
public record ShipDefinition(int id, int icon, String color, String name, String description, int tier, String rarity,
                             int renderIndex, int engineDisplacement, float hull, float cargo,
                             List<TurretSlot> weaponLayout, String vanillaWeaponLayout, int energySlots, int armorSlots, int shieldSlots,
                             int deviceSlots, int moduleSlots, int engineSlots,
                             Registration registration, Recipe recipe, List<ShipStat> shipStats, List<Integer> builtInDevices,
                             List<ShipLootEntry> lootTable, boolean isStation, boolean isPlatform) {

    public record TurretSlot(double angle, double distance) {
    }

    /**
     * Optional registration settings for a ship.
     * Sections that are not present in the JSON are simply not registered.
     */
    public record Registration(MarketOptions market, List<NpcSpawn> npc, List<BossSpawn> boss, PoliceSpawn police,
                               List<UniqueLootDrop> uniqueLoot, RogueDroneSpawn rogueDrone,
                               List<BlobSpawn> blob, BlobBossSpawn blobBoss, List<ShardSpawn> shard,
                               List<ShardBossSpawn> shardBoss, List<BroodlingSpawn> broodling, LurkerSpawn lurker) {

        // Returns an empty registration configuration.
        public static Registration empty() {
            return new Registration(
                    null, List.of(), List.of(), null, List.of(), null,
                    List.of(), null, List.of(), List.of(), List.of(), null
            );
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
     * Registers this ship as a rogue drone.
     * <p>
     * Unlike npc/boss/police, vanilla's own rogue drone spawn code (SpawnNPC.configRogueDrone) equips gear by
     * switching on the literal vanilla ship id (181-189) rather than by tier, so there's no generic per-tier
     * gear table to fall back on for a custom id - anything not in that switch just gets weak default gear.
     * These fields are vanilla's own per-id preset values (weapon/energy item ids, level range, credit range)
     * so a custom rogue drone can be given a preset appropriate to its tier instead of that default.
     * <p>
     * tier picks which roll bucket (0-4, where 4 covers tier 4 or higher) this ship competes in - see
     * NPCRegistrar's rogue drone bucket sizes for the vanilla ticket counts per bucket.
     */
    public record RogueDroneSpawn(int tier, int weight, int weaponLaser, int weaponBay, int energyFullID,
                                  int levelMin, int levelMax, long creditMin, long creditMax) {
    }

    /**
     * Registers this ship as a candidate blob, competing against vanilla's own fixed blob id for that tier
     * (190 + tier) the same weighted way npc/boss do. Blob gear is applied procedurally by tier rather than
     * by literal ship id, so a substituted custom ship gets appropriate gear automatically - no separate
     * gear preset is needed here, unlike rogueDrone.
     */
    public record BlobSpawn(int tier, int weight) {
    }

    /**
     * Registers this ship as a candidate "blob boss" (vanilla id 196), a single fixed encounter distinct
     * from the tiered blob ladder above.
     */
    public record BlobBossSpawn(int weight) {
    }

    /**
     * Registers this ship as a candidate shard mob, competing against vanilla's own fixed shard id for
     * that nebula type (171 + type). Same "gear is tier-parametrized, not id-dispatched" situation as blob.
     */
    public record ShardSpawn(int type, int weight) {
    }

    /**
     * Registers this ship as a candidate shard boss. darker picks which of vanilla's two shard boss ids
     * (197 normal, 198 darker) this entry competes against.
     */
    public record ShardBossSpawn(boolean darker, int weight) {
    }

    /**
     * Registers this ship as a candidate broodling-family spawn. type selects which of vanilla's four fixed
     * broodling ids this entry competes against: "broodling" (223), "queen" (226), "darkQueen" (227), or
     * "regular" (224, the plain brood variant).
     */
    public record BroodlingSpawn(String type, int weight) {
    }

    /**
     * Registers this ship as a candidate lurker-family spawn. One shared pool covers every lurker
     * sub-variant (normal, lurkerling, and lurker carrier) and every spawn method that can produce one,
     * the same way vanilla's own police pool covers Cruiser/Carrier/Corvette through one spawnPolice(...).
     */
    public record LurkerSpawn(int weight) {
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

        JsonValue weaponLayoutValue = root.get("weaponLayout");
        List<TurretSlot> layout = new ArrayList<>();
        String vanillaWeaponLayout = null;

        if (weaponLayoutValue.type() == JsonValue.Type.STRING) {
            vanillaWeaponLayout = weaponLayoutValue.asString();
        } else {
            for (JsonValue slot : weaponLayoutValue.asArray()) {
                double angle = slot.get("angle").asDouble();
                double distance = slot.get("distance").asDouble();
                layout.add(new TurretSlot(angle, distance));
            }

            if (layout.isEmpty()) {
                throw new JsonValue.JsonException("weaponLayout must have at least one slot");
            }
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
        List<ShipLootEntry> lootTable = ShipLootEntry.parseList(root);

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
                vanillaWeaponLayout,
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
                lootTable,
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

        MarketOptions market = MarketOptions.parse(registrationValue);

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

        RogueDroneSpawn rogueDrone = null;

        JsonValue rogueDroneValue = registrationValue.getOrNull("rogueDrone");
        if (rogueDroneValue != null && !rogueDroneValue.isNull()) {
            int tier = rogueDroneValue.get("tier").asInt();
            int weight = rogueDroneValue.getInt("weight", 1);
            int weaponLaser = rogueDroneValue.get("weaponLaser").asInt();
            int weaponBay = rogueDroneValue.get("weaponBay").asInt();
            int energyFullID = rogueDroneValue.get("energyFullID").asInt();
            int levelMin = rogueDroneValue.get("levelMin").asInt();
            int levelMax = rogueDroneValue.get("levelMax").asInt();
            long creditMin = rogueDroneValue.get("creditMin").asLong();
            long creditMax = rogueDroneValue.get("creditMax").asLong();

            if (tier < 0 || tier > 4) {
                throw new JsonValue.JsonException("rogueDrone tier must be between 0 and 4");
            }

            if (weight < 1) {
                throw new JsonValue.JsonException("rogueDrone weight must be at least 1");
            }

            if (levelMin > levelMax) {
                throw new JsonValue.JsonException("rogueDrone levelMin must not be greater than levelMax");
            }

            if (creditMin > creditMax) {
                throw new JsonValue.JsonException("rogueDrone creditMin must not be greater than creditMax");
            }

            rogueDrone = new RogueDroneSpawn(
                    tier, weight, weaponLaser, weaponBay, energyFullID, levelMin, levelMax, creditMin, creditMax
            );
        }

        List<BlobSpawn> blob = new ArrayList<>();

        JsonValue blobValue = registrationValue.getOrNull("blob");
        if (blobValue != null && !blobValue.isNull()) {
            for (JsonValue entry : blobValue.asArray()) {
                int tier = entry.get("tier").asInt();
                int weight = entry.getInt("weight", 1);

                if (tier < 0 || tier > 5) {
                    throw new JsonValue.JsonException("blob tier must be between 0 and 5");
                }
                if (weight < 1) {
                    throw new JsonValue.JsonException("blob weight must be at least 1");
                }
                blob.add(new BlobSpawn(tier, weight));
            }
        }

        BlobBossSpawn blobBoss = null;

        JsonValue blobBossValue = registrationValue.getOrNull("blobBoss");
        if (blobBossValue != null && !blobBossValue.isNull()) {
            int weight = blobBossValue.getInt("weight", 1);

            if (weight < 1) {
                throw new JsonValue.JsonException("blobBoss weight must be at least 1");
            }
            blobBoss = new BlobBossSpawn(weight);
        }

        List<ShardSpawn> shard = new ArrayList<>();

        JsonValue shardValue = registrationValue.getOrNull("shard");
        if (shardValue != null && !shardValue.isNull()) {
            for (JsonValue entry : shardValue.asArray()) {
                int type = entry.get("type").asInt();
                int weight = entry.getInt("weight", 1);

                if (type < 0 || type > 5) {
                    throw new JsonValue.JsonException("shard type must be between 0 and 5");
                }
                if (weight < 1) {
                    throw new JsonValue.JsonException("shard weight must be at least 1");
                }
                shard.add(new ShardSpawn(type, weight));
            }
        }

        List<ShardBossSpawn> shardBoss = new ArrayList<>();

        JsonValue shardBossValue = registrationValue.getOrNull("shardBoss");
        if (shardBossValue != null && !shardBossValue.isNull()) {
            for (JsonValue entry : shardBossValue.asArray()) {
                boolean darker = entry.getBoolean("darker", false);
                int weight = entry.getInt("weight", 1);

                if (weight < 1) {
                    throw new JsonValue.JsonException("shardBoss weight must be at least 1");
                }
                shardBoss.add(new ShardBossSpawn(darker, weight));
            }
        }

        List<BroodlingSpawn> broodling = new ArrayList<>();

        JsonValue broodlingValue = registrationValue.getOrNull("broodling");
        if (broodlingValue != null && !broodlingValue.isNull()) {
            for (JsonValue entry : broodlingValue.asArray()) {
                String type = entry.get("type").asString();
                int weight = entry.getInt("weight", 1);

                if (!type.equals("broodling") && !type.equals("queen") && !type.equals("darkQueen") && !type.equals("regular")) {
                    throw new JsonValue.JsonException("broodling type must be one of: broodling, queen, darkQueen, regular");
                }
                if (weight < 1) {
                    throw new JsonValue.JsonException("broodling weight must be at least 1");
                }
                broodling.add(new BroodlingSpawn(type, weight));
            }
        }

        LurkerSpawn lurker = null;

        JsonValue lurkerValue = registrationValue.getOrNull("lurker");
        if (lurkerValue != null && !lurkerValue.isNull()) {
            int weight = lurkerValue.getInt("weight", 1);

            if (weight < 1) {
                throw new JsonValue.JsonException("lurker weight must be at least 1");
            }
            lurker = new LurkerSpawn(weight);
        }

        return new Registration(
                market,
                List.copyOf(npc),
                List.copyOf(boss),
                police,
                List.copyOf(uniqueLoot),
                rogueDrone,
                List.copyOf(blob),
                blobBoss,
                List.copyOf(shard),
                List.copyOf(shardBoss),
                List.copyOf(broodling),
                lurker
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