package bash.reactioner.bilang.menu;

import bash.reactioner.bilang.BiLangPlugin;
import bash.reactioner.bilang.data.LanguageData;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.security.KeyStore;
import java.util.Map;
import java.util.UUID;

public class MenusManager implements Listener {
    private Inventory menu = Bukkit.createInventory(null, 9, "Choose a laguage");
    private BiLangPlugin main;

    public MenusManager(BiLangPlugin main) {
        this.main = main;
        int i = 0;
        for (Map.Entry<String, LanguageData> entry : main.getLanguages().entrySet()) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", entry.getValue().getValue(), entry.getValue().getSignature()));
            meta.setDisplayName(entry.getValue().getDisplayName());
            meta.setPlayerProfile(profile);
            item.setItemMeta(meta);
            menu.setItem(i++, item);
        }
    }

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        if (e.getInventory() != menu || e.getCurrentItem() == null) return;
        e.setCancelled(true);
        for (Map.Entry<String, LanguageData> entry : main.getLanguages().entrySet()) {
            if (!entry.getValue().getDisplayName().equals(e.getCurrentItem().getItemMeta().getDisplayName())) continue;
            if (entry.getKey().equals(main.getLocale(e.getWhoClicked().getName()))) {
                e.getWhoClicked().sendMessage("You already have chosen language " + entry.getKey());
            } else {
                main.changeLanguage((Player) e.getWhoClicked(), entry.getKey());
                e.getWhoClicked().sendMessage("You successfully changed the language to " + entry.getKey());
            }
            break;
        }
    }

    public void open(Player p) {
        p.openInventory(menu);
    }
}
