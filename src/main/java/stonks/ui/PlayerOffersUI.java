package stonks.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import featherpowders.ui.PlayerUI;
import featherpowders.ui.chest.ChestUI;
import featherpowders.utils.ItemBuilder;
import stonks.Stonks;
import stonks.StonksHelper;
import stonks.players.StonksPlayer;
import stonks.stock.OfferType;
import stonks.stock.StockOffer;

public class PlayerOffersUI extends ChestUI {
    
    public PlayerOffersUI(Player player, MarketUI marketUI) {
        super(player, "Market > Your Offers", 5);
        
        for (int i = 0; i < 9; i++) set(i, 0, MarketUI.BORDER, null);
        set(1, 0, new ItemBuilder(Material.ARROW, 1).name("§e <- Go Back ").getItem(), event -> {
            PlayerUI.openUI(player, marketUI);
        });
        
        try (StonksPlayer data = new StonksPlayer(player.getUniqueId())) {
            List<StockOffer> offers = data.getOffers();
            for (int i = 0; i < offers.size(); i++) {
                StockOffer offer = offers.get(i);
                
                double totalWithoutTax = offer.pricePerUnit * offer.units;
                double totalWithTax = totalWithoutTax * (1.0 - Stonks.TAX);
                
                ItemStack button = offer.stock.createUnit(1);
                ItemMeta meta = button.getItemMeta();
                meta.setDisplayName(offer.offerType.preferedColor + offer.offerType.offerUIName + "§7: " + meta.getDisplayName());
                List<String> lore = new ArrayList<String>();
                lore.add("");
                lore.add("§7Price Per Unit: §6" + offer.pricePerUnit);
                lore.add("§7Total: §6" + (offer.offerType == OfferType.BUY_OFFER? MarketUI.DECIMAL_FORMAT.format(totalWithoutTax) : MarketUI.DECIMAL_FORMAT.format(totalWithTax) + " §8(tax included)"));
                lore.add("");
                lore.add("§7Progress: §6" + offer.unitsClaimed + "§7/§a" + offer.unitsFilled + "§7/" + offer.units);
                if (offer.unitsClaimed < offer.unitsFilled) {
                    int units = (offer.unitsFilled - offer.unitsClaimed);
                    lore.add("§e§l(!) §eYou have §6" + (offer.offerType == OfferType.BUY_OFFER? units + " units" : MarketUI.DECIMAL_FORMAT.format(units * offer.pricePerUnit * (1.0 - Stonks.TAX))) + " §eto claim!");
                    lore.add("");
                    lore.add("§eClick §7to claim");
                } else {
                    lore.add("");
                    lore.add("§eClick §7to manage");
                }
                meta.setLore(lore);
                button.setItemMeta(meta);
                
                set(i % 9, 1 + (i / 9), button, event -> {
                    event.setCancelled(true);
                    int unitsCanClaim = (offer.unitsFilled - offer.unitsClaimed);
                    if (unitsCanClaim > 0) {
                        if (offer.offerType == OfferType.BUY_OFFER) {
                            unitsCanClaim = Math.min(unitsCanClaim, StonksHelper.canAddUnits(player, offer.stock));
                            if (unitsCanClaim == 0) {
                                player.sendMessage("§cCan't claim: §fYou don't have enough inventory space");
                                return;
                            }
                            player.getInventory().addItem(offer.stock.createUnit(unitsCanClaim));
                            offer.unitsClaimed += unitsCanClaim;
                            
                            Stonks.updateMarketOffer(offer);
                            try (StonksPlayer data2 = new StonksPlayer(player.getUniqueId())) {
                                data2.updateOffer(offer, false);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            
                            player.closeInventory();
                            player.sendMessage("§7Claimed " + unitsCanClaim + " units");
                        }
                        if (offer.offerType == OfferType.SELL_OFFER) {
                            double value = unitsCanClaim * offer.pricePerUnit * (1.0 - Stonks.TAX);
                            StonksHelper.giveMoney(player, value);
                            offer.unitsClaimed += unitsCanClaim;
                            
                            Stonks.updateMarketOffer(offer);
                            try (StonksPlayer data2 = new StonksPlayer(player.getUniqueId())) {
                                data2.updateOffer(offer, false);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            
                            player.closeInventory();
                            player.sendMessage("§7Claimed " + MarketUI.DECIMAL_FORMAT.format(value));
                        }
                    } else PlayerUI.openUI(player, new OfferOptionsUI(player, this, offer));
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void failback(InventoryClickEvent event) { event.setCancelled(true); }

}
