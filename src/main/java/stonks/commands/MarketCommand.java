package stonks.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import featherpowders.commands.ArgumentsMatch;
import featherpowders.commands.Command;
import featherpowders.ui.PlayerUI;
import stonks.ui.MarketUI;

public class MarketCommand extends Command {
    
    @ArgumentsMatch(pattern = {})
    public void index(CommandSender sender) {
        if (sender instanceof Player player) {
            MarketUI ui = new MarketUI(player, 0);
            PlayerUI.openUI(player, ui);
        } else sendError(sender, "Cannot open market in console", "But why doe?");
    }
    
}
