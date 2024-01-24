package bash.reactioner.bilang.adapters;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.plugin.Plugin;

public class DisguisedChatAdapter extends PacketAdapter {
    public DisguisedChatAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Server.DISGUISED_CHAT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
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
