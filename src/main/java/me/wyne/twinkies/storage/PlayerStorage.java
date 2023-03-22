package me.wyne.twinkies.storage;

import com.google.gson.*;
import me.wyne.twinkies.Twinkies;
import me.wyne.wutils.log.Log;
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
                Log.info("Загрузка данных из файла '" + storageFile.getName() + "'...");
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
                Log.info("Данные из файла '" + storageFile.getName() + "' загружены");
            } catch (FileNotFoundException e) {
                Log.error("Произошла ошибка при загрузке данных из файла '" + storageFile.getName() + "'");
                Log.error(e.getMessage());
            }
        });
    }

    @NotNull
    private OfflinePlayer getOfflinePlayerCase(@NotNull final String playerName)
    {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers())
        {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equals(playerName))
                return offlinePlayer;
        }

        return Bukkit.getOfflinePlayer(playerName);
    }

    @NotNull
    public Component getComponent(@NotNull final HashMap<UUID, String> data, @NotNull final UUID playerUUID, @NotNull final Function<String, Component> appendComponent)
    {
        if (data.containsKey(playerUUID))
            return Component.text(get(data, playerUUID)).append(appendComponent.apply(get(data, playerUUID)));
        return Component.empty();
    }

    @NotNull
    public Set<Component> getComponentCollection(@NotNull final HashMap<UUID, Set<String>> data, @NotNull final UUID playerUUID, @NotNull final Function<String, Component> appendComponent)
    {
        Set<Component> collection = new HashSet<>();

        for (String element : getCollection(data, playerUUID))
        {
            collection.add(Component.text(element).append(appendComponent.apply(element)));
        }

        return collection;
    }

    @NotNull
    public Component appendComponentCollection(@NotNull Component component, @NotNull final Collection<Component> append)
    {
        int i = 0;
        for (Component apeendComponent : append)
        {
            component = component.append(apeendComponent);
            if (i != append.size() - 1)
                component = component.appendNewline();
            i++;
        }
        return component;
    }

    @NotNull
    public Component getPlayerInfo(@NotNull final OfflinePlayer player, @NotNull final Function<String, Component> appendNick, @NotNull final Function<String, Component> appendIp)
    {
        Component playerInfo = Component.text("Информация о игроке '")
                .append(Component.text(player.getName()))
                .append(Component.text("'"));

        if (!getCollection(playerNicknames, player.getUniqueId()).isEmpty())
        {
            playerInfo = playerInfo.appendNewline()
                    .append(Component.text("Никнеймы:"))
                    .color(NamedTextColor.BLUE)
                    .appendNewline();
            playerInfo = appendComponentCollection(playerInfo, getComponentCollection(playerNicknames, player.getUniqueId(), appendNick));
        }

        if (!getCollection(playerIps, player.getUniqueId()).isEmpty())
        {
            playerInfo = playerInfo.appendNewline().appendNewline()
                    .append(Component.text("IP адреса:"))
                    .color(NamedTextColor.BLUE)
                    .appendNewline();
            playerInfo = appendComponentCollection(playerInfo, getComponentCollection(playerIps, player.getUniqueId(), appendIp));
        }

        if (playerLastNickname.containsKey(player.getUniqueId()))
        {
            playerInfo = playerInfo.appendNewline().appendNewline()
                    .append(Component.text("Последний никнейм: "))
                    .color(NamedTextColor.BLUE);
            playerInfo = playerInfo.append(getComponent(playerLastNickname, player.getUniqueId(), appendNick));
        }

        if (playerLastIp.containsKey(player.getUniqueId()))
        {
            playerInfo = playerInfo.appendNewline().appendNewline()
                    .append(Component.text("Последний IP адрес: "))
                    .color(NamedTextColor.BLUE);
            playerInfo = playerInfo.append(getComponent(playerLastIp, player.getUniqueId(), appendIp));
        }

        return playerInfo;
    }

    @NotNull
    public List<String> dataTabComplete(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return List.of();

        List<String> result = new ArrayList<>();

        if (args.length == 1)
            result.add("data");

        return result;
    }

    @NotNull
    public List<String> searchTabComplete(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return List.of();

        List<String> result = new ArrayList<>();
        Set<UUID> playerUUIDs = new HashSet<>(playerNicknames.keySet());
        playerUUIDs.addAll(playerIps.keySet());

        if (args.length == 2 || args.length == 3 || args.length == 4)
            result.add("search");

        if (args.length == 2)
        {
            for (UUID playerUUID : playerUUIDs)
            {
                if (Bukkit.getOfflinePlayer(playerUUID).getName() == null)
                    continue;

                if (args[1].isBlank())
                    result.add(Bukkit.getOfflinePlayer(playerUUID).getName());
                else
                {
                    if (Bukkit.getOfflinePlayer(playerUUID).getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        result.add(Bukkit.getOfflinePlayer(playerUUID).getName());
                }
            }
        }

        if (args.length == 2 || args.length == 3)
        {
            for (UUID playerUUID : playerUUIDs)
            {
                result.addAll(getCollection(playerNicknames, playerUUID));
                result.addAll(getCollection(playerIps, playerUUID));
            }
        }

        return result;
    }

    @NotNull
    public List<String> deleteTabComplete(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerDataMod"))
            return List.of();

        List<String> result = new ArrayList<>();
        Set<UUID> playerUUIDs = new HashSet<>(playerNicknames.keySet());
        playerUUIDs.addAll(playerIps.keySet());

        if (args.length == 3 || args.length == 4)
            result.add("delete");

        if (args.length == 2)
        {
            for (UUID playerUUID : playerUUIDs)
            {
                if (Bukkit.getOfflinePlayer(playerUUID).getName() == null)
                    continue;

                if (args[1].isBlank())
                    result.add(Bukkit.getOfflinePlayer(playerUUID).getName());
                else
                {
                    if (Bukkit.getOfflinePlayer(playerUUID).getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        result.add(Bukkit.getOfflinePlayer(playerUUID).getName());
                }
            }
        }

        if (args.length == 3)
        {
            for (UUID playerUUID : playerUUIDs)
            {
                result.addAll(getCollection(playerNicknames, playerUUID));
                result.addAll(getCollection(playerIps, playerUUID));
            }
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
        if (args[1].equalsIgnoreCase("search"))
            return;
        if (!playerNicknames.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()) && !playerIps.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()))
        {
            sender.sendMessage(Component.text("Игрок '").append(Component.text(args[1]).append(Component.text("' не найден!"))).color(NamedTextColor.RED));
            Log.error("Произошла ошибка при попытке получить данные о игроке '" + args[1] + "'");
            Log.error("Игрок '" + args[1] + "' не найден");
            if (sender instanceof Player)
                Log.error("Исполнитель запроса: '" + sender.getName() + "'");
            else
                Log.error("Запрос был выполнен из консоли");
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
            Log.info("Игрок '" + sender.getName() + "' запросил данные о игроке '" + args[1] + "'");
        else
            Log.info("Консоль запросила данные о игроке '" + args[1] + "'");
    }

    public void searchTwinks(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return;
        if (args.length < 2)
            return;
        if (!args[0].equalsIgnoreCase("data"))
            return;

        if (args.length == 4 && args[3].equalsIgnoreCase("search"))
        {
            if (!playerNicknames.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()) && !playerIps.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()))
            {
                sender.sendMessage(Component.text("Игрок '").append(Component.text(args[1]).append(Component.text("' не найден!"))).color(NamedTextColor.RED));
                Log.error("Произошла ошибка при попытке поиска твинков игрока '" + args[1] + "'");
                Log.error("Игрок '" + args[1] + "' не найден");
                if (sender instanceof Player)
                    Log.error("Исполнитель запроса: '" + sender.getName() + "'");
                else
                    Log.error("Запрос был выполнен из консоли");
                return;
            }

            UUID playerUUID = getOfflinePlayerCase(args[1]).getUniqueId();

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
                    Log.info("Игрок '" + sender.getName() + "' запросил поиск твинков игрока '" + args[1] + "' по нику '" + args[2] + "'");
                else
                    Log.info("Консоль запросила поиск твинков игрока '" + args[1] + "' по нику '" + args[2] + "'");
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
                    Log.info("Игрок '" + sender.getName() + "' запросил поиск твинков игрока '" + args[1] + "' по IP адресу '" + args[2] + "'");
                else
                    Log.info("Консоль запросила поиск твинков игрока '" + args[1] + "' по IP адресу '" + args[2] + "'");
            }
            else
            {
                sender.sendMessage(Component.text("Значение '").append(Component.text(args[2])).append(Component.text("' игрока '")).append(Component.text(args[1])).append(Component.text("' не найдено!")).color(NamedTextColor.RED));
                Log.error("Произошла ошибка при попытке поиска твинков игрока '" + args[1] + "'");
                Log.error("Значение '" + args[2] + "' не найдено");
                if (sender instanceof Player)
                    Log.error("Исполнитель запроса: '" + sender.getName() + "'");
                else
                    Log.error("Запрос был выполнен из консоли");
            }
        }
        else if (args.length == 3 && args[2].equalsIgnoreCase("search"))
        {
            if (!playerNicknames.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()) && !playerIps.containsKey(getOfflinePlayerCase(args[1]).getUniqueId()))
            {
                sender.sendMessage(Component.text("Игрок '").append(Component.text(args[1]).append(Component.text("' не найден!"))).color(NamedTextColor.RED));
                Log.error("Произошла ошибка при попытке поиска твинков игрока '" + args[1] + "'");
                Log.error("Игрок '" + args[1] + "' не найден");
                if (sender instanceof Player)
                    Log.error("Исполнитель запроса: '" + sender.getName() + "'");
                else
                    Log.error("Запрос был выполнен из консоли");
                return;
            }

            UUID playerUUID = getOfflinePlayerCase(args[1]).getUniqueId();

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
                Log.info("Игрок '" + sender.getName() + "' запросил поиск твинков игрока '" + args[1] + "'");
            else
                Log.info("Консоль запросила поиск твинков игрока '" + args[1] + "'");
        }
        else if (args.length == 2 && args[1].equalsIgnoreCase("search"))
        {
            Component searchResult = Component.text("Результаты поиска твинков всех игроков").color(NamedTextColor.BLUE);

            Set<Component> foundTwinks = new HashSet<>();

            for (OfflinePlayer player : Bukkit.getOfflinePlayers())
            {
                for (String nick : getCollection(playerNicknames, player.getUniqueId()))
                {
                    for (UUID compareUUID : playerNicknames.keySet())
                    {
                        if (player.getUniqueId().equals(compareUUID))
                            continue;

                        if (getCollection(playerNicknames, compareUUID).contains(nick))
                        {
                            foundTwinks.add(Component.empty()
                                    .append(Component.text(Bukkit.getOfflinePlayer(player.getUniqueId()).getName()).hoverEvent(HoverEvent.showText(getPlayerInfo(Bukkit.getOfflinePlayer(player.getUniqueId()), (nickname) -> Component.empty(), (ip) -> Component.empty()))))
                                    .append(Component.text(" ("))
                                    .append(Component.text(Bukkit.getOfflinePlayer(compareUUID).getName()).hoverEvent(HoverEvent.showText(getPlayerInfo(Bukkit.getOfflinePlayer(compareUUID), (nickname) -> Component.empty(), (ip) -> Component.empty()))))
                                    .append(Component.text(")"))
                                    .append(Component.text(" ("))
                                    .append(Component.text(nick))
                                    .append(Component.text(")")));
                        }
                    }
                }

                for (String ip : getCollection(playerIps, player.getUniqueId()))
                {
                    for (UUID compareUUID : playerIps.keySet())
                    {
                        if (player.getUniqueId().equals(compareUUID))
                            continue;

                        OfflinePlayer comparePlayer = Bukkit.getOfflinePlayer(compareUUID);

                        if (getCollection(playerIps, compareUUID).contains(ip))
                        {
                            foundTwinks.add(Component.empty()
                                    .append(Component.text(Bukkit.getOfflinePlayer(player.getUniqueId()).getName()).hoverEvent(HoverEvent.showText(getPlayerInfo(Bukkit.getOfflinePlayer(player.getUniqueId()), (nickname) -> Component.empty(), (ip1) -> Component.empty()))))
                                    .append(Component.text(" ("))
                                    .append(Component.text(Bukkit.getOfflinePlayer(compareUUID).getName()).hoverEvent(HoverEvent.showText(getPlayerInfo(Bukkit.getOfflinePlayer(compareUUID), (nickname) -> Component.empty(), (ip1) -> Component.empty()))))
                                    .append(Component.text(")"))
                                    .append(Component.text(" ("))
                                    .append(Component.text(ip))
                                    .append(Component.text(")")));
                        }
                    }
                }
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

            if (foundTwinks.isEmpty())
                searchResult = Component.text("Твинки не найдены.").color(NamedTextColor.RED);

            sender.sendMessage(searchResult);

            if (sender instanceof Player)
                Log.info("Игрок '" + sender.getName() + "' запросил поиск твинков всех игроков");
            else
                Log.info("Консоль запросила поиск твинков всех игроков");
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
            Log.error("Произошла ошибка при попытке удалить данные о игроке '" + args[1] + "'");
            Log.error("Игрок '" + args[1] + "' не найден");
            if (sender instanceof Player)
                Log.error("Исполнитель запроса: '" + sender.getName() + "'");
            else
                Log.error("Запрос был выполнен из консоли");
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
                Log.error("Произошла ошибка при попытке удалить данные о игроке '" + args[1] + "'");
                Log.error("Значение '" + args[2] + "' не найдено");
                if (sender instanceof Player)
                    Log.error("Исполнитель запроса: '" + sender.getName() + "'");
                else
                    Log.error("Запрос был выполнен из консоли");
                return;
            }

            sender.sendMessage(Component.text("Значение '").append(Component.text(args[2])).append(Component.text("' игрока '")).append(Component.text(args[1])).append(Component.text("' удалено.")).color(NamedTextColor.GREEN));
            if (sender instanceof Player)
                Log.info("Значение '" + args[2] + "' игрока '" + args[1] + "' удалено игроком '" + sender.getName() + "'");
            else
                Log.info("Значение '" + args[2] + "' игрока '" + args[1] + "' удалено консолью");
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
                Log.info("Информация о игроке '" + args[1] + "' удалена игроком '" + sender.getName() + "'");
            else
                Log.info("Информация о игроке '" + args[1] + "' удалена консолью");
        }
    }
}
