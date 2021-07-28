package stonks.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import featherpowders.items.CustomStack;
import featherpowders.ui.PlayerUI;
import featherpowders.ui.chest.ChestUI;
import featherpowders.utils.ItemBuilder;
import stonks.Stonks;
import stonks.stock.StockInfo;

public class MarketUI extends ChestUI {
    
    protected static final ItemStack BORDER = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE, 1).name("§0").getItem();
    protected static final String NOT_AVALIABLE = "§8n/a";
    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,##0.0");
    
    private int page;
    
    public MarketUI(Player player, int page) {
        super(player, "Market", 5);
        this.page = page;
        
        for (int i = 0; i < 9; i++) set(i, 0, BORDER, null);
        set(3, 0, new ItemBuilder(Material.PAPER, 1).name("§eSell all").lore("§7Sell all items in your", "§7inventory", "", "§eClick §7to sell").getItem(), null);
        set(5, 0, new ItemBuilder(Material.GOLD_INGOT, 1).name("§eYour offers").lore("§7View your offers", "", "§eClick §7to open").getItem(), event -> {
            PlayerUI.openUI(player, new PlayerOffersUI(player, this));
        });
        renderPage();
    }
    
    private void renderPage() {
        if (page > 0) set(1, 0, new ItemBuilder(Material.ARROW, 1).name("§e <- Previous Page ").getItem(), event -> {
            page--;
            renderPage();
        });
        if ((page + 1) * 36 < Stonks.STOCKS.size()) set(7, 0, new ItemBuilder(Material.ARROW, 1).name("§e Next Page -> ").getItem(), event -> {
            page++;
            renderPage();
        });
        
        for (int i = 0; i < 36; i++) {
            int stockIndex = page * 36 + i;
            if (Stonks.STOCKS.size() <= stockIndex) {
                clearSlot(i % 9, 1 + (i / 9));
                continue;
            }
            
            StockInfo info = Stonks.STOCKS.get(stockIndex);
            CustomStack stack = Stonks.DRIVER.createItem(info.itemType);
            ItemStack item = Stonks.DRIVER.fromCustom(stack);
            
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Instant Buy Price: " + (info.cachedSellOfferPrice >= 0? "§6" + DECIMAL_FORMAT.format(info.cachedSellOfferPrice) : NOT_AVALIABLE));
            lore.add("§7Instant Sell Price: " + (info.cachedBuyOfferPrice >= 0? "§6" + DECIMAL_FORMAT.format(info.cachedBuyOfferPrice) : NOT_AVALIABLE));
            lore.add("");
            lore.add("§eClick §7for details");
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            set(i % 9, 1 + (i / 9), item, event -> {
                StockUI ui = new StockUI(player, this, info);
                PlayerUI.openUI(player, ui);
            });
        }
    }

    @Override
    public void failback(InventoryClickEvent event) { event.setCancelled(true); }

}
