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
import org.jetbrains.annotations.Nullable;

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
    private OfflinePlayer getOfflinePlayerCase(@NotNull final String playerNick)
    {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers())
        {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equals(playerNick))
                return offlinePlayer;
        }

        return Bukkit.getOfflinePlayer(playerNick);
    }

    @NotNull
    public Component getComponent(@NotNull final HashMap<UUID, String> data, @NotNull final UUID playerUUID, @Nullable final Function<String, Component> appendComponent)
    {
        if (data.containsKey(playerUUID))
        {
            if (appendComponent != null)
                return Component.text(get(data, playerUUID)).color(NamedTextColor.WHITE).append(appendComponent.apply(get(data, playerUUID)));
            else
                return Component.text(get(data, playerUUID)).color(NamedTextColor.WHITE);
        }

        return Component.empty();
    }

    @NotNull
    public Set<Component> getComponentCollection(@NotNull final HashMap<UUID, Set<String>> data, @NotNull final UUID playerUUID, @Nullable final Function<String, Component> appendComponent)
    {
        Set<Component> collection = new HashSet<>();

        for (String element : getCollection(data, playerUUID))
        {
            if (appendComponent != null)
                collection.add(Component.text(element).color(NamedTextColor.WHITE).append(appendComponent.apply(element)));
            else
                collection.add(Component.text(element).color(NamedTextColor.WHITE));
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
    public Component getPlayerInfo(@NotNull final OfflinePlayer player, @Nullable final Function<String, Component> appendNick, @Nullable final Function<String, Component> appendIp)
    {
        Component playerInfo = Component.text("Информация о игроке '")
                .append(Component.text(player.getName()))
                .append(Component.text("'"))
                .color(NamedTextColor.BLUE);

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
            playerInfo = playerInfo.appendNewline()
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

        if (args.length == 2 && args[0].equalsIgnoreCase("data"))
            result.add("search");

        return result;
    }

    @NotNull
    public List<String> playerTabComplete(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return List.of();
        if (!args[0].equalsIgnoreCase("data"))
            return List.of();

        List<String> result = new ArrayList<>();
        Set<UUID> playerUUIDs = new HashSet<>(playerNicknames.keySet());
        playerUUIDs.addAll(playerIps.keySet());

        if (args.length == 2)
            result.add("player");

        if (args.length == 3 && args[1].equalsIgnoreCase("player"))
        {
            for (UUID playerUUID : playerUUIDs)
            {
                if (Bukkit.getOfflinePlayer(playerUUID).getName() == null)
                    continue;

                if (args[2].isBlank())
                    result.add(Bukkit.getOfflinePlayer(playerUUID).getName());
                else
                {
                    if (Bukkit.getOfflinePlayer(playerUUID).getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        result.add(Bukkit.getOfflinePlayer(playerUUID).getName());
                }
            }
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("player"))
        {
            result.add("nick");
            result.add("ip");
            result.add("search");
        }

        if (args.length == 5 && args[3].equalsIgnoreCase("nick"))
        {
            result.addAll(getCollection(playerNicknames, getOfflinePlayerCase(args[2]).getUniqueId()));
        }
        else if (args.length == 5 && args[3].equalsIgnoreCase("ip"))
        {
            result.addAll(getCollection(playerIps, getOfflinePlayerCase(args[2]).getUniqueId()));
        }

        return result;
    }

    @NotNull
    public List<String> nickTabComplete(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return List.of();
        if (!args[0].equalsIgnoreCase("data"))
            return List.of();

        List<String> result = new ArrayList<>();
        Set<UUID> playerUUIDs = new HashSet<>(playerNicknames.keySet());
        playerUUIDs.addAll(playerIps.keySet());

        if (args.length == 2)
            result.add("nick");

        if (args.length == 3 && args[1].equalsIgnoreCase("nick"))
        {
            for (UUID playerUUID : playerUUIDs) {
                result.addAll(getCollection(playerNicknames, playerUUID));
            }
        }

        return result;
    }

    @NotNull
    public List<String> ipTabComplete(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return List.of();
        if (!args[0].equalsIgnoreCase("data"))
            return List.of();

        List<String> result = new ArrayList<>();
        Set<UUID> playerUUIDs = new HashSet<>(playerNicknames.keySet());
        playerUUIDs.addAll(playerIps.keySet());

        if (args.length == 2)
            result.add("ip");

        if (args.length == 3 && args[1].equalsIgnoreCase("ip"))
        {
            for (UUID playerUUID : playerUUIDs) {
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

    public void onCommand(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        playerData(sender, args);
        nickSearch(sender, args);
        ipSearch(sender, args);
        playerNickSearch(sender, args);
        playerIpSearch(sender, args);
        playerSearch(sender, args);
    }

    /**
     * /twinkies data search - Поиск твинков всех игроков
     */
    public void allSearch(@NotNull final CommandSender sender, @NotNull final String[] args)
    {

    }

    /**
     * /twinkies data nick [nick] - Найти игроков использовавших указанный [nick]
     */
    public boolean nickSearch(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return false;
        if (args.length != 3)
            return false;
        if (!args[0].equalsIgnoreCase("data") || !args[1].equalsIgnoreCase("nick"))
            return false;

        String searchNick = args[2];

        Component searchResult = Component.text("Следующие игроки использовали никнейм '")
                .append(Component.text(searchNick))
                .append(Component.text("'"))
                .color(NamedTextColor.BLUE);

        boolean found = false;

        for (UUID compareUUID : playerNicknames.keySet())
        {
            if (getCollection(playerNicknames, compareUUID).contains(searchNick))
            {
                found = true;
                searchResult = searchResult.appendNewline()
                        .append(Component.text(Bukkit.getOfflinePlayer(compareUUID).getName())
                                .hoverEvent(HoverEvent.showText(getPlayerInfo(Bukkit.getOfflinePlayer(compareUUID), null, null)))
                                .color(NamedTextColor.WHITE));
            }
        }

        if (found)
        {
            sender.sendMessage(searchResult);
            if (sender instanceof Player)
                Log.info("Игрок '" + sender.getName() + "' запросил поиск игроков использовавших никнейм '" + searchNick + "'");
            else
                Log.info("Консоль запросила поиск игроков использовавших никнейм '" + searchNick + "'");
        }

        return found;
    }

    /**
     * /twinkies data ip [ip] - Найти игроков использовавших указанный [ip]
     */
    public boolean ipSearch(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return false;
        if (args.length != 3)
            return false;
        if (!args[0].equalsIgnoreCase("data") || !args[1].equalsIgnoreCase("ip"))
            return false;

        String searchIp = args[2];

        Component searchResult = Component.text("Следующие игроки использовали IP адрес '")
                .append(Component.text(searchIp))
                .append(Component.text("'"))
                .color(NamedTextColor.BLUE);

        boolean found = false;

        for (UUID compareUUID : playerIps.keySet())
        {
            if (getCollection(playerIps, compareUUID).contains(searchIp))
            {
                found = true;
                searchResult = searchResult.appendNewline()
                        .append(Component.text(Bukkit.getOfflinePlayer(compareUUID).getName())
                                .hoverEvent(HoverEvent.showText(getPlayerInfo(Bukkit.getOfflinePlayer(compareUUID), null, null)))
                                .color(NamedTextColor.WHITE));
            }
        }

        if (found)
        {
            sender.sendMessage(searchResult);
            if (sender instanceof Player)
                Log.info("Игрок '" + sender.getName() + "' запросил поиск игроков использовавших IP адрес '" + searchIp + "'");
            else
                Log.info("Консоль запросила поиск игроков использовавших IP адрес '" + searchIp + "'");
        }

        return found;
    }

    /**
     * /twinkies data player [playerNick] - Показать информацию о игроке [playerNick]
     */
    public void playerData(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return;
        if (args.length != 3)
            return;
        if (!args[0].equalsIgnoreCase("data") || !args[1].equalsIgnoreCase("player"))
            return;
        if (getCollection(playerNicknames, getOfflinePlayerCase(args[2]).getUniqueId()).isEmpty() && getCollection(playerIps, getOfflinePlayerCase(args[2]).getUniqueId()).isEmpty())
        {
            sender.sendMessage(Component.text("Информация о игроке '").append(Component.text(args[2]).append(Component.text("' не найдена!"))).color(NamedTextColor.RED));
            Log.warn("Произошла ошибка при попытке получить данные о игроке '" + args[2] + "'");
            Log.warn("Информация о игроке '" + args[2] + "' не найдена");
            if (sender instanceof Player)
                Log.warn("Исполнитель запроса: '" + sender.getName() + "'");
            else
                Log.warn("Запрос был выполнен из консоли");
            return;
        }

        OfflinePlayer player = getOfflinePlayerCase(args[2]);

        sender.sendMessage(getPlayerInfo(player, (nick) ->
                        Component.text(" [✔]").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN)
                                .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы найти возможные твинки по этому никнейму").color(NamedTextColor.GREEN)))
                                .clickEvent(ClickEvent.suggestCommand("/twinkies data player " + args[2] + " nick " + nick))
                                .appendSpace()
                                .append(Component.text("[✖]").decorate(TextDecoration.BOLD).color(NamedTextColor.RED)
                                        .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы удалить этот никнейм").color(NamedTextColor.RED)))
                                        .clickEvent(ClickEvent.suggestCommand("/twinkies data player " + args[2] + " nick " + nick + " delete"))),
                (ip) ->
                        Component.text(" [✔]").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN)
                                .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы найти возможные твинки по этому IP адресу").color(NamedTextColor.GREEN)))
                                .clickEvent(ClickEvent.suggestCommand("/twinkies data player " + args[2] + " ip " + ip))
                                .appendSpace()
                                .append(Component.text("[✖]").decorate(TextDecoration.BOLD).color(NamedTextColor.RED)
                                        .hoverEvent(HoverEvent.showText(Component.text("Нажмите чтобы удалить этот IP адрес").color(NamedTextColor.RED)))
                                        .clickEvent(ClickEvent.suggestCommand("/twinkies data player " + args[2] + " ip " + ip + " delete")))
        ));

        if (sender instanceof Player)
            Log.info("Игрок '" + sender.getName() + "' запросил данные о игроке '" + args[2] + "'");
        else
            Log.info("Консоль запросила данные о игроке '" + args[2] + "'");
    }

    /**
     * /twinkies data player [playerNick] nick [nick] - Найти твинки игрока [playerNick] по указанному [nick]
     */
    public void playerNickSearch(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return;
        if (args.length != 5)
            return;
        if (!args[0].equalsIgnoreCase("data") || !args[1].equalsIgnoreCase("player") || !args[3].equalsIgnoreCase("nick"))
            return;
        if (!getCollection(playerNicknames, getOfflinePlayerCase(args[2]).getUniqueId()).contains(args[4]))
            return;
        if (getCollection(playerNicknames, getOfflinePlayerCase(args[2]).getUniqueId()).isEmpty())
        {
            sender.sendMessage(Component.text("Никнеймы игрока '").append(Component.text(args[2]).append(Component.text("' не найдены!"))).color(NamedTextColor.RED));
            Log.warn("Произошла ошибка при попытке поиска твинков игрока '" + args[2] + "' по никейму '" + args[4] + "'");
            Log.warn("Никнеймы игрока '" + args[2] + "' не найдены");
            if (sender instanceof Player)
                Log.warn("Исполнитель запроса: '" + sender.getName() + "'");
            else
                Log.warn("Запрос был выполнен из консоли");
            return;
        }

        UUID playerUUID = getOfflinePlayerCase(args[2]).getUniqueId();
        String searchNick = args[4];

        Component searchResult = Component.text("Результаты поиска твинков по нику '")
                .append(Component.text(searchNick))
                .append(Component.text("'"))
                .color(NamedTextColor.BLUE);

        Set<Component> foundTwinks = searchTwinksByNick(playerUUID, searchNick);
        Set<Component> predictedTwinks = predictTwinksByNick(playerUUID, searchNick);

        if (!foundTwinks.isEmpty())
        {
            searchResult = searchResult.appendNewline()
                    .append(Component.text("Найденные твинки:"))
                    .color(NamedTextColor.BLUE)
                    .appendNewline();
            searchResult = appendComponentCollection(searchResult, foundTwinks);
        }

        if (!predictedTwinks.isEmpty())
        {
            searchResult = searchResult.appendNewline()
                    .append(Component.text("Игроки с похожими никнеймами:"))
                    .color(NamedTextColor.BLUE)
                    .appendNewline();
            searchResult = appendComponentCollection(searchResult, predictedTwinks);
        }

        if (foundTwinks.isEmpty() && predictedTwinks.isEmpty())
            searchResult = Component.text("Твинки не найдены.").color(NamedTextColor.RED);

        sender.sendMessage(searchResult);

        if (sender instanceof Player)
            Log.info("Игрок '" + sender.getName() + "' запросил поиск твинков игрока '" + args[2] + "' по нику '" + searchNick + "'");
        else
            Log.info("Консоль запросила поиск твинков игрока '" + args[2] + "' по нику '" + searchNick + "'");
    }

    /**
     * /twinkies data player [playerNick] ip [ip] - Найти твинки игрока [playerNick] по указанному [ip]
     */
    public void playerIpSearch(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return;
        if (args.length != 5)
            return;
        if (!args[0].equalsIgnoreCase("data") || !args[1].equalsIgnoreCase("player") || !args[3].equalsIgnoreCase("ip"))
            return;
        if (!getCollection(playerIps, getOfflinePlayerCase(args[2]).getUniqueId()).contains(args[4]))
            return;
        if (getCollection(playerIps, getOfflinePlayerCase(args[2]).getUniqueId()).isEmpty())
        {
            sender.sendMessage(Component.text("IP адреса игрока '").append(Component.text(args[2]).append(Component.text("' не найдены!"))).color(NamedTextColor.RED));
            Log.warn("Произошла ошибка при попытке поиска твинков игрока '" + args[2] + "' по IP адресу '" + args[4] + "'");
            Log.warn("IP адреса игрока '" + args[2] + "' не найдены");
            if (sender instanceof Player)
                Log.warn("Исполнитель запроса: '" + sender.getName() + "'");
            else
                Log.warn("Запрос был выполнен из консоли");
            return;
        }

        UUID playerUUID = getOfflinePlayerCase(args[2]).getUniqueId();
        String searchIp = args[4];

        Component searchResult = Component.text("Результаты поиска твинков по IP адресу '")
                .append(Component.text(searchIp))
                .append(Component.text("'"))
                .color(NamedTextColor.BLUE);


        Set<Component> foundTwinks = searchTwinksByIp(playerUUID, searchIp);

        if (!foundTwinks.isEmpty())
        {
            searchResult = searchResult.appendNewline()
                    .append(Component.text("Найденные твинки:"))
                    .color(NamedTextColor.BLUE)
                    .appendNewline();
            searchResult = appendComponentCollection(searchResult, foundTwinks);
        }

        if (foundTwinks.isEmpty())
            searchResult = Component.text("Твинки не найдены.").color(NamedTextColor.RED);

        sender.sendMessage(searchResult);

        if (sender instanceof Player)
            Log.info("Игрок '" + sender.getName() + "' запросил поиск твинков игрока '" + args[2] + "' по IP адресу '" + searchIp + "'");
        else
            Log.info("Консоль запросила поиск твинков игрока '" + args[2] + "' по IP адресу '" + searchIp + "'");
    }

    /**
     * /twinkies data player [playerNick] search - Найти твинки игрока [playerNick] по всем сохранённым никнеймам и ip
     */
    public void playerSearch(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.playerData"))
            return;
        if (args.length != 4)
            return;
        if (!args[0].equalsIgnoreCase("data") || !args[1].equalsIgnoreCase("player") || !args[3].equalsIgnoreCase("search"))
            return;
        if (!getOfflinePlayerCase(args[2]).hasPlayedBefore())
        {
            sender.sendMessage(Component.text("Игрок '").append(Component.text(args[2]).append(Component.text("' не найден!"))).color(NamedTextColor.RED));
            Log.warn("Произошла ошибка при попытке поиска твинков игрока '" + args[1] + "'");
            Log.warn("Игрок '" + args[2] + "' не найден");
            if (sender instanceof Player)
                Log.warn("Исполнитель запроса: '" + sender.getName() + "'");
            else
                Log.warn("Запрос был выполнен из консоли");
            return;
        }

        String playerNick = args[2];
        UUID playerUUID = getOfflinePlayerCase(args[2]).getUniqueId();

        Component searchResult = Component.text("Результаты поиска твинков игрока '")
                .append(Component.text())
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

        if (!foundTwinks.isEmpty())
        {
            searchResult = searchResult.appendNewline()
                    .append(Component.text("Найденные твинки:"))
                    .color(NamedTextColor.BLUE)
                    .appendNewline();
            searchResult = appendComponentCollection(searchResult, foundTwinks);
        }

        if (!predictedTwinks.isEmpty())
        {
            searchResult = searchResult.appendNewline()
                    .append(Component.text("Игроки с похожими никнеймами:"))
                    .color(NamedTextColor.BLUE)
                    .appendNewline();
            searchResult = appendComponentCollection(searchResult, predictedTwinks);
        }

        if (foundTwinks.isEmpty() && predictedTwinks.isEmpty())
            searchResult = Component.text("Твинки не найдены.").color(NamedTextColor.RED);

        sender.sendMessage(searchResult);

        if (sender instanceof Player)
            Log.info("Игрок '" + sender.getName() + "' запросил поиск твинков игрока '" + playerNick + "'");
        else
            Log.info("Консоль запросила поиск твинков игрока '" + playerNick + "'");
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
                        .append(Component.text(comparePlayer.getName()).hoverEvent(HoverEvent.showText(getPlayerInfo(comparePlayer, null, null))))
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
                                    .append(Component.text(comparePlayer.getName()).hoverEvent(HoverEvent.showText(getPlayerInfo(comparePlayer, null, null))))
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
