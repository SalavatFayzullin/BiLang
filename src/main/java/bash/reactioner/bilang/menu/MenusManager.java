package bash.reactioner.bilang.menu;

import bash.reactioner.bilang.BiLangPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenusManager implements Listener {
    private Inventory menu = Bukkit.createInventory(null, 9, "Choose a laguage");
    private BiLangPlugin main;

    public MenusManager(BiLangPlugin main) {
        this.main = main;
        for (int i = 0; i < main.getLanguages().size(); i++) {
            ItemStack item = new ItemStack(Material.SLIME_BALL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(main.getLanguages().get(i));
            item.setItemMeta(meta);
            menu.setItem(i, item);
        }
    }

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        if (e.getInventory() != menu || e.getCurrentItem() == null) return;
        e.setCancelled(true);
        for (int i = 0; i < main.getLanguages().size(); i++) {
            if (!e.getCurrentItem().equals(menu.getItem(i))) continue;
            if (main.getLanguages().get(i).equals(main.getLocale(e.getWhoClicked().getName()))) {
                e.getWhoClicked().sendMessage("You already have chosen language " + main.getLanguages().get(i));
            } else {
                main.changeLanguage((Player) e.getWhoClicked(), main.getLanguages().get(i));
                e.getWhoClicked().sendMessage("You successfully changed the language to " + main.getLanguages().get(i));
            }
            break;
        }
    }

    public void open(Player p) {
        p.openInventory(menu);
    }
}
