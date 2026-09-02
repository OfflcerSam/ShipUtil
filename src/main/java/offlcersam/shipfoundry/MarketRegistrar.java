package offlcersam.shipfoundry;

import _settings.Localization;
import com.sector.bridge.SSFMLLogger;
import game.markets.Market;
import game.markets.MarketDatabase;
import game.markets.MarketItem;
import illuminatus.core.datastructures.List;
import items.Item;

import java.lang.reflect.Field;

/*
 * Registers ships that opted into market listings through their JSON data.
 */
public final class MarketRegistrar {

    private static boolean registered;

    private MarketRegistrar() {
    }

    public static void registerMarkets() {
        if (registered) {
            return;
        }
        registered = true;

        /*
         * Only ships with a "registration": { "market": { ... } } section are included here.
         */
        java.util.List<MarketListing> ships = ShipRegistrar.getMarketShipListings();

        if (ships.isEmpty()) {
            SSFMLLogger.log("[ShipFoundry] No custom ships opted into market registration");
            return;
        }

        int updatedMarkets = 0;
        int addedListings = 0;

        List<Market> markets = getMarkets();

        if (markets != null) {
            for (int marketIndex = 0; marketIndex < markets.size(); marketIndex++) {
                Market market = markets.getChecked(marketIndex);

                if (market == null) {
                    continue;
                }

                // Check MarketList for addStationIndices.
                if (market.stationMatches(501) || market.stationMatches(511)) {
                    addedListings += addListings(market, ships);

                    MarketDatabase.setMarket(marketIndex, market);
                    updatedMarkets++;
                }
            }
        }

        SSFMLLogger.log(
                "[ShipFoundry] Added "
                        + addedListings
                        + " custom ship listings to "
                        + updatedMarkets
                        + " markets"
        );
    }

    private static int addListings(Market market, java.util.List<MarketListing> listings) {
        int added = 0;

        // Ships always use the dedicated "Unique" market tag, regardless of which station type sells them.
        String marketTag = Localization.MARKET_UNIQUE_ITEM_TAG.string;

        for (MarketListing listing : listings) {
            if (listing.produce()) {
                MarketItem sellListing = new MarketItem(listing.databaseId(), MarketItem.PRODUCES_ALWAYS);

                if (sellListing.item != null) {
                    Item.markAsMarketItem(sellListing.item, marketTag);
                }

                market.addChecked(sellListing);
                added++;
            }

            if (listing.consume()) {
                MarketItem buyListing = new MarketItem(listing.databaseId(), MarketItem.CONSUMES_ALWAYS);

                if (buyListing.item != null) {
                    Item.markAsMarketItem(buyListing.item, marketTag);
                }

                market.addChecked(buyListing);
                added++;
            }
        }

        return added;
    }

    @SuppressWarnings("unchecked")
    private static List<Market> getMarkets() {
        try {
            Field field = MarketDatabase.class.getDeclaredField("markets");
            field.setAccessible(true);
            return (List<Market>) field.get(null);
        } catch (ReflectiveOperationException exception) {
            SSFMLLogger.log("[ShipFoundry] Could not access MarketDatabase markets: " + exception);
            return null;
        }
    }
}