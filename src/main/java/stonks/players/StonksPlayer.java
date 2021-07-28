package stonks.players;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import stonks.Stonks;
import stonks.stock.OfferType;
import stonks.stock.StockInfo;
import stonks.stock.StockOffer;

public class StonksPlayer implements Closeable {
    
    private static File playerDataFolder;
    public static void init(Stonks plugin) {
        playerDataFolder = new File(plugin.getDataFolder(), "players");
        if (!playerDataFolder.exists()) playerDataFolder.mkdirs();
    }
    
    public final UUID uuid;
    private File playerDataFile;
    private YamlConfiguration playerData;
    
    public StonksPlayer(UUID uuid) {
        this.uuid = uuid;
        this.playerDataFile = new File(playerDataFolder, uuid.toString() + ".yml");
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
    }
    
    public void updateOffer(StockOffer offer, boolean presistClaims) {
        if (offer.ownerUUID != uuid) return;
        if (offer.unitsClaimed == offer.unitsFilled && offer.unitsFilled == offer.units) {
            playerData.set(offer.offerUUID.toString(), null);
            return;
        }
        playerData.set(offer.offerUUID.toString() + ".id", offer.stock.itemType.dataId);
        playerData.set(offer.offerUUID.toString() + ".type", offer.offerType.toString());
        playerData.set(offer.offerUUID.toString() + ".pricePerUnit", offer.pricePerUnit);
        playerData.set(offer.offerUUID.toString() + ".units", offer.units);
        if (!presistClaims) playerData.set(offer.offerUUID.toString() + ".claimed", offer.unitsClaimed);
        playerData.set(offer.offerUUID.toString() + ".filled", offer.unitsFilled);
    }
    
    public ArrayList<StockOffer> getOffers() {
        ArrayList<StockOffer> offers = new ArrayList<>();
        for (String offerUUID : playerData.getKeys(false)) {
            StockInfo stock = Stonks.getStockInfo(playerData.getString(offerUUID + ".id"));
            OfferType type = OfferType.valueOf(playerData.getString(offerUUID + ".type"));
            double pricePerUnit = playerData.getDouble(offerUUID + ".pricePerUnit");
            int units = playerData.getInt(offerUUID + ".units");
            int claimed = playerData.getInt(offerUUID + ".claimed");
            int filled = playerData.getInt(offerUUID + ".filled");
            
            StockOffer offer = new StockOffer(stock, uuid, type, units, pricePerUnit);
            offer.offerUUID = UUID.fromString(offerUUID);
            offer.unitsClaimed = claimed;
            offer.unitsFilled = filled;
            offers.add(offer);
        }
        return offers;
    }
    
    @Override
    public void close() throws IOException {
        playerData.save(playerDataFile);
    }
    
}
