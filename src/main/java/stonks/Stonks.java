package stonks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import featherpowders.items.CustomStack;
import featherpowders.items.CustomType;
import featherpowders.items.ItemsDriver;
import stonks.commands.MarketCommand;
import stonks.events.ChatEventsHandler;
import stonks.hooks.VaultEconomyHook;
import stonks.players.StonksPlayer;
import stonks.stock.OfferType;
import stonks.stock.StockInfo;
import stonks.stock.StockOffer;

public class Stonks extends JavaPlugin {
    
    public static double TAX;
    public static double MIN_PRICE;
    public static double PRICE_CAP;
    public static int MAX_UNITS;
    public static ArrayList<StockInfo> STOCKS = new ArrayList<>();
    public static ItemsDriver<CustomType, CustomStack> DRIVER;
    
    private static File stocksDataFile;
    private static YamlConfiguration stocksData;
    
    public static StockInfo getStockInfo(String id) {
        return STOCKS.stream().filter(p -> p.itemType.dataId.equals(id)).findFirst().orElse(null);
    }
    
    @Override
    public void onEnable() {
        DRIVER = ItemsDriver.getDefaultDriver();
        if (DRIVER == null) {
            getServer().getConsoleSender().sendMessage(new String[] {
                    "",
                    "§cFATAL ERROR: §fNo custom items driver found (FeatherPowder items driver)",
                    "§fPlease install a plugin that handle custom items",
                    "§fThe plugin must uses custom items driver API that's provided by FeatherPowder",
                    "",
                    "§eStonks will be disabled",
                    ""
            });
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Setup economy
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            System.out.println("[Stonks] Vault found, hooking...");
            VaultEconomyHook vault = new VaultEconomyHook();
            if (!vault.serviceAvailable()) System.err.println("[Stonks] Vault Economy service is not available. Look like you don't have economy plugin :(");
            else StonksHelper.economy = vault;
        }
        if (StonksHelper.economy == null) {
            getServer().getConsoleSender().sendMessage(new String[] {
                    "",
                    "§eWARNING: §fNo economy plugin found!",
                    "§fStonks will ignore all balance check and allow player to create orders without",
                    "§ftaking money",
                    ""
            });
        }
        
        saveResource("config.yml", false);
        reloadConfig();
        
        TAX = getConfig().getDouble("market.tax", 0.01);
        PRICE_CAP = getConfig().getDouble("market.unitPriceCap", 50_000_000.0);
        MIN_PRICE = getConfig().getDouble("market.minimumPrice", 0.5);
        MAX_UNITS = getConfig().getInt("market.unitsCap", 25600);
        
        StonksPlayer.init(this);
        stocksDataFile = new File(getDataFolder(), "stocks_data.yml");
        stocksData = YamlConfiguration.loadConfiguration(stocksDataFile);
        
        for (String itemID : getConfig().getStringList("stocks")) {
            CustomType itemType = DRIVER.fromDataID(itemID);
            if (itemType == null) {
                getServer().getConsoleSender().sendMessage("§e[Stonks] Item with ID " + itemID + " not found");
                continue;
            }
            
            StockInfo info = new StockInfo(itemType);
            STOCKS.add(info);
        }
        getServer().getConsoleSender().sendMessage("§e[Stonks] §f" + STOCKS.size() + " stocks registered in market!");

        getServer().getConsoleSender().sendMessage("§e[Stonks] §fLoading market offers data...");
        for (String id : stocksData.getKeys(false)) {
            StockInfo info = getStockInfo(id);
            for (String uuid : stocksData.getConfigurationSection(id).getKeys(false)) {
                UUID uuidObj = UUID.fromString(uuid);
                UUID owner = UUID.fromString(stocksData.getString(id + "." + uuid + ".owner"));
                OfferType type = OfferType.valueOf(stocksData.getString(id + "." + uuid + ".type"));
                double pricePerUnit = stocksData.getDouble(id + "." + uuid + ".pricePerUnit");
                int units = stocksData.getInt(id + "." + uuid + ".units");
                int filled = stocksData.getInt(id + "." + uuid + ".filled");
                
                StockOffer offer = new StockOffer(info, owner, type, units, pricePerUnit);
                offer.offerUUID = uuidObj;
                offer.unitsFilled = filled;

                if (type == OfferType.BUY_OFFER) info.detailedBuyOffers.add(offer);
                if (type == OfferType.SELL_OFFER) info.detailedSellOffers.add(offer);
            }
        }
        getServer().getConsoleSender().sendMessage("§e[Stonks] §fMarket data loaded");
        
        getServer().getPluginManager().registerEvents(new ChatEventsHandler(), this);
        
        getCommand("market").setExecutor(new MarketCommand());
        
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            STOCKS.forEach(stock -> { stock.calculatePrice(false); });
        }, 0, 20);
    }
    
    @Override
    public void onDisable() {
        try {
            stocksData.save(stocksDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void updateMarketOffer(StockOffer offer) {
        if (offer.unitsFilled == offer.units && stocksData.contains(offer.stock.itemType.dataId + "." + offer.offerUUID.toString())) {
            stocksData.set(offer.stock.itemType.dataId + "." + offer.offerUUID.toString(), null);
            return;
        }
        
        stocksData.set(offer.stock.itemType.dataId + "." + offer.offerUUID.toString() + ".owner", offer.ownerUUID.toString());
        stocksData.set(offer.stock.itemType.dataId + "." + offer.offerUUID.toString() + ".type", offer.offerType.toString());
        stocksData.set(offer.stock.itemType.dataId + "." + offer.offerUUID.toString() + ".pricePerUnit", offer.pricePerUnit);
        stocksData.set(offer.stock.itemType.dataId + "." + offer.offerUUID.toString() + ".units", offer.units);
        stocksData.set(offer.stock.itemType.dataId + "." + offer.offerUUID.toString() + ".filled", offer.unitsFilled);
    }
    
}
