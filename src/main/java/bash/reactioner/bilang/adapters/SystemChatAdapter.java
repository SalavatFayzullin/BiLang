package bash.reactioner.bilang.adapters;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class SystemChatAdapter extends PacketAdapter {
    private Map<Player, List<PacketContainer>> initialPacketsToSend = new HashMap<>();
    private Set<Player> playersToPreservePacketSending = new HashSet<>();

    public SystemChatAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Server.SYSTEM_CHAT);
    }

    public void addPlayer(Player p) {
        playersToPreservePacketSending.add(p);
    }

    public void sendMessages(Player p) {
        List<PacketContainer> packets = initialPacketsToSend.get(p);
        if (packets == null) return;
        playersToPreservePacketSending.remove(p);
        packets.forEach(packet -> ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet));
        initialPacketsToSend.remove(p);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (playersToPreservePacketSending.contains(event.getPlayer())) {
            List<PacketContainer> packets = initialPacketsToSend.get(event.getPlayer());
            if (packets == null) {
                packets = new ArrayList<>();
                initialPacketsToSend.put(event.getPlayer(), packets);
            }
            packets.add(event.getPacket());
            event.setCancelled(true);
            return;
        }
        if (event.getPacket().getChatComponents().size() > 0) {
            String content = event.getPacket().getChatComponents().read(0).getJson();
            content = PlaceholderAPI.setPlaceholders(event.getPlayer(), content);
            event.getPacket().getChatComponents().write(0, WrappedChatComponent.fromJson(content));
        } else {
            String content = JSONComponentSerializer.json().serialize(event.getPacket().getSpecificModifier(Component.class).read(0));
            content = PlaceholderAPI.setPlaceholders(event.getPlayer(), content);
            event.getPacket().getSpecificModifier(Component.class).write(0, JSONComponentSerializer.json().deserialize(content));
        }
    }
}
