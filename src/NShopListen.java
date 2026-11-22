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
        // 去除颜色码对比标题，防止颜色字符导致判断失败
        String viewTitle = ChatColor.stripColor(event.getView().getTitle());
        String menuTitlePlain = ChatColor.stripColor(plugin.menuTitle);
        if (!viewTitle.equals(menuTitlePlain)) {
            return;
        }

        // 核心：取消事件，禁止拖拽、取出、移动GUI物品
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int guiSize = event.getInventory().getSize();
        int rawSlot = event.getRawSlot();

        // 点击位置在玩家背包区域，直接返回，不处理购买
        if (rawSlot >= guiSize) {
            return;
        }

        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("items");
        if (itemsSection == null) {
            return;
        }

        for (String key : itemsSection.getKeys(false)) {
            String path = "items." + key + ".";
            int confSlot = plugin.getConfig().getInt(path + "slot");

            if (confSlot == rawSlot) {

                double price = plugin.getConfig().getDouble(path + "price");
                String mateName = plugin.getConfig().getString(path + "mate", "STONE");
                if (mateName != null) mateName = mateName.toUpperCase();
                Material mate = Material.matchMaterial(mateName);
                plugin.getLogger().info("购买商品key:"+key+" | path:"+path+" | material读取值:"+mateName);
                int amount = plugin.getConfig().getInt(path + "amount", 1);

                if (mate == null) {
                    plugin.getLogger().warning("材质 ["+mateName+"] 无效！");
                    break; // 材质无效，跳出循环，不要return
                }

                if (economy.has(player, price)) {
                    economy.withdrawPlayer(player, price);
                    ItemStack buyItem = new ItemStack(mate, amount);

                    if (player.getInventory().firstEmpty() == -1) {
                        player.getWorld().dropItem(player.getLocation(), buyItem);
                        player.sendMessage(ChatColor.YELLOW + "背包已满，物品掉落在你的脚下！");
                    } else {
                        player.getInventory().addItem(buyItem);
                    }
                    player.sendMessage(ChatColor.GREEN + "购买成功！花费了 " + price);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                } else {
                    player.sendMessage(ChatColor.RED + "你的钱不够！需要 " + price);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
                return;
            }
        }
    }
}
