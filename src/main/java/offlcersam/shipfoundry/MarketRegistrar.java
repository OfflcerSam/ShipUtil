package offlcersam.shipfoundry;

import _settings.Localization;
import com.sector.bridge.SSFMLLogger;
import game.markets.Market;
import game.markets.MarketDatabase;
import illuminatus.core.datastructures.List;
import items.Item;

import java.lang.reflect.Field;

/*
 * Registers ships that opted into market listings through their JSON data.
 */
public final class MarketRegistrar {

    private static final int PRODUCES_SOMETIMES = 3;

    private static boolean registered;

    private MarketRegistrar() {
    }

    public static void registerMarkets() {
        if (registered) {
            return;
        }
        registered = true;

        int updatedMarkets = 0;
        int addedShips = 0;

        /*
         * Only ships with "registration": { "market": true } are included here.
         */
        int[] ships = ShipRegistrar.getMarketShipDatabaseIDs();

        if (ships.length == 0) {
            SSFMLLogger.log("[ShipFoundry] No custom ships opted into market registration");
            return;
        }

        List<Market> markets = getMarkets();

        if (markets != null) {
            for (int marketIndex = 0; marketIndex < markets.size(); marketIndex++) {
                Market market = markets.getChecked(marketIndex);

                if (market == null) {
                    continue;
                }

                // Check MarketList for addStationIndices.
                if (market.stationMatches(501) || market.stationMatches(511)) {

                    for (int shipID : ships) {
                        Item shipItem = new Item(shipID);
                        Item.markAsMarketItem(shipItem, Localization.MARKET_UNIQUE_ITEM_TAG.string);
                        market.addChecked(shipItem, PRODUCES_SOMETIMES);
                        addedShips++;
                    }

                    MarketDatabase.setMarket(marketIndex, market);
                    updatedMarkets++;
                }
            }
        }

        SSFMLLogger.log(
                "[ShipFoundry] Added "
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
            SSFMLLogger.log("[ShipFoundry] Could not access MarketDatabase markets: " + exception);
            return null;
        }
    }
}