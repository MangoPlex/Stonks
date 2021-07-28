package stonks.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import featherpowders.ui.PlayerUI;
import featherpowders.ui.chest.ChestUI;
import featherpowders.utils.ItemBuilder;
import stonks.Stonks;
import stonks.StonksHelper;
import stonks.stock.OfferType;

public class BuyOfferUnitsSelectUI extends ChestUI {
    
    public BuyOfferUnitsSelectUI(Player player, StockUI stockUI) {
        super(player, "Market > Stock Buy Offer", 4);
        
        for (int i = 0; i < 9; i++) set(i, 0, MarketUI.BORDER, null);
        set(1, 0, new ItemBuilder(Material.ARROW, 1).name("§e <- Go Back ").getItem(), event -> {
            PlayerUI.openUI(player, stockUI);
        });
        set(4, 0, stockUI.stock.createUnit(1), null);

        set(1, 2, new ItemBuilder(Material.GOLD_INGOT, 1).name("§6A stack §7(x64)").getItem(), event -> selectUnits(stockUI, 64));
        set(3, 2, new ItemBuilder(Material.GOLD_INGOT, 2).name("§62 stacks §7(x128)").getItem(), event -> selectUnits(stockUI, 128));
        set(5, 2, new ItemBuilder(Material.GOLD_INGOT, 16).name("§6A thousand §7(x1024)").getItem(), event -> selectUnits(stockUI, 1024));
        set(7, 2, new ItemBuilder(Material.GOLD_INGOT, 1).name("§6Custom amount").lore("§7Can buy up to §e" + Stonks.MAX_UNITS + " §7units").getItem(), event -> {
            event.setCancelled(true);
            StonksHelper.markBuyOfferCustom(player);
            StonksHelper.stockUI.put(player.getUniqueId(), stockUI);
            player.closeInventory();
            player.sendMessage(new String[] {
                "",
                "§7Type in chat how many units you want to buy",
                "§7Type §ccancel §7to cancel",
                ""
            });
        });
    }
    
    private void selectUnits(StockUI stockUI, int amount) {
        OfferUI offerUI = new OfferUI(player, stockUI, OfferType.BUY_OFFER, amount);
        PlayerUI.openUI(player, offerUI);
    }

    @Override
    public void failback(InventoryClickEvent event) { event.setCancelled(true); }

}
