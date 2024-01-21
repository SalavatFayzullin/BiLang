package bash.reactioner.ourtriton;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class TritonPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.DISGUISED_CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                String content = event.getPacket().getChatComponents().read(0).getJson();
                content = content.replace("%player_balance%", "Ты лучший");
                event.getPacket().getChatComponents().write(0, WrappedChatComponent.fromJson(content));
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.SYSTEM_CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                String content = PlainTextComponentSerializer.plainText().serialize(event.getPacket().getSpecificModifier(Component.class).read(0));
                content = content.replace("%player_balance%", "Ты пидорас");
                Component component = PlainTextComponentSerializer.plainText().deserialize(content);
                event.getPacket().getSpecificModifier(Component.class).write(0, component);
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                for (int i = 0; i < event.getPacket().getDataValueCollectionModifier().size(); i++) {
                    for (int j = 0; j < event.getPacket().getDataValueCollectionModifier().read(i).size(); j++) {
                        if (event.getPacket().getDataValueCollectionModifier().read(i).get(j).getIndex() == 2) {
                            //getLogger().info("Name change ^_^");
                            Optional<?> optional = (Optional<?>) event.getPacket().getDataValueCollectionModifier().read(i).get(j).getValue();
                            if (optional.isPresent() && optional.get() instanceof WrappedChatComponent name) {
                                String content = name.getJson();
                                content = content.replace("%player_name%", event.getPlayer().getName());
                                event.getPacket().getDataValueCollectionModifier().read(i).get(j).setValue(Optional.of(WrappedChatComponent.fromJson(content)));
                            }
                        }
                    }
                }
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.SCOREBOARD_SCORE) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacket().getStrings().size() == 0) return;
                String score = event.getPacket().getStrings().read(0);
                score = score.replace("%triton_text%", event.getPlayer().getName());
                event.getPacket().getStrings().write(0, score);
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER) {
            @Override
            public void onPacketSending(PacketEvent event) {
                for (int i = 0; i < event.getPacket().getSpecificModifier(Component.class).size(); i++) {
                    Component component = event.getPacket().getSpecificModifier(Component.class).read(i);
                    String content = PlainTextComponentSerializer.plainText().serialize(component);
                    content = content.replace("%triton_text%", event.getPlayer().getName());
                    event.getPacket().getSpecificModifier(Component.class).write(i, PlainTextComponentSerializer.plainText().deserialize(content));
                }
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent e) {
                var data = e.getPacket().getPlayerInfoDataLists().read(1).get(0);
                String name = data.getProfile().getName().replace("a", "o");
                PlayerInfoData newData = new PlayerInfoData(data.getProfile().withName(name), data.getLatency(), data.getGameMode(), data.getDisplayName());
                e.getPacket().getPlayerInfoDataLists().write(1, List.of(newData));
            }
        });
        getServer().getPluginManager().registerEvents(this, this);
//        Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage("%player_balance%")), 0, 5 * 20);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.getObjective("Test");
        if (objective == null) objective = scoreboard.registerNewObjective("Test", Criteria.DUMMY, Component.text("Some cool text"));
        objective.getScore("Hello").setScore(0);
        objective.getScore("Hello1").setScore(1);
        objective.getScore("%triton_text%").setScore(2);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        e.getPlayer().setScoreboard(scoreboard);
        e.getPlayer().sendPlayerListHeaderAndFooter(Component.text("%triton_text%"), Component.text("%triton_text%"));
    }

//    @EventHandler
//    private void onDrop(PlayerDropItemEvent e) {
//        Entity entity = e.getPlayer().getWorld().spawnEntity(e.getPlayer().getLocation(), EntityType.CHICKEN);
//        entity.customName(Component.text(ChatColor.RED + "%player_name%"));
//        entity.setCustomNameVisible(true);
//    }
}
