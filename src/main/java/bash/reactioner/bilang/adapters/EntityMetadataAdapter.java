package bash.reactioner.bilang.adapters;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityMetadataAdapter extends PacketAdapter {
    public EntityMetadataAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Server.ENTITY_METADATA);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        for (int i = 0; i < event.getPacket().getDataValueCollectionModifier().size(); i++) {
            for (int j = 0; j < event.getPacket().getDataValueCollectionModifier().read(i).size(); j++) {
                if (event.getPacket().getDataValueCollectionModifier().read(i).get(j).getIndex() == 2) {
                    Optional<?> optional = (Optional<?>) event.getPacket().getDataValueCollectionModifier().read(i).get(j).getValue();
                    if (optional.isPresent() && optional.get() instanceof WrappedChatComponent name) {
                        String content = name.getJson();
                        content = PlaceholderAPI.setPlaceholders(event.getPlayer(), content);
                        event.getPacket().getDataValueCollectionModifier().read(i).get(j).setValue(Optional.of(WrappedChatComponent.fromJson(content)));
                    }
                }
            }
        }
    }
}
