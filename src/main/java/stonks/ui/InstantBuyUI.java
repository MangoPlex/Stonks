package stonks.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import featherpowders.ui.PlayerUI;
import featherpowders.ui.chest.ChestUI;
import featherpowders.utils.ItemBuilder;
import stonks.StonksHelper;

public class InstantBuyUI extends ChestUI {
    
    public InstantBuyUI(Player player, StockUI stockUI) {
        super(player, "Market > Instant Buy", 4);
        
        for (int i = 0; i < 9; i++) set(i, 0, MarketUI.BORDER, null);
        set(1, 0, new ItemBuilder(Material.ARROW, 1).name("§e <- Go Back ").getItem(), event -> {
            PlayerUI.openUI(player, stockUI);
        });
        set(4, 0, stockUI.stock.createUnit(1), null);

        int freeSpace = StonksHelper.canAddUnits(player, stockUI.stock);
        double _32 = 32 * stockUI.stock.cachedSellOfferPrice;
        double _64 = 64 * stockUI.stock.cachedSellOfferPrice;
        double _128 = 128 * stockUI.stock.cachedSellOfferPrice;
        double _all = freeSpace * stockUI.stock.cachedSellOfferPrice;

        set(1, 2, new ItemBuilder(Material.GOLD_INGOT, 32).name("§6Buy 32 units").lore("§7Price: §6" + (_32 >= 0? MarketUI.DECIMAL_FORMAT.format(_32) : MarketUI.NOT_AVALIABLE), "", "§eClick §7to buy").getItem(), event -> {
            event.setCancelled(true);
            StonksHelper.instantBuy(player, stockUI.stock, 32);
        });
        set(3, 2, new ItemBuilder(Material.GOLD_INGOT, 64).name("§6Buy a stack").lore("§7Price: §6" + (_64 >= 0? MarketUI.DECIMAL_FORMAT.format(_64) : MarketUI.NOT_AVALIABLE), "", "§eClick §7to buy").getItem(), event -> {
            event.setCancelled(true);
            StonksHelper.instantBuy(player, stockUI.stock, 64);
        });
        set(5, 2, new ItemBuilder(Material.CHEST, 2).name("§6Buy 2 stacks").lore("§7Price: §6" + (_128 >= 0? MarketUI.DECIMAL_FORMAT.format(_128) : MarketUI.NOT_AVALIABLE), "", "§eClick §7to buy").getItem(), event -> {
            event.setCancelled(true);
            StonksHelper.instantBuy(player, stockUI.stock, 128);
        });
        set(7, 2, new ItemBuilder(Material.CHEST, 64).name("§6Fill inventory").lore("§e" + freeSpace + " §7units", "§7Price: §6" + (_all >= 0? MarketUI.DECIMAL_FORMAT.format(_all) : MarketUI.NOT_AVALIABLE), "", "§eClick §7to buy").getItem(), event -> {
            event.setCancelled(true);
            StonksHelper.instantBuy(player, stockUI.stock, StonksHelper.canAddUnits(player, stockUI.stock));
        });
    }
    
    @Override
    public void failback(InventoryClickEvent event) { event.setCancelled(true); }

}
