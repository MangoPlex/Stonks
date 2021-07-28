package stonks.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import featherpowders.ui.PlayerUI;
import stonks.Stonks;
import stonks.StonksHelper;
import stonks.stock.OfferType;
import stonks.ui.OfferConfirmUI;
import stonks.ui.OfferUI;
import stonks.ui.StockUI;

public class ChatEventsHandler implements Listener {
    
    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        processEventAsync(event, player, message);
    }
    
    private void processEventAsync(AsyncPlayerChatEvent event, Player player, String message) {
        if (StonksHelper.offeringCustom.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            
            Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(Stonks.class), () -> {
                if (message.equalsIgnoreCase("cancel")) {
                    player.sendMessage("§cOffer canceled");
                    StonksHelper.clearOfferMarking(player);
                    return;
                }
                
                double value;
                try {
                    value = Double.parseDouble(message);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid input, offer canceled");
                    StonksHelper.clearOfferMarking(player);
                    return;
                }
                
                OfferType type = StonksHelper.offeringCustom.get(player.getUniqueId());
                if (type == OfferType.BUY_OFFER) {
                    if (!StonksHelper.offeringUnits.containsKey(player.getUniqueId())) {
                        int units = (int) Math.round(value);
                        if (units < 1 || units > Stonks.MAX_UNITS) {
                            player.sendMessage("§cUnits count invalid, offer canceled");
                            StonksHelper.clearOfferMarking(player);
                            return;
                        }
                        
                        StockUI stockUI = StonksHelper.stockUI.get(player.getUniqueId());
                        StonksHelper.offeringUnits.put(player.getUniqueId(), units);
                        PlayerUI.openUI(player, new OfferUI(player, stockUI, type, units));
                    } else {
                        if (value < Stonks.MIN_PRICE || value > Stonks.PRICE_CAP) {
                            player.sendMessage("§cInvalid price, offer canceled");
                            StonksHelper.clearOfferMarking(player);
                            return;
                        }
                        
                        int units = StonksHelper.offeringUnits.get(player.getUniqueId());
                        StockUI stockUI = StonksHelper.stockUI.get(player.getUniqueId());
                        PlayerUI.openUI(player, new OfferConfirmUI(player, StonksHelper.offeringPreviousUI.get(player.getUniqueId()), stockUI, type, units, value));
                        StonksHelper.clearOfferMarking(player);
                    }
                }
                if (type == OfferType.SELL_OFFER) {
                    if (value < Stonks.MIN_PRICE || value > Stonks.PRICE_CAP) {
                        player.sendMessage("§cInvalid price, offer canceled");
                        StonksHelper.clearOfferMarking(player);
                        return;
                    }
                    
                    StockUI stockUI = StonksHelper.stockUI.get(player.getUniqueId());
                    int units = StonksHelper.countUnitsInInventory(player, stockUI.stock);
                        
                    PlayerUI.openUI(player, new OfferConfirmUI(player, StonksHelper.offeringPreviousUI.get(player.getUniqueId()), stockUI, type, units, value));
                    StonksHelper.clearOfferMarking(player);
                }
            });
        }
    }
    
}
