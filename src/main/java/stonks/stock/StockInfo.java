package stonks.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import featherpowders.items.CustomStack;
import featherpowders.items.CustomType;
import stonks.Stonks;
import stonks.players.StonksPlayer;

public class StockInfo {
    
    public CustomType itemType;
    public ArrayList<StockOffer> detailedBuyOffers = new ArrayList<>();
    public ArrayList<StockOffer> detailedSellOffers = new ArrayList<>();
    
    public double cachedBuyOfferPrice = -1;
    public double cachedSellOfferPrice = -1;
    
    public StockInfo(CustomType itemType) {
        this.itemType = itemType;
    }
    
    public void sortOffers() {
        Collections.sort(detailedBuyOffers);
        Collections.sort(detailedSellOffers);
    }
    
    /**
     * "Calculate" price per unit for this stock. This is used to display stock price information.
     * It also uses heavy sorting operation (the algorithm is actually optimized, but still heavy) to
     * get the offer with highest price per unit in buy offers and lowest price per unit in sell offers
     */
    public void calculatePrice(boolean sendFilledMessage) {
        sortOffers();

        filterOffers(detailedBuyOffers.iterator(), sendFilledMessage);
        filterOffers(detailedSellOffers.iterator(), sendFilledMessage);
        
        cachedBuyOfferPrice = detailedBuyOffers.size() > 0? detailedBuyOffers.get(0).pricePerUnit : -1;
        cachedSellOfferPrice = detailedSellOffers.size() > 0? detailedSellOffers.get(0).pricePerUnit : -1;
    }
    
    public StockOffer getOffer(OfferType type, UUID uuid) {
        if (type == OfferType.BUY_OFFER) return detailedBuyOffers.stream().filter(p -> p.offerUUID == uuid).findFirst().orElse(null);
        if (type == OfferType.SELL_OFFER) return detailedSellOffers.stream().filter(p -> p.offerUUID == uuid).findFirst().orElse(null);
        return null;
    }
    
    private void filterOffers(Iterator<StockOffer> iter, boolean sendFilledMessage) {
        while (iter.hasNext()) {
            StockOffer offer = iter.next();
            if (offer.unitsFilled == offer.units) {
                iter.remove();
                offer.sendFilledMessage();
                Stonks.updateMarketOffer(offer);
            }
        }
    }
    
    public StockOffer buyOffer(UUID owner, int units, double pricePerUnit) {
        StockOffer offer = new StockOffer(this, owner, OfferType.BUY_OFFER, units, pricePerUnit);
        detailedBuyOffers.add(offer);
        calculatePrice(true);
        return offer;
    }
    
    public StockOffer sellOffer(UUID owner, int units, double pricePerUnit) {
        StockOffer offer = new StockOffer(this, owner, OfferType.SELL_OFFER, units, pricePerUnit);
        detailedSellOffers.add(offer);
        calculatePrice(true);
        return offer;
    }
    
    public double calculateInstantBuy(int units) {
        // Assume the offers are sorted
        double price = 0;
        for (StockOffer offer : detailedSellOffers) {
            int unitsAdd = Math.min(offer.unitsLeft(), units);
            price += unitsAdd * offer.pricePerUnit;
            units -= unitsAdd;
            if (units == 0) return price;
        }
        if (units > 0) return -1;
        return price;
    }
    
    public void performInstantBuy(Player player, int units) {
        // Assume the offers are sorted
        int unitsCounter = units;
        for (StockOffer offer : detailedSellOffers) {
            int unitsAdd = Math.min(offer.unitsLeft(), units);
            unitsCounter -= unitsAdd;
            offer.unitsFilled += unitsAdd;
            
            try (StonksPlayer data = new StonksPlayer(offer.ownerUUID)) {
                data.updateOffer(offer, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stonks.updateMarketOffer(offer);
            
            if (unitsCounter == 0) break;
        }
        
        CustomStack stack = Stonks.DRIVER.createItem(itemType, units);
        ItemStack item = Stonks.DRIVER.fromCustom(stack);
        player.getInventory().addItem(item);
        calculatePrice(true);
    }
    
    public boolean canInstantSell(int units) {
        for (StockOffer offer : detailedBuyOffers) {
            units -= Math.min(offer.unitsLeft(), units);
            if (units == 0) return true;
        }
        if (units > 0) return false;
        return true;
    }
    
    public void instantSell(int units) {
        for (StockOffer offer : detailedBuyOffers) {
            int unitsTake = Math.min(units, offer.unitsLeft());
            offer.unitsFilled += unitsTake;
            units -= unitsTake;
            
            try (StonksPlayer data = new StonksPlayer(offer.ownerUUID)) {
                data.updateOffer(offer, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stonks.updateMarketOffer(offer);
            
            if (units == 0) break;
        }
        calculatePrice(true);
    }
    
    public ItemStack createUnit(int amount) {
        CustomStack stack = Stonks.DRIVER.createItem(itemType, amount);
        ItemStack item = Stonks.DRIVER.fromCustom(stack);
        return item;
    }
    
}
