package Admin.AdminSystem;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.event.Listener;
import java.util.List;

public final class AdminSystem extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new AdminUI(this), this);
        needAdminSetup();
        HelpCommand.AdminData.clearRequests();
    }

    private void needAdminSetup() {
        getCommand("needadmin").setExecutor(new HelpCommand());

        getCommand("needadmin").setTabCompleter(new TabCompleter() {
            @Override
            public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
                if (strings.length == 1)
                    return List.of("request", "queue", "cancel", "help", "clear");

                return List.of();
            }
        });

    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(new NamespacedKey("request", "colour")))
            HelpCommand.AdminData.removeValues(player);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        try {
            String[] lines = event.getLines();
            Bukkit.getPlayer(AdminUI.responseMap.get(player.getUniqueId())).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[NEEDADMIN] %s: ".formatted(player.getName()) + String.join(" ", lines));
        }
        catch (Exception e) {

        }

        Block originalBlock = AdminUI.originalBlockMap.get(player.getUniqueId());
        Block block = event.getBlock();

        block.setType(originalBlock.getType());
        block.setBlockData(originalBlock.getBlockData());

        AdminUI.responseMap.remove(player.getUniqueId());
        AdminUI.originalBlockMap.remove(player.getUniqueId());
    }
}
