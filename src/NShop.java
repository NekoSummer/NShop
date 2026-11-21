import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class NShop extends JavaPlugin {

    private static Economy economy = null;
    public String menuTitle =
            ChatColor.translateAlternateColorCodes('&', "&l&7NShop");

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("No Vault Plugin!!!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new NShopListen(this, economy), this);
        getLogger().info("NShop插件已加载");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "请在游戏中使用命令");
            return true;
        }

        Player player = (Player) sender;

        Inventory gui = Bukkit.createInventory(null, 9, menuTitle);

        ConfigurationSection itemsSection = getConfig().getConfigurationSection("items");
        if (itemsSection == null)
            return true;

        for (String key : itemsSection.getKeys(false)) {
            String path = "items." + key + ".";
            int slot = getConfig().getInt(path + "slot");

            Material mate = Material.matchMaterial(getConfig().getString(path + "mate", "STONE"));
            if (mate == null)
                continue;

            int amount = getConfig().getInt(path + "amount", 1);
            ItemStack item = new ItemStack(mate, amount);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                String name = ChatColor.translateAlternateColorCodes('&', getConfig().getString(path + "name", ""));
                meta.setDisplayName(name);

                List<String> loreConf = getConfig().getStringList(path + "lore");
                List<String> lore = new ArrayList<>();

                for (String line: loreConf)
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));

                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(slot, item);
        }

        player.openInventory(gui);
        return true;
    }
}
