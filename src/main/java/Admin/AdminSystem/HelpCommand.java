package Admin.AdminSystem;

import net.md_5.bungee.api.ChatColor;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Optional;

public class HelpCommand implements CommandExecutor {
    private final String[] helpLore = {"left:accept", "right:reply", "shift:delete"};

    public static class AdminData {
        public static void removeValues(Player player) {
            player.getPersistentDataContainer().remove(new NamespacedKey("request", "message"));
            player.getPersistentDataContainer().remove(new NamespacedKey("request", "colour"));

        }

        public static void sendToAdmins(String message) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.isOp())
                    continue;

                TextComponent adminMessage = new TextComponent(message);
                adminMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/needadmin queue"));

                adminMessage.setItalic(true);
                adminMessage.setColor(ChatColor.GRAY);

                player.spigot().sendMessage(adminMessage);
            }
        }

        public static boolean clearRequests() {
            boolean isEmpty = true;

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getPersistentDataContainer().has(new NamespacedKey("request", "colour"))) {
                    if (ChatColor.valueOf(player.getPersistentDataContainer().get(new NamespacedKey("request", "colour"), PersistentDataType.STRING).toUpperCase()) == ChatColor.RED)
                        player.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "Your request has been denied");
                    isEmpty = false;
                    removeValues(player);
                }
            }

            return isEmpty;
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player commandPlayer = (Player) commandSender;

            if (strings.length == 0) {
                viewQueue(commandPlayer);
                return true;
            }

            switch (strings[0]) {
                case "request":
                    requestAdmin(commandPlayer, strings);
                    break;
                case "cancel":
                    cancelRequest(commandPlayer);
                    break;
                case "help":
                    helpMessage(commandPlayer);
                    break;
                case "clear":
                    clearRequestsCommand(commandPlayer);
                    break;
                default:
                    viewQueue(commandPlayer);
            }
        }

        return true;
    }

    private void requestAdmin(Player commandPlayer, String[] strings) {
        String message = "";

        for (int i = 1; i < strings.length; i++)
            message += strings[i] + " ";

        if (message.length() > 50)
            message = message.substring(0, 46);

        message = ChatColor.AQUA + message;

        commandPlayer.getPersistentDataContainer().set(new NamespacedKey("request", "message"), PersistentDataType.STRING, message);
        commandPlayer.getPersistentDataContainer().set(new NamespacedKey("request", "colour"), PersistentDataType.STRING, ChatColor.RED.getName());

        commandPlayer.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "Your request has been sent");
        AdminData.sendToAdmins(commandPlayer.getName() + " has sent a request for help");
    }

    private void clearRequestsCommand(Player commandPlayer) {
        if (!commandPlayer.isOp()) {
            commandPlayer.sendMessage(ChatColor.RED + "Only Admins are able to use this command");
            return;
        }

        if (AdminData.clearRequests())
            commandPlayer.sendMessage(ChatColor.RED + "Queue is empty");
        else
            commandPlayer.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "All requests have been cleared");

    }

    private void cancelRequest(Player commandPlayer) {
        if (!commandPlayer.getPersistentDataContainer().has(new NamespacedKey("request", "colour"))) {
            commandPlayer.sendMessage(ChatColor.RED + "You have no requests sent");
            return;
        }


        AdminData.removeValues(commandPlayer);
        commandPlayer.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "Your request has been cancelled");
        AdminData.sendToAdmins(commandPlayer.getName() + " has cancelled their request for help");
    }

    private void helpMessage(Player commandPlayer) {
        commandPlayer.sendMessage(ChatColor.AQUA + "Commands: " +
                "\n - request <message> -> sends a message to the admin team " +
                "\n - cancel -> cancels request sent " +
                "\n - queue -> allows admins to see all requests sent" +
                "\nIn Queue Inventory (for Admins): " +
                "\n - left click -> accepts request and teleports to request sender" +
                "\n - right click -> replies to request" +
                "\n - shift + right click -> deletes request");
    }

    private void viewQueue(Player commandPlayer) {
        if (!commandPlayer.isOp()) {
            commandPlayer.sendMessage(ChatColor.RED + "Only Admins are able to use this command");
            return;
        }

        Inventory AdminUI = Bukkit.createInventory(commandPlayer, 54, "Help Center");

        int index = 0;
        boolean isEmpty = true;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getPersistentDataContainer().has(new NamespacedKey("request", "colour"))) {
                ItemStack playerIcon = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta playerMeta = playerIcon.getItemMeta();

                SkullMeta skullMeta = (SkullMeta) playerMeta;
                skullMeta.setOwningPlayer(player);
                playerIcon.setItemMeta(skullMeta);

                playerMeta.setDisplayName(ChatColor.valueOf(player.getPersistentDataContainer().get(new NamespacedKey("request", "colour"), PersistentDataType.STRING).toUpperCase()) + player.getName());

                Optional.ofNullable(
                        player.getPersistentDataContainer().get(
                                new NamespacedKey("request", "message"),
                                PersistentDataType.STRING
                        )
                ).ifPresent(msg -> {
                    playerMeta.setLore(List.of(
                            ChatColor.AQUA + msg,
                            "",
                            ChatColor.GOLD + "" + ChatColor.BOLD + helpLore[0],
                            ChatColor.GOLD + "" + ChatColor.BOLD + helpLore[1],
                            ChatColor.GOLD + "" + ChatColor.BOLD + helpLore[2]
                            ));
                });


                playerIcon.setItemMeta(playerMeta);

                AdminUI.setItem(index, playerIcon);
                index++;
                isEmpty = false;
            }
        }
        if (isEmpty) {
            commandPlayer.sendMessage(ChatColor.RED + "Queue is empty");
            return;
        }

        commandPlayer.openInventory(AdminUI);
    }
}
