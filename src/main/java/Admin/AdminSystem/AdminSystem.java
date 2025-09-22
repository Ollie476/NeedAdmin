package Admin.AdminSystem;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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
        Bukkit.getScheduler().runTask(this, () -> {
            getCommand("needadmin").setExecutor(new HelpCommand());
            getCommand("needadmin").setTabCompleter((sender, command, alias, args) -> {
                if (args.length == 1)
                    return List.of("request", "queue", "cancel", "help", "clear");
                return List.of();
            });

            HelpCommand.AdminData.clearRequests();
        });

        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new AdminUI(this), this);
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

        BlockSnapshot originalBlockSnapshot = AdminUI.originalBlockMap.get(player.getUniqueId());
        Block block = event.getBlock();

        block.setType(originalBlockSnapshot.type());
        block.setBlockData(originalBlockSnapshot.data());

        AdminUI.responseMap.remove(player.getUniqueId());
        AdminUI.originalBlockMap.remove(player.getUniqueId());
    }
}
