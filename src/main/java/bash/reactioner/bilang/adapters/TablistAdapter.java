package bash.reactioner.bilang.adapters;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.plugin.Plugin;

public class TablistAdapter extends PacketAdapter {
    public TablistAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        for (int i = 0; i < event.getPacket().getSpecificModifier(Component.class).size(); i++) {
            Component component = event.getPacket().getSpecificModifier(Component.class).read(i);
            String content = PlainTextComponentSerializer.plainText().serialize(component);
            content = PlaceholderAPI.setPlaceholders(event.getPlayer(), content);
            event.getPacket().getSpecificModifier(Component.class).write(i, PlainTextComponentSerializer.plainText().deserialize(content));
        }
    }
}
