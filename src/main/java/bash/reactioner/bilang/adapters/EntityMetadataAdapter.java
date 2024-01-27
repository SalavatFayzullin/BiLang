package bash.reactioner.bilang.adapters;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class EntityMetadataAdapter extends PacketAdapter {
    public EntityMetadataAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Server.ENTITY_METADATA);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        for (int j = 0; j < event.getPacket().getWatchableCollectionModifier().size(); j++) {
            List<WrappedWatchableObject> objects = event.getPacket().getWatchableCollectionModifier().read(j);
            for (int i = 0; i < objects.size(); i++) {
                WrappedWatchableObject object = objects.get(i);
                if (object.getIndex() != 2) continue;
                Optional op = (Optional) objects.get(i).getValue();
                if (!op.isPresent()) continue;
                String json = PlaceholderAPI.setPlaceholders(event.getPlayer(), ((WrappedChatComponent) op.get()).getJson());
                object.setValue(Optional.of(WrappedChatComponent.fromJson(json)));
            }
        }

    }
}
