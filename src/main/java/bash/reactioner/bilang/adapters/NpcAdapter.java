package bash.reactioner.bilang.adapters;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class NpcAdapter extends PacketAdapter {
    public NpcAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Server.PLAYER_INFO);
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        var data = e.getPacket().getPlayerInfoDataLists().read(1).get(0);
        String name = PlaceholderAPI.setPlaceholders(e.getPlayer(), data.getProfile().getName());
        PlayerInfoData newData = new PlayerInfoData(data.getProfile().withName(name), data.getLatency(), data.getGameMode(), data.getDisplayName());
        e.getPacket().getPlayerInfoDataLists().write(1, List.of(newData));
    }
}
