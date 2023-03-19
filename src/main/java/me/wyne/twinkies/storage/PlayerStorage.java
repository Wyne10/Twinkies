package me.wyne.twinkies.storage;

import com.google.gson.*;
import me.wyne.twinkies.Twinkies;
import me.wyne.wutils.log.WLog;
import me.wyne.wutils.storage.JsonStorage;
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
    private OfflinePlayer getOfflinePlayerCase(@NotNull final String playerName)
    {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers())
        {
            if (offlinePlayer.getName().equals(playerName))
                return offlinePlayer;
        }

        return Bukkit.getOfflinePlayer(playerName);
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
            Set<UUID> playerUUIDs = new HashSet<>(playerNicknames.keySet());
            playerUUIDs.addAll(playerIps.keySet());

            for (UUID playerUUID : playerUUIDs)
            {
                if (args[1].isBlank())
                    result.add(Bukkit.getOfflinePlayer(playerUUID).getName());
                else
                {
                    if (Bukkit.getOfflinePlayer(playerUUID).getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        result.add(Bukkit.getOfflinePlayer(playerUUID).getName());
                }
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("data"))
        {
            result.add("search");
            if (sender.hasPermission("twinkies.playerDataMod"))
                result.add("delete");
            result.addAll(getCollection(playerNicknames, getOfflinePlayerCase(args[1]).getUniqueId()));
            result.addAll(getCollection(playerIps, getOfflinePlayerCase(args[1]).getUniqueId()));
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("data"))
        {
            result.add("search");
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
        if (!playerNicknames.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()) && !playerIps.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()))
        {
            sender.sendMessage(Component.text("Игрок '").append(Component.text(args[1]).append(Component.text("' не найден!"))).color(NamedTextColor.RED));
            WLog.error("Произошла ошибка при попытке получить данные о игроке '" + args[1] + "'");
            WLog.error("Игрок '" + args[1] + "' не найден");
            if (sender instanceof Player)
                WLog.error("Исполнитель запроса: '" + sender.getName() + "'");
            else
                WLog.error("Запрос был выполнен из консоли");
            return;
        }

        OfflinePlayer player = getOfflinePlayerCase(args[1]);

        sender.sendMessage(getPlayerInfo(player, (nickname) ->
                Component.text(" [✔]").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы найти возможные твинки по этому никнейму").color(NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.suggestCommand("/twinkies data " + args[1] + " " + nickname + " search"))
                .appendSpace()
                .append(Component.text("[✖]").decorate(TextDecoration.BOLD).color(NamedTextColor.RED)
                .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы удалить этот никнейм").color(NamedTextColor.RED)))
                .clickEvent(ClickEvent.suggestCommand("/twinkies data " + args[1] + " " + nickname + " delete"))),
                (ip) ->
                Component.text(" [✔]").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы найти возможные твинки по этому IP адресу").color(NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.suggestCommand("/twinkies data " + args[1] + " " + ip + " search"))
                .appendSpace()
                .append(Component.text("[✖]").decorate(TextDecoration.BOLD).color(NamedTextColor.RED)
                .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы удалить этот IP адрес").color(NamedTextColor.RED)))
                .clickEvent(ClickEvent.suggestCommand("/twinkies data " + args[1] + " " + ip + " delete")))
        ));

        if (sender instanceof Player)
            WLog.info("Игрок '" + sender.getName() + "' запросил данные о игроке '" + args[1] + "'");
        else
            WLog.info("Консоль запросила данные о игроке '" + args[1] + "'");
    }

    public void searchTwinks(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return;
        if (args.length < 3)
            return;
        if (!args[0].equalsIgnoreCase("data"))
            return;
        if (!playerNicknames.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()) && !playerIps.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()))
        {
            sender.sendMessage(Component.text("Игрок '").append(Component.text(args[1]).append(Component.text("' не найден!"))).color(NamedTextColor.RED));
            WLog.error("Произошла ошибка при попытке поиска твинков игрока '" + args[1] + "'");
            WLog.error("Игрок '" + args[1] + "' не найден");
            if (sender instanceof Player)
                WLog.error("Исполнитель запроса: '" + sender.getName() + "'");
            else
                WLog.error("Запрос был выполнен из консоли");
            return;
        }

        UUID playerUUID = getOfflinePlayerCase(args[1]).getUniqueId();

        if (args.length == 4 && args[3].equalsIgnoreCase("search"))
        {
            if (getCollection(playerNicknames, playerUUID).contains(args[2]))
            {
                Component searchResult = Component.text("Результаты поиска твинков по нику '")
                        .append(Component.text(args[2]))
                        .append(Component.text("'"))
                        .color(NamedTextColor.BLUE);

                Set<Component> foundTwinks = searchTwinksByNick(playerUUID, args[2]);
                Set<Component> predictedTwinks = predictTwinksByNick(playerUUID, args[2]);

                int i = 0;
                if (!foundTwinks.isEmpty())
                {
                    searchResult = searchResult.appendNewline();
                    searchResult = searchResult.append(Component.text("Найденные твинки: ")).color(NamedTextColor.BLUE);
                    searchResult = searchResult.appendNewline();
                    for (Component foundTwink : foundTwinks)
                    {
                        searchResult = searchResult.append(foundTwink);
                        if (i != foundTwinks.size() - 1)
                            searchResult = searchResult.appendNewline();
                        i++;
                    }
                }
                i = 0;
                if (!predictedTwinks.isEmpty())
                {
                    searchResult = searchResult.appendNewline();
                    searchResult = searchResult.append(Component.text("Игроки с похожими никнеймами:")).color(NamedTextColor.BLUE);
                    searchResult = searchResult.appendNewline();
                    for (Component predictedTwink : predictedTwinks)
                    {
                        searchResult = searchResult.append(predictedTwink);
                        if (i != predictedTwinks.size() - 1)
                            searchResult = searchResult.appendNewline();
                        i++;
                    }
                }

                if (foundTwinks.isEmpty() && predictedTwinks.isEmpty())
                    searchResult = Component.text("Твинки не найдены.").color(NamedTextColor.RED);

                sender.sendMessage(searchResult);

                if (sender instanceof Player)
                    WLog.info("Игрок '" + sender.getName() + "' запросил поиск твинков игрока '" + args[1] + "' по нику '" + args[2] + "'");
                else
                    WLog.info("Консоль запросила поиск твинков игрока '" + args[1] + "' по нику '" + args[2] + "'");
            }
            else if (getCollection(playerIps, playerUUID).contains(args[2]))
            {
                Component searchResult = Component.text("Результаты поиска твинков по IP адресу '")
                        .append(Component.text(args[2]))
                        .append(Component.text("'"))
                        .color(NamedTextColor.BLUE);

                Set<Component> foundTwinks = searchTwinksByIp(playerUUID, args[2]);

                int i = 0;
                if (!foundTwinks.isEmpty())
                {
                    searchResult = searchResult.appendNewline();
                    searchResult = searchResult.append(Component.text("Найденные твинки: ")).color(NamedTextColor.BLUE);
                    searchResult = searchResult.appendNewline();
                    for (Component foundTwink : foundTwinks)
                    {
                        searchResult = searchResult.append(foundTwink);
                        if (i != foundTwinks.size() - 1)
                            searchResult = searchResult.appendNewline();
                        i++;
                    }
                }
                else
                {
                    searchResult = Component.text("Твинки не найдены.").color(NamedTextColor.RED);
                }

                sender.sendMessage(searchResult);

                if (sender instanceof Player)
                    WLog.info("Игрок '" + sender.getName() + "' запросил поиск твинков игрока '" + args[1] + "' по IP адресу '" + args[2] + "'");
                else
                    WLog.info("Консоль запросила поиск твинков игрока '" + args[1] + "' по IP адресу '" + args[2] + "'");
            }
            else
            {
                sender.sendMessage(Component.text("Значение '").append(Component.text(args[2])).append(Component.text("' игрока '")).append(Component.text(args[1])).append(Component.text("' не найдено!")).color(NamedTextColor.RED));
                WLog.error("Произошла ошибка при попытке поиска твинков игрока '" + args[1] + "'");
                WLog.error("Значение '" + args[2] + "' не найдено");
                if (sender instanceof Player)
                    WLog.error("Исполнитель запроса: '" + sender.getName() + "'");
                else
                    WLog.error("Запрос был выполнен из консоли");
            }
        }
        else if (args.length == 3 && args[2].equalsIgnoreCase("search"))
        {
            Component searchResult = Component.text("Результаты поиска твинков игрока '")
                    .append(Component.text(args[1]))
                    .append(Component.text("'"))
                    .color(NamedTextColor.BLUE);

            Set<Component> foundTwinks = new HashSet<>();
            Set<Component> predictedTwinks = new HashSet<>();

            for (String nick : getCollection(playerNicknames, playerUUID))
            {
                foundTwinks.addAll(searchTwinksByNick(playerUUID, nick));
                predictedTwinks.addAll(predictTwinksByNick(playerUUID, nick));
            }

            for (String ip : getCollection(playerIps, playerUUID))
            {
                foundTwinks.addAll(searchTwinksByIp(playerUUID, ip));
            }

            int i = 0;
            if (!foundTwinks.isEmpty())
            {
                searchResult = searchResult.appendNewline();
                searchResult = searchResult.append(Component.text("Найденные твинки: ")).color(NamedTextColor.BLUE);
                searchResult = searchResult.appendNewline();
                for (Component foundTwink : foundTwinks)
                {
                    searchResult = searchResult.append(foundTwink);
                    if (i != foundTwinks.size() - 1)
                        searchResult = searchResult.appendNewline();
                    i++;
                }
            }
            i = 0;
            if (!predictedTwinks.isEmpty())
            {
                searchResult = searchResult.appendNewline();
                searchResult = searchResult.append(Component.text("Игроки с похожими никнеймами:")).color(NamedTextColor.BLUE);
                searchResult = searchResult.appendNewline();
                for (Component predictedTwink : predictedTwinks)
                {
                    searchResult = searchResult.append(predictedTwink);
                    if (i != predictedTwinks.size() - 1)
                        searchResult = searchResult.appendNewline();
                    i++;
                }
            }

            if (foundTwinks.isEmpty() && predictedTwinks.isEmpty())
                searchResult = Component.text("Твинки не найдены.").color(NamedTextColor.RED);

            sender.sendMessage(searchResult);

            if (sender instanceof Player)
                WLog.info("Игрок '" + sender.getName() + "' запросил поиск твинков игрока '" + args[1] + "'");
            else
                WLog.info("Консоль запросила поиск твинков игрока '" + args[1] + "'");
        }
    }

    @NotNull
    private Set<Component> searchTwinksByNick(@NotNull final UUID playerUUID, @NotNull final String searchNick)
    {
        Set<Component> foundTwinks = new HashSet<>();

        for (UUID compareUUID : playerNicknames.keySet())
        {
            if (playerUUID.equals(compareUUID))
                continue;

            OfflinePlayer comparePlayer = Bukkit.getOfflinePlayer(compareUUID);

            if (getCollection(playerNicknames, compareUUID).contains(searchNick))
            {
                foundTwinks.add(Component.empty()
                        .append(Component.text(comparePlayer.getName()).hoverEvent(HoverEvent.showText(getPlayerInfo(comparePlayer, (nickname) -> Component.empty(), (ip) -> Component.empty()))))
                        .append(Component.text(" ("))
                        .append(Component.text(searchNick))
                        .append(Component.text(")")));
            }
        }

        return foundTwinks;
    }

    @NotNull
    private Set<Component> predictTwinksByNick(@NotNull final UUID playerUUID, @NotNull final String predictNick)
    {
        Set<Component> predictedTwinks = new HashSet<>();

        Set<String> predictSplitRegex = new HashSet<>();
        predictSplitRegex.add("");
        predictSplitRegex.add("(?=\\p{Upper})");
        predictSplitRegex.add("_+");
        predictSplitRegex.add("\\.+");

        for (UUID compareUUID : playerNicknames.keySet())
        {
            if (playerUUID.equals(compareUUID))
                continue;

            OfflinePlayer comparePlayer =  Bukkit.getOfflinePlayer(compareUUID);

            for (String splitRegex : predictSplitRegex)
            {
                for (String nickPart : predictNick.split(splitRegex))
                {
                    for (String compareNick : getCollection(playerNicknames, compareUUID))
                    {
                        if (compareNick.toLowerCase().contains(nickPart.toLowerCase()) && !compareNick.equals(nickPart))
                            predictedTwinks.add(Component.empty()
                                    .append(Component.text(comparePlayer.getName()).hoverEvent(HoverEvent.showText(getPlayerInfo(comparePlayer, (nickname) -> Component.empty(), (ip) -> Component.empty()))))
                                    .append(Component.text(" ("))
                                    .append(Component.text(compareNick))
                                    .append(Component.text(")")));
                    }
                }
            }
        }

        return predictedTwinks;
    }

    @NotNull
    private Set<Component> searchTwinksByIp(@NotNull final UUID playerUUID, @NotNull final String searchIp)
    {
        Set<Component> foundTwinks = new HashSet<>();

        for (UUID compareUUID : playerIps.keySet())
        {
            if (playerUUID.equals(compareUUID))
                continue;

            OfflinePlayer comparePlayer = Bukkit.getOfflinePlayer(compareUUID);

            if (getCollection(playerIps, compareUUID).contains(searchIp))
            {
                foundTwinks.add(Component.empty()
                        .append(Component.text(comparePlayer.getName()).hoverEvent(HoverEvent.showText(getPlayerInfo(comparePlayer, (nickname) -> Component.empty(), (ip) -> Component.empty()))))
                        .append(Component.text(" ("))
                        .append(Component.text(searchIp))
                        .append(Component.text(")")));
            }
        }

        return foundTwinks;
    }

    public void deleteData(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerDataMod"))
            return;
        if (args.length < 3)
            return;
        if (!args[0].equalsIgnoreCase("data"))
            return;
        if (!playerNicknames.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()) && !playerIps.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()))
        {
            sender.sendMessage(Component.text("Игрок '").append(Component.text(args[1]).append(Component.text("' не найден!"))).color(NamedTextColor.RED));
            WLog.error("Произошла ошибка при попытке удалить данные о игроке '" + args[1] + "'");
            WLog.error("Игрок '" + args[1] + "' не найден");
            if (sender instanceof Player)
                WLog.error("Исполнитель запроса: '" + sender.getName() + "'");
            else
                WLog.error("Запрос был выполнен из консоли");
            return;
        }

        UUID playerUUID = getOfflinePlayerCase(args[1]).getUniqueId();

        if (args.length == 4 && args[3].equalsIgnoreCase("delete"))
        {
            boolean deleted = false;

            if (playerLastNickname.containsKey(playerUUID) && playerLastNickname.get(playerUUID).equals(args[2]))
            {
                remove(playerLastNickname, playerUUID, "last-nickname");
                deleted = true;
            }
            if (getCollection(playerNicknames, playerUUID).contains(args[2]))
            {
                removeCollection(playerNicknames, playerUUID, args[2], "nicknames");
                deleted = true;
            }

            if (playerLastIp.containsKey(playerUUID) && playerLastIp.get(playerUUID).equals(args[2]))
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
                    WLog.error("Исполнитель запроса: '" + sender.getName() + "'");
                else
                    WLog.error("Запрос был выполнен из консоли");
                return;
            }

            sender.sendMessage(Component.text("Значение '").append(Component.text(args[2])).append(Component.text("' игрока '")).append(Component.text(args[1])).append(Component.text("' удалено.")).color(NamedTextColor.GREEN));
            if (sender instanceof Player)
                WLog.info("Значение '" + args[2] + "' игрока '" + args[1] + "' удалено игроком '" + sender.getName() + "'");
            else
                WLog.info("Значение '" + args[2] + "' игрока '" + args[1] + "' удалено консолью");
        }
        else if (args.length == 3 && args[2].equalsIgnoreCase("delete"))
        {
            playerLastNickname.remove(playerUUID);
            playerNicknames.remove(playerUUID);
            playerLastIp.remove(playerUUID);
            playerIps.remove(playerUUID);
            remove(null, playerUUID, null);
            sender.sendMessage(Component.text("Информация о игроке '").append(Component.text(args[1])).append(Component.text("' удалена.")).color(NamedTextColor.GREEN));
            if (sender instanceof Player)
                WLog.info("Информация о игроке '" + args[1] + "' удалена игроком '" + sender.getName() + "'");
            else
                WLog.info("Информация о игроке '" + args[1] + "' удалена консолью");
        }
    }
}
