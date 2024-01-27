package bash.reactioner.bilang;

import bash.reactioner.bilang.adapters.*;
import bash.reactioner.bilang.data.LanguageData;
import bash.reactioner.bilang.data.LanguageRepository;
import bash.reactioner.bilang.data.MySqlLanguageRepository;
import bash.reactioner.bilang.menu.MenusManager;
import bash.reactioner.bilang.papi.BiLangExpansion;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;

public class BiLangPlugin extends JavaPlugin implements Listener {
    private Plugin papi;
    private PlaceholderExpansion expansion;
    private List<PacketAdapter> adapters;
    private Map<String, Map<String, String>> placeholders;
    private String defaultLocale;
    private Scoreboard dummy;
    private ChatAdapter chatAdapter;
    private String url;
    private String username;
    private String password;
    private String createSql;
    private String readSql;
    private String updateSql;
    private String deleteSql;
    private LanguageRepository repository;
    private Map<String, String> locales;
    private Map<String, LanguageData> languages;
    private MenusManager menusManager;
    private String createTableSql;
    private boolean shouldCreateTableIfNotExists;
    private Set<Player> cooldowns;
    private boolean checkPermission;
    private String permission;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        locales = new HashMap<>();
        dummy = Bukkit.getScoreboardManager().getNewScoreboard();
        papi = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null) {
            expansion = new BiLangExpansion(this);
            expansion.register();
        }
        adapters = new LinkedList<>();
        chatAdapter = new ChatAdapter(this);
        adapters.add(chatAdapter);
        adapters.add(new EntityMetadataAdapter(this));
        adapters.add(new ScoreboardScoreAdapter(this));
        adapters.add(new NpcAdapter(this));
        adapters.add(new TablistAdapter(this));
        for (PacketAdapter adapter : adapters) ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
        placeholders = new HashMap<>();
        ConfigurationSection localedPlaceholders = getConfig().getConfigurationSection("placeholders");
        localedPlaceholders.getKeys(false).forEach(placeholder -> {
            Map<String, String> localed = new HashMap<>();
            ConfigurationSection message = localedPlaceholders.getConfigurationSection(placeholder);
            message.getKeys(false).forEach(concreteLanguage -> localed.put(concreteLanguage.toLowerCase(), message.getString(concreteLanguage)));
            placeholders.put(placeholder, localed);
        });
        defaultLocale = getConfig().getString("default-locale");
        getServer().getPluginManager().registerEvents(this, this);
        createSql = getConfig().getString("sql-queries.create");
        readSql = getConfig().getString("sql-queries.read");
        updateSql = getConfig().getString("sql-queries.update");
        deleteSql = getConfig().getString("sql-queries.delete");
        url = getConfig().getString("db.url");
        username = getConfig().getString("db.username");
        password = getConfig().getString("db.password");
        try {
            repository = new MySqlLanguageRepository(this, url, username, password);
        } catch (SQLException e) {
            getLogger().severe("Please, configure database connection config!");
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        languages = new HashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection("languages");
        section.getKeys(false).forEach(lang -> {
            ConfigurationSection section1 = section.getConfigurationSection(lang);
            languages.put(lang, new LanguageData(section1.getString("display-name"), section1.getString("value"), section1.getString("signature")));
        });
        menusManager = new MenusManager(this);
        getServer().getPluginManager().registerEvents(menusManager, this);
        getCommand("lang").setExecutor(this);
        createTableSql = getConfig().getString("sql-queries.create-table");
        shouldCreateTableIfNotExists = getConfig().getBoolean("create-table-if-does-not-exist");
        cooldowns = new HashSet<>();
        permission = getConfig().getString("lang-command.permission");
        checkPermission = getConfig().getBoolean("lang-command.check-permission");
    }

    public String getCreateTableSql() {
        return createTableSql;
    }

    public boolean isShouldCreateTableIfNotExists() {
        return shouldCreateTableIfNotExists;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command");
            return true;
        }
        Player p = (Player) sender;
        if (checkPermission && !p.hasPermission(permission)) {
            p.sendMessage("You have not enough permissions");
            return true;
        }
        if (cooldowns.contains(p)) sender.sendMessage("You cannot execute this command so frequently");
        else menusManager.open(p);
        return true;
    }

    public void changeLanguage(Player p, String locale) {
        setLocale(p.getName(), locale);
        repository.update(p.getName(), locale);
        updateData(p);
        cooldowns.add(p);
        Bukkit.getScheduler().runTaskLater(this, () -> cooldowns.remove(p), 20 * 3);
        p.closeInventory();
    }

    public Map<String, LanguageData> getLanguages() {
        return languages;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCreateSql() {
        return createSql;
    }

    public String getReadSql() {
        return readSql;
    }

    public String getUpdateSql() {
        return updateSql;
    }

    public String getDeleteSql() {
        return deleteSql;
    }

    @EventHandler
    private void onChange(PlayerLocaleChangeEvent e) {
        Bukkit.getScheduler().runTask(this, () -> {
            updateData(e.getPlayer());
            chatAdapter.sendMessages(e.getPlayer());
        });
    }

    private void updateData(Player p) {
        Scoreboard old = p.getScoreboard();
        p.setScoreboard(dummy);
        p.setScoreboard(old);
        String header = p.getPlayerListHeader(), footer = p.getPlayerListFooter();
        p.setPlayerListHeaderFooter("", "");
        p.setPlayerListHeaderFooter(header, footer);
        int renderDistance = p.getClientViewDistance() * 16;
        p.getNearbyEntities(renderDistance, renderDistance, renderDistance).forEach(entity -> {
            if (entity.getCustomName() == null) return;
            String temp = entity.getCustomName();
            entity.setCustomName("");
            entity.setCustomName(temp);
        });
    }

    public String getLocale(String playerName) {
        return locales.get(playerName);
    }

    public void setLocale(String playerName, String locale) {
        locales.put(playerName, locale);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onJoin(PlayerJoinEvent e) {
        chatAdapter.addPlayer(e.getPlayer());

//        e.getPlayer().sendMessage(ChatColor.RED + "%bilang_some-greeting-message%");
//        e.getPlayer().setPlayerListHeaderFooter(ChatColor.RED + "%bilang_some-greeting-message%", ChatColor.RED + "%bilang_some-greeting-message%");

        Optional<String> locale = repository.read(e.getPlayer().getName());
        if (!locale.isPresent()) locales.put(e.getPlayer().getName(), repository.create(e.getPlayer().getName()));
        else locales.put(e.getPlayer().getName(), locale.get());
    }

    public Map<String, Map<String, String>> getPlaceholders() {
        return placeholders;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    @Override
    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
        if (repository != null) {
            try {
                repository.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
