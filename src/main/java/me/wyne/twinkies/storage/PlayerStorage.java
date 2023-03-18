package me.wyne.twinkies.storage;

import com.google.gson.*;
import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.wlog.WLog;
import me.wyne.twinkies.wstorage.JsonStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
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
import java.util.function.Function;

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
    public Component getPlayerInfo(@NotNull final OfflinePlayer player, @NotNull final Function<String, Component> appendNick, @NotNull final Function<String, Component> appendIp)
    {
        Component playerInfo = Component.text("Информация о игроке '")
                .append(Component.text(player.getName()))
                .append(Component.text("'"))
                .appendNewline()
                .append(Component.text("Никнеймы:"))
                .color(NamedTextColor.BLUE)
                .appendNewline();

        for (String nickname : getCollection(playerNicknames, player.getUniqueId()))
        {
            playerInfo = playerInfo.append(Component.text(nickname).color(NamedTextColor.WHITE)).append(appendNick.apply(nickname));
            playerInfo = playerInfo.appendNewline();
        }

        playerInfo = playerInfo.appendNewline().append(Component.text("IP адреса:")).color(NamedTextColor.BLUE).appendNewline();

        for (String ip : getCollection(playerIps, player.getUniqueId()))
        {
            playerInfo = playerInfo.append(Component.text(ip).color(NamedTextColor.WHITE)).append(appendIp.apply(ip));
            playerInfo = playerInfo.appendNewline();
        }

        playerInfo = playerInfo.appendNewline();

        if (playerLastNickname.containsKey(player.getUniqueId()))
        {
            playerInfo = playerInfo.append(Component.text("Последний никнейм: ")).color(NamedTextColor.BLUE);
            playerInfo = playerInfo.append(Component.text(playerLastNickname.get(player.getUniqueId())).color(NamedTextColor.WHITE)).append(appendNick.apply(playerLastNickname.get(player.getUniqueId())));
            playerInfo = playerInfo.appendNewline();
        }

        if (playerLastIp.containsKey(player.getUniqueId()))
        {
            playerInfo = playerInfo.append(Component.text("Последний IP адрес: ").color(NamedTextColor.BLUE));
            playerInfo = playerInfo.append(Component.text(playerLastIp.get(player.getUniqueId())).color(NamedTextColor.WHITE)).append(appendIp.apply(playerLastIp.get(player.getUniqueId())));
        }

        return playerInfo;
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
                result.addAll(getCollection(playerNicknames, Bukkit.getOfflinePlayer(args[1]).getUniqueId()));
                result.addAll(getCollection(playerIps, Bukkit.getOfflinePlayer(args[1]).getUniqueId()));
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
            WLog.error("Игрок '" + args[1] + "' не найден");
            if (sender instanceof Player)
                WLog.error("Игрок: '" + sender.getName() + "'");
            else
                WLog.error("Запрос был выполнен из консоли");
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

        sender.sendMessage(getPlayerInfo(player, (nickname) ->
                Component.text(" [✔]").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы найти возможные твинки по этому никнейму").color(NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.suggestCommand("/twinkies data " + args[1] + " " + nickname + " find"))
                .appendSpace()
                .append(Component.text("[✖]").decorate(TextDecoration.BOLD).color(NamedTextColor.RED)
                .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы удалить этот никнейм").color(NamedTextColor.RED)))
                .clickEvent(ClickEvent.suggestCommand("/twinkies data " + args[1] + " " + nickname + " delete"))),
                (ip) ->
                Component.text(" [✔]").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы найти возможные твинки по этому IP адресу").color(NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.suggestCommand("/twinkies data " + args[1] + " " + ip + " find"))
                .appendSpace()
                .append(Component.text("[✖]").decorate(TextDecoration.BOLD).color(NamedTextColor.RED)
                .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы удалить этот IP адрес").color(NamedTextColor.RED)))
                .clickEvent(ClickEvent.suggestCommand("/twinkies data " + args[1] + " " + ip + " delete")))
        ));
    }

    public void deleteData(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerDataMod"))
            return;
        if (args.length != 4)
            return;
        if (!args[0].equalsIgnoreCase("data") || !args[3].equalsIgnoreCase("delete"))
            return;
        if (!Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore())
        {
            sender.sendMessage(Component.text("Игрок '").append(Component.text(args[1]).append(Component.text("' не найден!"))).color(NamedTextColor.RED));
            WLog.error("Произошла ошибка при попытке получить удалить данные о игроке '" + args[1] + "'");
            WLog.error("Игрок '" + args[1] + "' не найден");
            if (sender instanceof Player)
                WLog.error("Игрок: '" + sender.getName() + "'");
            else
                WLog.error("Запрос был выполнен из консоли");
            return;
        }

        UUID playerUUID = Bukkit.getOfflinePlayer(args[1]).getUniqueId();

        boolean deleted = false;

        if (playerLastNickname.containsKey(playerUUID) && playerLastNickname.get(playerUUID).equalsIgnoreCase(args[2]))
        {
            remove(playerLastNickname, playerUUID, "last-nickname");
            deleted = true;
        }
        if (getCollection(playerNicknames, playerUUID).contains(args[2]))
        {
            removeCollection(playerNicknames, playerUUID, args[2], "nicknames");
            deleted = true;
        }

        if (playerLastIp.containsKey(playerUUID) && playerLastIp.get(playerUUID).equalsIgnoreCase(args[2]))
        {
            remove(playerLastIp, playerUUID, "last-ip");
            deleted = true;
        }
        if (getCollection(playerIps, playerUUID).contains(args[2]))
        {
            removeCollection(playerIps, playerUUID, args[2], "ips");
            deleted = true;
        }

        if (!deleted)
        {
            sender.sendMessage(Component.text("Значение '").append(Component.text(args[2])).append(Component.text("' игрока '")).append(Component.text(args[1])).append(Component.text("' не найдено!")).color(NamedTextColor.RED));
            WLog.error("Произошла ошибка при попытке удалить данные о игроке '" + args[1] + "'");
            WLog.error("Значение '" + args[2] + "' не найдено");
            if (sender instanceof Player)
                WLog.error("Игрок: '" + sender.getName() + "'");
            else
                WLog.error("Запрос был выполнен из консоли");
        }
        else {
            sender.sendMessage(Component.text("Значение '").append(Component.text(args[2])).append(Component.text("' игрока '")).append(Component.text(args[1])).append(Component.text("' удалено.")).color(NamedTextColor.GREEN));
            if (sender instanceof Player)
                WLog.info("Значение '" + args[2] + "' игрока '" + args[1] + "' удалено игроком '" + sender.getName() + "'");
            else
                WLog.info("Значение '" + args[2] + "' игрока '" + args[1] + "' удалено консолью");
        }
    }
}
