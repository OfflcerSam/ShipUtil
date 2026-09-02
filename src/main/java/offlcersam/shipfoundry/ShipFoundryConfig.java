package offlcersam.shipfoundry;

import com.sector.bridge.SSFMLConfig;
import com.sector.bridge.SSFMLLogger;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * ShipFoundry's config, backed by SSFML's SSFMLConfig API.
 * Lives at {@code <gameDir>/config/shipfoundry/shipfoundry.cfg} once loaded.
 * <p>
 * Options:
 * - enabledPacks: comma-separated list of ships/ subfolder names to load.
 *   Blank (the default) means load every pack found, same as before this config existed.
 * - debugLogging: when true, logs one line per ship/market listing registered instead of just per-pack and per-boot summaries.
 *   Off by default to keep normal logs quiet.
 * - debugItemGrantEnabled: whether DebugItemGrant grants ships to a character on load at all.
 *   Was previously a hardcoded constant in that class.
 * - debugItemGrantCharacterName: which character save name triggers a grant of every registered ship
 *   (as opposed to just the ships from a matching pack folder). Was previously a hardcoded "STEST" constant
 *   in that class.
 */
public final class ShipFoundryConfig {

    private static final String MOD_ID = "shipfoundry";

    private static final String KEY_ENABLED_PACKS = "enabledPacks";
    private static final String KEY_DEBUG_LOGGING = "debugLogging";
    private static final String KEY_DEBUG_ITEM_GRANT_ENABLED = "debugItemGrantEnabled";
    private static final String KEY_DEBUG_ITEM_GRANT_CHARACTER_NAME = "debugItemGrantCharacterName";

    private static final String DEFAULT_DEBUG_ITEM_GRANT_ENABLED = "true";
    private static final String DEFAULT_DEBUG_ITEM_GRANT_CHARACTER_NAME = "STEST";

    private static boolean loaded;
    private static SSFMLConfig.Config config;
    private static Set<String> enabledPacks = Set.of();

    private ShipFoundryConfig() {
    }

    public static void load() {
        if (loaded) {
            return;
        }
        loaded = true;

        List<SSFMLConfig.ConfigEntry> schema = List.of(
                new SSFMLConfig.ConfigEntry(
                        KEY_ENABLED_PACKS, "",
                        "Comma-separated ships/ subfolder names to load. Leave blank to load all of them."
                ),
                new SSFMLConfig.ConfigEntry(
                        KEY_DEBUG_LOGGING, "false",
                        "Logs one line per ship/market listing registered, instead of just summaries."
                ),
                new SSFMLConfig.ConfigEntry(
                        KEY_DEBUG_ITEM_GRANT_ENABLED, DEFAULT_DEBUG_ITEM_GRANT_ENABLED,
                        "Grants all JSON registered ships to a character with debugItemGrantCharacterName's name loads."
                ),
                new SSFMLConfig.ConfigEntry(
                        KEY_DEBUG_ITEM_GRANT_CHARACTER_NAME, DEFAULT_DEBUG_ITEM_GRANT_CHARACTER_NAME,
                        "Character save name that triggers granting every registered ship. Case-insensitive."
                )
        );

        config = SSFMLConfig.load(MOD_ID, schema);
        enabledPacks = parsePackList(config.getString(KEY_ENABLED_PACKS));
    }

    /** True if no packs were explicitly listed (meaning: load everything), or this one was named. */
    public static boolean isPackEnabled(String packName) {
        return enabledPacks.isEmpty() || enabledPacks.contains(packName.toLowerCase());
    }

    public static boolean debugLogging() {
        return config != null && config.getBoolean(KEY_DEBUG_LOGGING);
    }

    public static boolean debugItemGrantEnabled() {
        return config == null || config.getBoolean(KEY_DEBUG_ITEM_GRANT_ENABLED);
    }

    public static String debugItemGrantCharacterName() {
        return config != null ? config.getString(KEY_DEBUG_ITEM_GRANT_CHARACTER_NAME) : DEFAULT_DEBUG_ITEM_GRANT_CHARACTER_NAME;
    }

    /** Only logs when debugLogging is enabled - for per-item confirmations, not warnings/errors/summaries. */
    public static void debug(String message) {
        if (debugLogging()) {
            SSFMLLogger.log(message);
        }
    }

    private static Set<String> parsePackList(String raw) {
        Set<String> packs = new LinkedHashSet<>();

        for (String entry : raw.split(",")) {
            String trimmed = entry.trim();

            if (!trimmed.isEmpty()) {
                packs.add(trimmed.toLowerCase());
            }
        }

        return packs;
    }
}