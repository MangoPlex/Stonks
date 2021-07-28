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

public class OfferUI extends ChestUI {
    
    public OfferUI(Player player, StockUI stockUI, OfferType type, int units) {
        super(player, "Market > Stock " + type.offerUIName + (type == OfferType.BUY_OFFER? " (x" + units + ")" : ""), 4);
        
        for (int i = 0; i < 9; i++) set(i, 0, MarketUI.BORDER, null);
        set(1, 0, new ItemBuilder(Material.ARROW, 1).name("§e <- Go Back ").getItem(), event -> {
            PlayerUI.openUI(player, stockUI);
        });
        set(4, 0, stockUI.stock.createUnit(1), null);
        
        if (type == OfferType.BUY_OFFER) {
            double topOffer = stockUI.stock.cachedBuyOfferPrice;

            set(1, 2, new ItemBuilder(topOffer >= 0? Material.GOLD_INGOT : Material.BARRIER, 1).name("§6Same as top offer").lore(
                "§7Offer your money for items with",
                "§7the price equals to current top",
                "§7offer",
                "",
                "§7New offer: " + (stockUI.stock.cachedBuyOfferPrice >= 0? "§6" + MarketUI.DECIMAL_FORMAT.format(stockUI.stock.cachedBuyOfferPrice) + "§7/unit" : MarketUI.NOT_AVALIABLE),
                "",
                topOffer >= 0? "§eClick §7to proceed" : "§cNo top offer :("
            ).getItem(), topOffer < 0? null : event -> {
                double pricePerUnit = stockUI.stock.cachedBuyOfferPrice;
                PlayerUI.openUI(player, new OfferConfirmUI(player, this, stockUI, type, units, pricePerUnit));
            });

            set(4, 2, new ItemBuilder(topOffer >= 0? Material.GOLD_INGOT : Material.BARRIER, 1).name("§6Top offer + 0.1").lore(
                "§7Offer your money for items with",
                "§7the price higher than top offer",
                "§7so your offer will be filled first",
                "",
                "§7New offer: " + (stockUI.stock.cachedBuyOfferPrice >= 0? "§6" + MarketUI.DECIMAL_FORMAT.format(stockUI.stock.cachedBuyOfferPrice + 0.1) + "§7/unit" : MarketUI.NOT_AVALIABLE),
                "",
                topOffer >= 0? stockUI.stock.cachedBuyOfferPrice + 0.1 > Stonks.PRICE_CAP? "§cPrice cap reached" : "§eClick §7to proceed" : "§cNo top offer :("
            ).getItem(), topOffer < 0? null : event -> {
                event.setCancelled(true);
                if (stockUI.stock.cachedBuyOfferPrice + 0.1 > Stonks.PRICE_CAP) return;
                
                double pricePerUnit = stockUI.stock.cachedBuyOfferPrice + 0.1;
                PlayerUI.openUI(player, new OfferConfirmUI(player, this, stockUI, type, units, pricePerUnit));
            });

            set(7, 2, new ItemBuilder(Material.GOLD_INGOT, 1).name("§6Custom price").lore(
                "§7Offer your money for items with",
                "§7the price of your choice",
                "",
                "§7Can place up to §6" + Stonks.PRICE_CAP,
                "",
                "§eClick §7to proceed"
            ).getItem(), event -> {
                event.setCancelled(true);
                player.closeInventory();
                player.sendMessage(new String[] {
                    "",
                    "§7You're about to offer your money for " + units + " items",
                    "§7Please type in chat how much you're willing to offer per unit",
                    "§7Type §ccancel §7to cancel",
                    ""
                });
                StonksHelper.markBuyOfferCustom(player);
                StonksHelper.stockUI.put(player.getUniqueId(), stockUI);
                StonksHelper.offeringUnits.put(player.getUniqueId(), units);
                StonksHelper.offeringPreviousUI.put(player.getUniqueId(), this);
            });
        }
        if (type == OfferType.SELL_OFFER) {
            double topOffer = stockUI.stock.cachedSellOfferPrice;
            int sellUnitsCalc = 0;
            for (CustomStack stack : Stonks.DRIVER.fromBukkit(player.getInventory().getContents())) {
                if (stack == null) continue;
                if (stack.type == stockUI.stock.itemType) sellUnitsCalc += stack.amount;
            }
            int sellUnits = sellUnitsCalc;
            
            set(1, 2, new ItemBuilder(topOffer >= 0? Material.GOLD_INGOT : Material.BARRIER, 1).name("§6Same as top offer").lore(
                "§7Offer all items in your inventory",
                "§7with the price equals to current",
                "§7top offer",
                "",
                "§7New offer: " + (stockUI.stock.cachedSellOfferPrice >= 0? "§6" + MarketUI.DECIMAL_FORMAT.format(stockUI.stock.cachedSellOfferPrice) + "§7/unit" : MarketUI.NOT_AVALIABLE),
                "",
                topOffer >= 0? "§eClick §7to proceed" : "§cNo top offer :("
            ).getItem(), topOffer < 0? null : event -> {
                double pricePerUnit = stockUI.stock.cachedSellOfferPrice;
                PlayerUI.openUI(player, new OfferConfirmUI(player, this, stockUI, type, sellUnits, pricePerUnit));
            });
            set(4, 2, new ItemBuilder(topOffer >= 0? Material.GOLD_INGOT : Material.BARRIER, 1).name("§6Top offer - 0.1").lore(
                "§7Offer all items in your inventory",
                "§7with the price lower than top offer",
                "§7so your offer will be filled first",
                "",
                "§7New offer: " + (stockUI.stock.cachedSellOfferPrice >= 0? "§6" + MarketUI.DECIMAL_FORMAT.format(stockUI.stock.cachedSellOfferPrice - 0.1) + "§7/unit" : MarketUI.NOT_AVALIABLE),
                "",
                topOffer >= 0? stockUI.stock.cachedBuyOfferPrice - 0.1 < Stonks.MIN_PRICE? "§cMinimum price reached" : "§eClick §7to proceed" : "§cNo top offer :("
            ).getItem(), topOffer < 0? null : event -> {
                event.setCancelled(true);
                if (stockUI.stock.cachedBuyOfferPrice - 0.1 < Stonks.MIN_PRICE) return;
                
                double pricePerUnit = stockUI.stock.cachedSellOfferPrice - 0.1;
                PlayerUI.openUI(player, new OfferConfirmUI(player, this, stockUI, type, sellUnits, pricePerUnit));
            });
            set(7, 2, new ItemBuilder(Material.GOLD_INGOT, 1).name("§6Custom price").lore(
                "§7Offer all items in your inventory",
                "§7with the price of your choice",
                "",
                "§7Can place up to §6" + Stonks.PRICE_CAP,
                "",
                "§eClick §7to proceed"
            ).getItem(), event -> {
                event.setCancelled(true);
                player.closeInventory();
                player.sendMessage(new String[] {
                    "",
                    "§7You're about to offer all items in your inventory with custom price",
                    "§7Please type in chat how much you're willing to offer per unit",
                    "§7Type §ccancel §7to cancel",
                    ""
                });
                StonksHelper.markSellOfferCustom(player);
                StonksHelper.stockUI.put(player.getUniqueId(), stockUI);
                StonksHelper.offeringPreviousUI.put(player.getUniqueId(), this);
            });
        }
    }

    @Override
    public void failback(InventoryClickEvent event) { event.setCancelled(true); }

}
