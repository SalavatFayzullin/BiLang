package bash.reactioner.bilang.adapters;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class NpcAdapter extends PacketAdapter {
    public NpcAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Server.PLAYER_INFO);
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        for (int i = 0; i < e.getPacket().getPlayerInfoDataLists().size(); i++) {
            List<PlayerInfoData> datas = new ArrayList<>();
            for (int j = 0; j < e.getPacket().getPlayerInfoDataLists().read(i).size(); j++) {
                PlayerInfoData data = e.getPacket().getPlayerInfoDataLists().read(i).get(j);
                String name = PlaceholderAPI.setPlaceholders(e.getPlayer(), data.getProfile().getName());
                PlayerInfoData newData = new PlayerInfoData(data.getProfile().withName(name), data.getLatency(), data.getGameMode(), data.getDisplayName());
                datas.add(newData);
            }
            e.getPacket().getPlayerInfoDataLists().write(i, datas);
        }
    }
}
