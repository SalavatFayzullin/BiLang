package bash.reactioner.bilang.adapters;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ScoreboardScoreAdapter extends PacketAdapter {
    public ScoreboardScoreAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Server.SCOREBOARD_SCORE);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        for (int i = 0; i < event.getPacket().getStrings().size(); i++) {
            String score = event.getPacket().getStrings().read(i);
            if (score == null) continue;
            score = PlaceholderAPI.setPlaceholders(event.getPlayer(), score);
            event.getPacket().getStrings().write(i, score);
        }
    }
}
