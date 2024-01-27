package bash.reactioner.bilang.adapters;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.plugin.Plugin;

public class TablistAdapter extends PacketAdapter {
    public TablistAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        for (int i = 0; i < event.getPacket().getChatComponents().size(); i++) {
            WrappedChatComponent component = event.getPacket().getChatComponents().read(i);
            String content = component.getJson();
            content = PlaceholderAPI.setPlaceholders(event.getPlayer(), content);
            event.getPacket().getChatComponents().write(i, WrappedChatComponent.fromJson(content));
        }
    }
}
