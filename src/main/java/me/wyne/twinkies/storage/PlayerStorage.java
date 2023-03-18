package me.wyne.twinkies.storage;

import com.google.gson.*;
import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.wlog.WLog;
import me.wyne.twinkies.wstorage.JsonStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class PlayerStorage extends JsonStorage {

    private final HashMap<UUID, String> playerLastNickname = new HashMap<>();
    public HashMap<UUID, String> playerLastNickname() {
        return playerLastNickname;
    }
    private final HashMap<UUID, Set<String>> playerNicknames = new HashMap<>();
    public HashMap<UUID, Set<String>> playerNicknames() {
        return playerNicknames;
    }
    private final HashMap<UUID, String> playerLastIp = new HashMap<>();
    public HashMap<UUID, String> playerLastIp() {
        return playerLastIp;
    }
    private final HashMap<UUID, Set<String>> playerIps = new HashMap<>();
    public HashMap<UUID, Set<String>> playerIps() {
        return playerIps;
    }

    public PlayerStorage(@NotNull final Twinkies plugin)
    {
        super(plugin, "playerData.json");
    }

    public boolean isPlayerSaved(@NotNull final OfflinePlayer player)
    {
        if (!playerLastNickname.containsKey(player.getUniqueId()) || playerLastNickname.get(player.getUniqueId()).isEmpty())
            return false;
        if (!playerNicknames.containsKey(player.getUniqueId()) || playerNicknames.get(player.getUniqueId()).isEmpty())
            return false;
        if (!playerLastIp.containsKey(player.getUniqueId()) || playerLastIp.get(player.getUniqueId()).isEmpty())
            return false;
        if (!playerIps.containsKey(player.getUniqueId()) || playerIps.get(player.getUniqueId()).isEmpty())
            return false;
        return true;
    }

    public void loadData()
    {
        executorService.execute(() -> {
            try {
                WLog.info("Загрузка данных из файла '" + storageFile.getName() + "'...");
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                for (Map.Entry<String, JsonElement> playerObject : playerObjects.entrySet())
                {
                    Set<String> newPlayerNicknames = new HashSet<>();
                    Set<String> newPlayerIps = new HashSet<>();
                    for (JsonElement playerNicknameJson : playerObject.getValue().getAsJsonObject().getAsJsonArray("nicknames"))
                    {
                        newPlayerNicknames.add(playerNicknameJson.getAsString());
                    }
                    for (JsonElement playerIpJson : playerObject.getValue().getAsJsonObject().getAsJsonArray("ips"))
                    {
                        newPlayerIps.add(playerIpJson.getAsString());
                    }

                    if (playerObject.getValue().getAsJsonObject().has("last-nickname"))
                        playerLastNickname.put(UUID.fromString(playerObject.getKey()), playerObject.getValue().getAsJsonObject().get("last-nickname").getAsString());
                    playerNicknames.put(UUID.fromString(playerObject.getKey()), newPlayerNicknames);
                    if (playerObject.getValue().getAsJsonObject().has("last-ip"))
                        playerLastIp.put(UUID.fromString(playerObject.getKey()), playerObject.getValue().getAsJsonObject().get("last-ip").getAsString());
                    playerIps.put(UUID.fromString(playerObject.getKey()), newPlayerIps);
                }
                WLog.info("Данные из файла '" + storageFile.getName() + "' загружены");
            } catch (FileNotFoundException e) {
                WLog.error("Произошла ошибка при загрузке данных из файла '" + storageFile.getName() + "'");
                WLog.error(e.getMessage());
            }
        });
    }

    @NotNull
    public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return List.of();

        List<String> result = new ArrayList<>();

        if (args.length == 1)
        {
            result.add("data");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("data"))
        {
            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
            {
                if (args[1].isBlank())
                    result.add(player.getName());
                else
                {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        result.add(player.getName());
                }
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("data"))
        {
            if (Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore())
            {
                if (playerNicknames.containsKey(Bukkit.getOfflinePlayer(args[1]).getUniqueId()))
                    result.addAll(playerNicknames.get(Bukkit.getOfflinePlayer(args[1]).getUniqueId()));

                if (playerIps.containsKey(Bukkit.getOfflinePlayer(args[1]).getUniqueId()))
                    result.addAll(playerIps.get(Bukkit.getOfflinePlayer(args[1]).getUniqueId()));
            }
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("data"))
        {
            result.add("find");
            if (sender.hasPermission("twinkies.playerDataMod"))
                result.add("delete");
        }

        return result;
    }

    public void showDataManager(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return;
        if (args.length != 2)
            return;
        if (!args[0].equalsIgnoreCase("data"))
            return;
        if (!Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore())
        {
            sender.sendMessage(Component.text("Игрок '").append(Component.text(args[1]).append(Component.text("' не найден!"))).color(NamedTextColor.RED));
            WLog.error("Произошла ошибка при попытке получить данные о игроке '" + args[1] + "'");
            if (sender instanceof Player)
                WLog.error("Игрок: '" + sender.getName() + "'");
            else
                WLog.error("Запрос был выполнен из консоли");
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

        Component playerInfo = Component.text("Информация о игроке '")
                .append(Component.text(player.getName()))
                .append(Component.text("'"))
                .appendNewline()
                .append(Component.text("Никнеймы:"))
                .color(NamedTextColor.BLUE)
                .appendNewline();

        if (playerNicknames.containsKey(player.getUniqueId()))
        {
            for (String nickname : playerNicknames.get(player.getUniqueId()))
            {
                playerInfo = playerInfoAppend(playerInfo, args[1], nickname, "никнейм");
                playerInfo = playerInfo.appendNewline();
            }
        }

        playerInfo = playerInfo.appendNewline().append(Component.text("IP адреса:")).color(NamedTextColor.BLUE).appendNewline();

        if (playerIps.containsKey(player.getUniqueId()))
        {
            for (String ip : playerIps.get(player.getUniqueId()))
            {
                playerInfo = playerInfoAppend(playerInfo, args[1], ip, "IP адрес");
                playerInfo = playerInfo.appendNewline();
            }
        }

        playerInfo = playerInfo.appendNewline();

        if (playerLastNickname.containsKey(player.getUniqueId()))
        {
            playerInfo = playerInfo.append(Component.text("Последний никнейм: ").color(NamedTextColor.BLUE));
            playerInfo = playerInfoAppend(playerInfo, args[1], playerLastNickname.get(player.getUniqueId()), "никнейм");
            playerInfo = playerInfo.appendNewline();
        }

        if (playerLastIp.containsKey(player.getUniqueId()))
        {
            playerInfo = playerInfo.append(Component.text("Последний IP адрес: ").color(NamedTextColor.BLUE));
            playerInfo = playerInfoAppend(playerInfo, args[1], playerLastIp.get(player.getUniqueId()), "IP адрес");
        }

        sender.sendMessage(playerInfo);
    }

    private Component playerInfoAppend(@NotNull final Component playerInfo, @NotNull final String playerNick, @NotNull final String append, @NotNull final String hoverEnd)
    {
        return playerInfo.append(Component.text(append).color(NamedTextColor.WHITE))
                .appendSpace()
                .append(Component.text("[✔]").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы найти возможные твинки по этому " + hoverEnd + "у").color(NamedTextColor.GREEN)))
                        .clickEvent(ClickEvent.suggestCommand("/twinkies data " + playerNick + " " + append + " find")))
                .appendSpace()
                .append(Component.text("[✖]").decorate(TextDecoration.BOLD).color(NamedTextColor.RED)
                        .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы удалить этот " + hoverEnd).color(NamedTextColor.RED)))
                        .clickEvent(ClickEvent.suggestCommand("/twinkies data " + playerNick + " " + append + " delete")));
    }
}
