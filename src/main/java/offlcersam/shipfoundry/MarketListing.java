package offlcersam.shipfoundry;

/**
 * One ship opted into market registration: its full database id, plus the produce/consume flags resolved from its MarketOptions.
 * Produced by ShipRegistrar, consumed by MarketRegistrar so it doesn't need to know about ShipDefinition directly.
 */
public record MarketListing(int databaseId, boolean produce, boolean consume) {
}