package offlcersam.shipfoundry;

import offlcersam.shipfoundry.json.JsonValue;

/**
 * Optional per-ship market registration control, parsed from the "market" object inside a ship's
 * "registration" section.
 * <p>
 * JSON shape:
 * <pre>
 * "registration": {
 *   "market": {
 *     "produce": true,   // list as an always-buyable (PRODUCES_ALWAYS) shipyard entry - default true
 *     "consume": true    // list as an always-sellable-back (CONSUMES_ALWAYS) shipyard entry - default true
 *   }
 * }
 * </pre>
 * Omitting "market" entirely (or leaving out "registration" altogether) means the ship isn't added to
 * any market at all - same as the old "registration": { "market": false }.
 * "market": {} (or setting both flags false) also counts as opting out, since there'd be nothing left to list.
 * <p>
 * Game update: this replaces the old plain "market": true/false boolean now that MarketItem no longer has a
 * single BUY_AND_SELL_ALWAYS constant covering both directions at once. See MarketRegistrar for how
 * produce/consume map onto MarketItem entries.
 */
public record MarketOptions(boolean produce, boolean consume) {

    /** Returns null if "market" is absent, null, or resolves to neither direction being enabled. */
    public static MarketOptions parse(JsonValue root) {
        JsonValue marketValue = root.getOrNull("market");

        if (marketValue == null || marketValue.isNull()) {
            return null;
        }

        boolean produce = marketValue.getBoolean("produce", true);
        boolean consume = marketValue.getBoolean("consume", true);

        if (!produce && !consume) {
            return null;
        }

        return new MarketOptions(produce, consume);
    }
}