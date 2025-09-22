package Admin.AdminSystem;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;


import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;


import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminUI implements Listener {
    static final Map<UUID, UUID> responseMap = new HashMap<UUID, UUID>();
    static final Map<UUID, Block> originalBlockMap = new HashMap<>();
    private Plugin plugin;

    public AdminUI(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta())
            return;

        e.setCancelled(true);

        ItemMeta itemMeta = clickedItem.getItemMeta();
        String helpPlayerName = ChatColor.stripColor(itemMeta.getDisplayName());

        if (e.getView().getTitle().equals("Help Center")) {
            Player target = Bukkit.getPlayer(helpPlayerName);

            if (target == null) {
                player.sendMessage(ChatColor.ITALIC + "" + ChatColor.RED + "Player is offline");
            }

            if (e.getClick() == ClickType.MIDDLE) {
                removeRequest(e, player, target);
                return;
            }

            if (e.isRightClick()) {
                openMessager(player, target);
                return;
            }

            switch (clickedItem.getType()){

                case PLAYER_HEAD:
                    if (player == target) {
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "Can't help self");
                        e.setCancelled(true);
                        return;
                    }

                    player.teleport(target);

                    player.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "Now helping " + helpPlayerName);
                    target.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "You are being helped by " + player.getName());

                    target.getPersistentDataContainer().set(new NamespacedKey("request", "colour"), PersistentDataType.STRING, ChatColor.GREEN.name());

                    BukkitScheduler scheduler = Bukkit.getScheduler();

                    scheduler.runTaskLater(plugin, () -> removeRequest(e, player, target), 400);
                    break;
            }

        }
    }

    private void openMessager(Player player, Player target) {
        Block block = player.getLocation().getBlock();

        originalBlockMap.put(player.getUniqueId(), block);


        block.setType(Material.OAK_SIGN);
        Sign sign = (Sign) block.getState();
        sign.update();

        responseMap.put(player.getUniqueId(), target.getUniqueId());
        player.openSign(sign);
    }

    private void removeRequest(InventoryClickEvent e, Player player, Player helpPlayer) {
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You have removed the request from: " + helpPlayer.getName());

        if (ChatColor.valueOf(helpPlayer.getPersistentDataContainer().get(new NamespacedKey("request", "colour"), PersistentDataType.STRING).toUpperCase()) != ChatColor.GREEN)
            helpPlayer.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC +  "Your request has been denied");

        HelpCommand.AdminData.removeValues(helpPlayer);

        if (e.getCurrentItem() != null)
            e.getClickedInventory().setItem(e.getSlot(), null);
    }
}
