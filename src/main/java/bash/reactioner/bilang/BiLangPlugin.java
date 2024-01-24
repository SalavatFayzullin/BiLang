package bash.reactioner.bilang;

import bash.reactioner.bilang.adapters.*;
import bash.reactioner.bilang.papi.BiLangExpansion;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class BiLangPlugin extends JavaPlugin implements Listener {
    private Plugin papi;
    private PlaceholderExpansion expansion;
    private List<PacketAdapter> adapters;
    private Map<String, Map<String, String>> placeholders;
    private String defaultLocale;
    private Scoreboard dummy;
    private SystemChatAdapter systemChatAdapter;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        dummy = Bukkit.getScoreboardManager().getNewScoreboard();
        papi = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null) {
            expansion = new BiLangExpansion(this);
            expansion.register();
        }
        adapters = new LinkedList<>();
        systemChatAdapter = new SystemChatAdapter(this);
        adapters.add(systemChatAdapter);
        adapters.add(new DisguisedChatAdapter(this));
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
    }

    @EventHandler
    private void onChange(PlayerLocaleChangeEvent e) {
        Bukkit.getScheduler().runTask(this, () -> {
            updateData(e.getPlayer());
            systemChatAdapter.sendMessages(e.getPlayer());
        });
    }

    private void updateData(Player p) {
        Scoreboard old = p.getScoreboard();
        p.setScoreboard(dummy);
        p.setScoreboard(old);
        Component header = p.playerListHeader(), footer = p.playerListFooter();
        p.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
        p.sendPlayerListHeaderAndFooter(header, footer);
        int renderDistance = p.getViewDistance() * 16;
        p.getNearbyEntities(renderDistance, renderDistance, renderDistance).forEach(entity -> {
            if (entity.customName() == null) return;
            entity.customName(entity.customName());
        });
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        systemChatAdapter.addPlayer(e.getPlayer());
        e.getPlayer().sendMessage("%bilang_some-greeting-message%");
        e.getPlayer().sendPlayerListHeaderAndFooter(Component.text("%bilang_some-greeting-message%"), Component.text("%bilang_some-greeting-message%"));
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
    }
}
