package offlcersam.shipfoundry;

import offlcersam.shipfoundry.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Example JSON:
 * {
 * "id": 350,
 * "icon": 30,
 * "color": "AZURE",
 * "name": "Arrowhead",
 * "description": "Maybe one day you could be a real arrow.",
 * "tier": 0,
 * "rarity": "UNCOMMON",
 * "renderIndex": 350,
 * "engineDisplacement": 37,
 * "hull": 300.0,
 * "cargo": 82.5,
 * "weaponLayout": [
 * { "angle": 32.5, "distance": -9.2 },
 * { "angle": -32.5, "distance": -9.2 },
 * { "angle": 0.0, "distance": -6.0 }
 * ],
 * "slots": {
 * "energy": 2,
 * "armor": 1,
 * "shield": 1,
 * "device": 0,
 * "module": 1,
 * "engine": 1
 * }
 * }
 */
public record ShipDefinition(int id, int icon, String color, String name, String description, int tier, String rarity,
                             int renderIndex, int engineDisplacement, float hull, float cargo,
                             List<TurretSlot> weaponLayout, int energySlots, int armorSlots, int shieldSlots,
                             int deviceSlots, int moduleSlots, int engineSlots) {

    public record TurretSlot(double angle, double distance) {
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

        return new ShipDefinition(id, icon, color, name, description, tier, rarity, renderIndex,
                engineDisplacement, hull, cargo, layout, energySlots, armorSlots, shieldSlots,
                deviceSlots, moduleSlots, engineSlots);
    }
}
