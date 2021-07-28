package stonks.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import featherpowders.items.CustomStack;
import featherpowders.ui.PlayerUI;
import featherpowders.ui.chest.ChestUI;
import featherpowders.utils.ItemBuilder;
import stonks.Stonks;
import stonks.StonksHelper;
import stonks.stock.OfferType;
import stonks.stock.StockInfo;

public class StockUI extends ChestUI {
    
    public StockInfo stock;
    
    public StockUI(Player player, MarketUI marketUI, StockInfo stock) {
        super(player, "Market > Stock Info", 4);
        this.stock = stock;
        
        int unitsInInv = StonksHelper.countUnitsInInventory(player, stock);
        
        for (int i = 0; i < 9; i++) set(i, 0, MarketUI.BORDER, null);
        set(1, 0, new ItemBuilder(Material.ARROW, 1).name("§e <- Go Back ").getItem(), event -> {
            PlayerUI.openUI(player, marketUI);
        });
        set(4, 0, stock.createUnit(1), null);

        set(1, 2, new ItemBuilder(Material.GOLD_INGOT, 1).name("§6Buy Instantly").lore(
            "§7Instant Buy Price: " + (stock.cachedSellOfferPrice >= 0? "§6" + MarketUI.DECIMAL_FORMAT.format(stock.cachedSellOfferPrice) : MarketUI.NOT_AVALIABLE),
            "",
            "§eLeft click §7for options",
            "§eRight click §7to buy x1"
        ).getItem(), event -> {
            event.setCancelled(true);
            if (event.isLeftClick()) PlayerUI.openUI(player, new InstantBuyUI(player, this));
            if (event.isRightClick()) StonksHelper.instantBuy(player, stock, 1);
        });
        set(2, 2, new ItemBuilder(Material.HOPPER, 1).name("§6Sell Instantly").lore(
            "§7Instant Sell Price: " + (stock.cachedBuyOfferPrice >= 0? "§6" + MarketUI.DECIMAL_FORMAT.format(stock.cachedBuyOfferPrice) : MarketUI.NOT_AVALIABLE),
            "§7You have §e" + unitsInInv + " §7units",
            "§7You will get §6" + (stock.cachedBuyOfferPrice >= 0? MarketUI.DECIMAL_FORMAT.format((unitsInInv * stock.cachedBuyOfferPrice) * (1.0 - Stonks.TAX)) + " §8(tax included)" : "§cnothing!"),
            "",
            stock.cachedBuyOfferPrice < 0? "§cNo buy offers :(" : unitsInInv > 0? "§eClick §7to sell all" : "§cYou have no item"
        ).getItem(), event -> {
            event.setCancelled(true);
            if (unitsInInv == 0) return;
            
            if (!stock.canInstantSell(unitsInInv)) {
                player.sendMessage("§cUnable to instant sell: §fNot enough offer §7§o(please take some units out of your inventory)");
                player.closeInventory();
                return;
            }
            
            int unitsLeft = unitsInInv;
            double value = (unitsInInv * stock.cachedBuyOfferPrice) * (1.0 - Stonks.TAX);
            CustomStack[] stackInv = Stonks.DRIVER.fromBukkit(player.getInventory().getContents());
            for (int i = 0; i < stackInv.length; i++) {
                CustomStack stack = stackInv[i];
                if (stack == null) continue;
                if (stack.type != stock.itemType) continue;
                int unitsTake = Math.min(unitsLeft, stack.amount);
                unitsLeft -= unitsTake;
                stack.amount -= unitsTake;
                
                if (stack.amount == 0) stackInv[i] = null;
                if (unitsLeft == 0) break;
            }
            
            if (unitsLeft > 0) {
                player.sendMessage("§cUnable to instant sell: §fItems gone before sell §7§o(Magic?)");
                player.closeInventory();
                return;
            }
            
            player.getInventory().setContents(Stonks.DRIVER.fromCustom(stackInv));
            stock.instantSell(unitsInInv);
            StonksHelper.giveMoney(player, value);
            player.sendMessage("§7Sold " + unitsInInv + " units for " + MarketUI.DECIMAL_FORMAT.format(value));
            player.closeInventory();
        });

        set(6, 2, new ItemBuilder(Material.DIAMOND, 1).name("§6Create Buy Offer").lore(
            "§7Current Buy Price: " + (stock.cachedBuyOfferPrice >= 0? "§6" + stock.cachedBuyOfferPrice : MarketUI.NOT_AVALIABLE),
            "",
            "§eClick §7to create"
        ).getItem(), event -> {
            event.setCancelled(true);
            PlayerUI.openUI(player, new BuyOfferUnitsSelectUI(player, this));
        });
        set(7, 2, new ItemBuilder(Material.HOPPER, 1).name("§6Create Sell Offer").lore(
            "§7Current Sell Price: " + (stock.cachedSellOfferPrice >= 0? "§6" + stock.cachedSellOfferPrice : MarketUI.NOT_AVALIABLE),
            "",
            unitsInInv > 0? "§eClick §7to create" : "§cYou have no item"
        ).getItem(), event -> {
            event.setCancelled(true);
            if (unitsInInv == 0) return;
            PlayerUI.openUI(player, new OfferUI(player, this, OfferType.SELL_OFFER, 0));
        });
    }
    
    @Override
    public void failback(InventoryClickEvent event) {
        event.setCancelled(true);
    }
    
}
