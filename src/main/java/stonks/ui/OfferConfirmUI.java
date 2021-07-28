package stonks.ui;

import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import featherpowders.items.CustomStack;
import featherpowders.ui.PlayerUI;
import featherpowders.ui.chest.ChestUI;
import featherpowders.utils.ItemBuilder;
import stonks.Stonks;
import stonks.StonksHelper;
import stonks.players.StonksPlayer;
import stonks.stock.OfferType;
import stonks.stock.StockOffer;

public class OfferConfirmUI extends ChestUI {
    
    public OfferConfirmUI(Player player, ChestUI previousUI, StockUI stockUI, OfferType type, int units, double pricePerUnit) {
        super(player, "Confirm Offer", 4);
        
        double totalPrice = type == OfferType.SELL_OFFER? (pricePerUnit * units * (1.0 - Stonks.TAX)) : (pricePerUnit * units);

        for (int i = 0; i < 9; i++) set(i, 0, MarketUI.BORDER, null);
        set(1, 0, new ItemBuilder(Material.ARROW, 1).name("§e <- Go Back ").getItem(), event -> {
            PlayerUI.openUI(player, previousUI);
        });
        set(4, 0, stockUI.stock.createUnit(1), null);
        
        set(4, 2, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE, 1).name("§6Confirm Offer").lore(
            "",
            "§7Offer Type: §e" + type.offerUIName,
            "§7§e" + units + " §7units",
            "§7Price Per Unit: §6" + MarketUI.DECIMAL_FORMAT.format(pricePerUnit),
            "",
            "§7You'll " + (type == OfferType.BUY_OFFER? "pay" : "get") + " §6" + MarketUI.DECIMAL_FORMAT.format(totalPrice) + (type == OfferType.SELL_OFFER? " §8(tax included)" : ""),
            "",
            "§eClick §7to confirm"
        ).getItem(), event -> {
            event.setCancelled(true);
            
            if (type == OfferType.BUY_OFFER) {
                if (!StonksHelper.hasAtLeastMoney(player, pricePerUnit * units)) {
                    player.sendMessage("§cNot enough money! §fYou better get more, or just wait for the price to drop");
                    player.closeInventory();
                    return;
                }
                
                StonksHelper.takeMoney(player, pricePerUnit * units);
                StockOffer offer = stockUI.stock.buyOffer(player.getUniqueId(), units, pricePerUnit);
                try (StonksPlayer data = new StonksPlayer(player.getUniqueId())) {
                    data.updateOffer(offer, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Stonks.updateMarketOffer(offer);
                
                player.sendMessage("§eOffer Created! §7It's time to wait...");
                player.closeInventory();
            }
            if (type == OfferType.SELL_OFFER) {
                int unitsLeft = units;
                
                CustomStack[] stackInv = Stonks.DRIVER.fromBukkit(player.getInventory().getContents());
                for (int i = 0; i < stackInv.length; i++) {
                    CustomStack stack = stackInv[i];
                    if (stack == null) continue;
                    if (stack.type != stockUI.stock.itemType) continue;
                    int unitsTake = Math.min(unitsLeft, stack.amount);
                    unitsLeft -= unitsTake;
                    stack.amount -= unitsTake;
                    
                    if (stack.amount == 0) stackInv[i] = null;
                    if (unitsLeft == 0) break;
                }
                
                if (unitsLeft > 0) {
                    player.sendMessage("§cUnable to create offer: §fItems gone before confirmation §7§o(Magic?)");
                    player.closeInventory();
                    return;
                }
                
                player.getInventory().setContents(Stonks.DRIVER.fromCustom(stackInv));
                StockOffer offer = stockUI.stock.sellOffer(player.getUniqueId(), units, pricePerUnit);
                try (StonksPlayer data = new StonksPlayer(player.getUniqueId())) {
                    data.updateOffer(offer, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Stonks.updateMarketOffer(offer);
                
                player.sendMessage("§eOffer Created! §7It's time to wait...");
                player.closeInventory();
            }
        });
    }

    @Override
    public void failback(InventoryClickEvent event) { event.setCancelled(true); }

}
