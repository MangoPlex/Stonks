package stonks.ui;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import featherpowders.ui.PlayerUI;
import featherpowders.ui.chest.ChestUI;
import featherpowders.utils.ItemBuilder;
import stonks.Stonks;
import stonks.StonksHelper;
import stonks.players.StonksPlayer;
import stonks.stock.OfferType;
import stonks.stock.StockOffer;

public class OfferOptionsUI extends ChestUI {
    
    public OfferOptionsUI(Player player, PlayerOffersUI prevUI, StockOffer offer) {
        super(player, "Market > Offer Options", 4);
        
        for (int i = 0; i < 9; i++) set(i, 0, MarketUI.BORDER, null);
        set(1, 0, new ItemBuilder(Material.ARROW, 1).name("§e <- Go Back ").getItem(), event -> {
            PlayerUI.openUI(player, prevUI);
        });
        set(4, 0, offer.stock.createUnit(1), null);
        
        set(4, 2, new ItemBuilder(Material.ANVIL, 1).name("§6Cancel Order").lore(
            "§7Cancel order and refund",
            offer.offerType == OfferType.BUY_OFFER? "§6" + MarketUI.DECIMAL_FORMAT.format(offer.unitsLeft() * offer.pricePerUnit) : "§e" + offer.unitsLeft() + " §7units",
            "",
            "§eClick §7to cancel"
        ).getItem(), event -> {
            event.setCancelled(true);
            if (offer.offerType == OfferType.BUY_OFFER) {
                StonksHelper.giveMoney(player, offer.unitsLeft() * offer.pricePerUnit);
                player.sendMessage("§7Refunded " + MarketUI.DECIMAL_FORMAT.format(offer.unitsLeft() * offer.pricePerUnit));
            }
            if (offer.offerType == OfferType.SELL_OFFER) {
                StonksHelper.addOrDrop(player, offer.stock, offer.unitsLeft());
                player.sendMessage("§7Refunded " + offer.unitsLeft() + " units");
            }
            
            player.closeInventory();
            
            offer.unitsClaimed = offer.unitsFilled = offer.units;
            try (StonksPlayer data = new StonksPlayer(player.getUniqueId())) {
                data.updateOffer(offer, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stonks.updateMarketOffer(offer);
            
            if (offer.offerType == OfferType.BUY_OFFER) findAndRemove(offer.stock.detailedBuyOffers.iterator(), offer.offerUUID);
            if (offer.offerType == OfferType.SELL_OFFER) findAndRemove(offer.stock.detailedSellOffers.iterator(), offer.offerUUID);
            offer.stock.calculatePrice(true);
        });
    }
    
    private void findAndRemove(Iterator<StockOffer> iter, UUID offerUUID) {
        while (iter.hasNext()) {
            StockOffer offer = iter.next();
            if (offer.offerUUID.equals(offerUUID)) iter.remove();
        }
    }
    
    @Override
    public void failback(InventoryClickEvent event) { event.setCancelled(true); }

}
