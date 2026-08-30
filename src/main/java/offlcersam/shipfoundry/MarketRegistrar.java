package offlcersam.shipfoundry;

import game.markets.Market;
import game.markets.MarketDatabase;
import game.markets.MarketItem;
import items.Item;
import illuminatus.core.datastructures.List;
import items.ItemTypeConstantsInterface;
import mods.ModLogger;
import java.lang.reflect.Field;

/*
TODO: Put JSON file ship data into here, expand upon it to include market tier customization (if possible).
 */
public final class MarketRegistrar {

    private static boolean registered;

    private MarketRegistrar() { }

    // Lazily adds all ships to every market tier for the moment, probably won't change until the next update.
    public static void registerMarkets() {
        if (registered) { return; }
        registered = true;

        int updatedMarkets = 0;
        int addedShips = 0;
        int[] ships = ShipRegistrar.getShipDatabaseIDs();

        List<Market> markets = getMarkets();

        if (markets != null) {
            for (int marketIndex = 0; marketIndex < markets.size(); marketIndex++) {
                Market market = markets.getChecked(marketIndex);
                if (market == null) {continue;}

                // Check MarketList for addStationIndices
                if (market.stationMatches(501) || market.stationMatches(511)) {

                    for (int shipID : ships) {
                        MarketItem listing = new MarketItem(
                                shipID,
                                MarketItem.BUY_AND_SELL_ALWAYS
                        );

                        if (listing.item != null) {
                            Item.markAsMarketItem(listing.item);
                        }

                        market.addChecked(listing);
                        addedShips++;
                    }

                    MarketDatabase.setMarket(marketIndex, market);
                    updatedMarkets++;
                }
            }
        }

        ModLogger.log(
                "[ShipTest] Added "
                        + addedShips
                        + " custom ship listings to "
                        + updatedMarkets
                        + " markets"
        );
    }

    @SuppressWarnings("unchecked")
    private static List<Market> getMarkets() {
        try {
            Field field = MarketDatabase.class.getDeclaredField("markets");
            field.setAccessible(true);
            return (List<Market>) field.get(null);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}