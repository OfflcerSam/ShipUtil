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
                             Registration registration) {

    public record TurretSlot(double angle, double distance) {
    }

    /**
     * Optional registration settings for a ship.
     * Sections that are not present in the JSON are simply not registered.
     */
    public record Registration(boolean market, List<NpcSpawn> npc, List<BossSpawn> boss, PoliceSpawn police) {

        // Returns an empty registration configuration.
        public static Registration empty() {
            return new Registration(false, List.of(), List.of(), null);
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
                registration
        );
    }

    /**
     * Parses optional registration settings.
     * If the "registration" object does not exist,
     * the ship is only registered as a usable ship and will not be added to markets or spawn pools.
     */
    private static Registration parseRegistration(JsonValue root) {
        JsonValue registrationValue;

        try {
            registrationValue = root.get("registration");
        } catch (JsonValue.JsonException ignored) {
            return Registration.empty();
        }

        boolean market = registrationValue.getBoolean("market", false);

        List<NpcSpawn> npc = new ArrayList<>();

        try {
            for (JsonValue entry : registrationValue.getArray("npc")) {
                int tier = entry.get("tier").asInt();
                int weight = entry.getInt("weight", 1);

                if (weight < 1) {
                    throw new JsonValue.JsonException("npc weight must be at least 1");
                }

                npc.add(new NpcSpawn(tier, weight));
            }
        } catch (JsonValue.JsonException ignored) {
            // NPC registration is optional.
        }

        List<BossSpawn> boss = new ArrayList<>();

        try {
            for (JsonValue entry : registrationValue.getArray("boss")) {
                int sectorTier = entry.get("sectorTier").asInt();
                int weight = entry.getInt("weight", 1);

                if (weight < 1) {
                    throw new JsonValue.JsonException("boss weight must be at least 1");
                }

                boss.add(new BossSpawn(sectorTier, weight));
            }
        } catch (JsonValue.JsonException ignored) {
            // Boss registration is optional.
        }

        PoliceSpawn police = null;

        try {
            JsonValue policeValue = registrationValue.get("police");

            int weight = policeValue.getInt("weight", 1);

            if (weight < 1) {
                throw new JsonValue.JsonException("police weight must be at least 1");
            }

            police = new PoliceSpawn(weight);
        } catch (JsonValue.JsonException ignored) {
            // Police registration is optional.
        }

        return new Registration(
                market,
                List.copyOf(npc),
                List.copyOf(boss),
                police
        );
    }

}
