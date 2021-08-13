package stonks.hooks;

import org.bukkit.entity.Player;

public abstract class EconomyHook {
    
    public abstract void giveMoney(Player player, double amount);
    public abstract void takeMoney(Player player, double amount);
    public abstract boolean hasMoney(Player player, double amount);
    
}
