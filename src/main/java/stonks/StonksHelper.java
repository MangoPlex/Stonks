package stonks;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import featherpowders.items.CustomStack;
import featherpowders.ui.chest.ChestUI;
import stonks.stock.OfferType;
import stonks.stock.StockInfo;
import stonks.ui.StockUI;

public class StonksHelper {
    
    // TODO: Hook with Vault Economy Service
    
    public static boolean hasAtLeastMoney(Player player, double amount) {
        return true;
    }

    public static void takeMoney(Player player, double amount) {}
    public static void giveMoney(Player player, double amount) {}
    
    public static HashMap<UUID, OfferType> offeringCustom = new HashMap<>();
    public static HashMap<UUID, Integer> offeringUnits = new HashMap<>();
    public static HashMap<UUID, StockUI> stockUI = new HashMap<>();
    public static HashMap<UUID, ChestUI> offeringPreviousUI = new HashMap<>();
    
    public static void markBuyOfferCustom(Player player) {
        offeringCustom.put(player.getUniqueId(), OfferType.BUY_OFFER);
    }
    
    public static void markSellOfferCustom(Player player) {
        offeringCustom.put(player.getUniqueId(), OfferType.SELL_OFFER);
    }
    
    public static void clearOfferMarking(Player player) {
        offeringCustom.remove(player.getUniqueId());
        offeringUnits.remove(player.getUniqueId());
        stockUI.remove(player.getUniqueId());
    }
    
    public static int countUnitsInInventory(Player player, StockInfo stock) {
        int units = 0;
        for (CustomStack stack : Stonks.DRIVER.fromBukkit(player.getInventory().getContents())) {
            if (stack == null) continue;
            if (stack.type == stock.itemType) units += stack.amount;
        }
        return units;
    }
    
    public static int canAddUnits(Player player, StockInfo stock) {
        int units = 0;
        ItemStack sample = stock.createUnit(1);
        
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR || item.getAmount() == 0) {
                units += sample.getType().getMaxStackSize();
                continue;
            }
            if (item.isSimilar(sample)) units += sample.getType().getMaxStackSize() - item.getAmount();
        }
        return units;
    }
    
    public static void addOrDrop(Player player, StockInfo stock, int amount) {
        int canAdd = canAddUnits(player, stock);
        if (canAdd >= amount) {
            player.getInventory().addItem(stock.createUnit(amount));
        } else {
            player.getInventory().addItem(stock.createUnit(canAdd));
            player.getWorld().dropItem(player.getLocation(), stock.createUnit(amount - canAdd));
        }
    }
    
    public static void instantBuy(Player player, StockInfo stock, int amount) {
        double value = stock.calculateInstantBuy(amount);
        if (value < 0) {
            player.closeInventory();
            player.sendMessage("§cCannot perform action: §fNot enough units §7§o(try something smaller)");
            return;
        }
        if (!hasAtLeastMoney(player, value)) {
            player.closeInventory();
            player.sendMessage("§cNot enough money!");
            return;
        }
        
        takeMoney(player, value);
        stock.performInstantBuy(player, amount);
        player.sendMessage("§7You've bought " + amount + " units for " + value);
    }
    
}
