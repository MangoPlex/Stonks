package stonks.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class VaultEconomyHook extends EconomyHook {

    private boolean available = false;
    private Economy economy;
    
    public VaultEconomyHook() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) { available = false; return; }
        economy = rsp.getProvider();
        if (economy == null) { available = false; return; }
    }
    
    public boolean serviceAvailable() { return available; }
    
    @Override
    public void giveMoney(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }

    @Override
    public void takeMoney(Player player, double amount) {
        economy.withdrawPlayer(player, amount);
    }

    @Override
    public boolean hasMoney(Player player, double amount) {
        return economy.has(player, amount);
    }

}
