import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class NShopListen implements Listener {
    private final NShop plugin;
    private final Economy economy;

    public NShopListen(NShop pluginMain, Economy economyApi) {
        plugin = pluginMain;
        economy = economyApi;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(plugin.menuTitle))
            return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        int guiSize = event.getInventory().getSize();

        if (event.getRawSlot() >= guiSize) return;

        int clickedSlot = event.getSlot();

        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("items");

        if (itemsSection == null)
            return;

        for (String key: itemsSection.getKeys(false)) {
            String path = "items." + key + ".";
            int ConfSlot = plugin.getConfig().getInt(path + "slot");

            if (ConfSlot == clickedSlot) {
                double price = plugin.getConfig().getDouble(path + "price");
                String mateName = plugin.getConfig().getString(path + "material", "STONE");
                int amount = plugin.getConfig().getInt(path + "amount", 1);
                Material mate = Material.matchMaterial(mateName);

                if (mate == null)
                    return;

                if (economy.has(player, price)) {
                    economy.withdrawPlayer(player, price);

                    ItemStack buyItem = new ItemStack(mate, amount);
                    if (player.getInventory().firstEmpty() == -1) {
                        player.getWorld().dropItem(player.getLocation(), buyItem);
                        player.sendMessage(ChatColor.YELLOW + "背包已满，物品掉落在你的脚下！");
                    } else {
                        player.getInventory().addItem(buyItem);
                    }

                    player.sendMessage(ChatColor.GREEN + "购买成功！花费了 " + price + " 金币。");
                    // 播放个音效增加手感 (可选)
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                } else {
                    player.sendMessage(ChatColor.RED + "你的钱不够！需要 " + price + " 金币。");
                    // 播放失败音效
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
                return;
            }
        }
    }
}
