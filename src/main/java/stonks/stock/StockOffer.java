package stonks.stock;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StockOffer implements Comparable<StockOffer> {
    
    public UUID offerUUID;
    
    public StockInfo stock;
    public UUID ownerUUID;
    public OfferType offerType;
    public int units;
    public double pricePerUnit;
    
    public int unitsFilled = 0;
    public int unitsClaimed = 0;
    
    public StockOffer(StockInfo stock, UUID ownerUUID, OfferType offerType, int units, double pricePerUnit) {
        this.offerUUID = UUID.randomUUID();
        this.stock = stock;
        this.ownerUUID = ownerUUID;
        this.offerType = offerType;
        this.units = units;
        this.pricePerUnit = pricePerUnit;
    }
    
    public int unitsLeft() { return units - unitsFilled; }
    
    public void sendFilledMessage() {
        Player player = Bukkit.getPlayer(ownerUUID);
        if (player == null || !player.isOnline()) return;
        
        ItemStack sample = stock.createUnit(1);
        String displayName = sample.getItemMeta().getDisplayName();
        player.sendMessage("§8>> §eMarket§7: Your offer for " + (displayName != null? displayName : sample.getType()) + "§7 has been filled");
    }
    
    @Override
    public int compareTo(StockOffer o) {
        double d = 0;
        if (offerType == OfferType.BUY_OFFER) d = o.pricePerUnit - pricePerUnit;
        if (offerType == OfferType.SELL_OFFER) d = pricePerUnit - o.pricePerUnit;
        return d > 0? 1 : d < 0? -1 : 0;
    }
    
}
