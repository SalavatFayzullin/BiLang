package bash.reactioner.bilang.papi;

import bash.reactioner.bilang.BiLangPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BiLangExpansion extends PlaceholderExpansion {
    private BiLangPlugin main;

    public BiLangExpansion(BiLangPlugin main) {
        this.main = main;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bilang";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Reactioner";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player p, @NotNull String params) {
        if (p == null) return null;
        String locale = main.getLocale(p.getName());
        Map<String, String> localedMessages = main.getPlaceholders().get(params);
        String message = localedMessages.getOrDefault(locale, localedMessages.get(main.getDefaultLocale()));
        return message;
    }
}
