package me.wyne.twinkies.storage;

import com.google.gson.*;
import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.logging.WLog;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerStorage {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Twinkies plugin;

    private final File storageFile;

    private final HashMap<UUID, String> playerLastNickname = new HashMap<>();
    private final HashMap<UUID, Set<String>> playerNicknames = new HashMap<>();
    private final HashMap<UUID, String> playerLastIp = new HashMap<>();
    private final HashMap<UUID, Set<String>> playerIps = new HashMap<>();

    private final ExecutorService executorService;

    public PlayerStorage(@NotNull final Twinkies plugin)
    {
        this.plugin = plugin;
        storageFile = new File(plugin.getDataFolder(), "playerData.json");
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Creates necessary folder is absent.
     */
    public void createStorageFolder()
    {
        if (!plugin.getDataFolder().exists()) {
            WLog.info(plugin, "Создание папки плагина...");
            plugin.getDataFolder().mkdirs();
            WLog.info(plugin, "Папка плагина создана");
        }
    }

    /**
     * Creates and formats json file.
     */
    public void createStorageFile()
    {
        if (!storageFile.exists()) {
            WLog.info(plugin, "Создание файла 'playerData.json'...");
            try {
                if (storageFile.createNewFile()) {
                    PrintWriter writer = new PrintWriter(storageFile);
                    writer.write("{ }");
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                WLog.error(plugin, "Произошла ошибка при создании файла 'playerData.json'");
                WLog.error(plugin, e.getMessage());
            }
            WLog.info(plugin, "Файл 'playerData.json' создан");
        }
    }

    public boolean isPlayerRegistered(@NotNull final OfflinePlayer player)
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

    @Nullable
    public String getPlayerLastNickname(@NotNull final OfflinePlayer player) {
        if (!playerLastNickname.containsKey(player.getUniqueId()) || playerLastNickname.get(player.getUniqueId()).isEmpty())
            return null;
        return playerLastNickname.get(player.getUniqueId());
    }

    @Nullable
    public Set<String> getPlayerNicknames(@NotNull final OfflinePlayer player)
    {
        if (!playerNicknames.containsKey(player.getUniqueId()) || playerNicknames.get(player.getUniqueId()).isEmpty())
            return null;
        return playerNicknames.get(player.getUniqueId());
    }

    @Nullable
    public String getPlayerLastIp(@NotNull final OfflinePlayer player) {
        if (!playerLastIp.containsKey(player.getUniqueId()) || playerLastIp.get(player.getUniqueId()).isEmpty())
            return null;
        return playerLastIp.get(player.getUniqueId());
    }

    @Nullable
    public Set<String> getPlayerIps(@NotNull final OfflinePlayer player)
    {
        if (!playerIps.containsKey(player.getUniqueId()) || playerIps.get(player.getUniqueId()).isEmpty())
            return null;
        return playerIps.get(player.getUniqueId());
    }

    public void loadData()
    {
        executorService.execute(() -> {
            try {
                WLog.info(plugin, "Загрузка данных из файла 'playerData.json'...");
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

                    playerLastNickname.put(UUID.fromString(playerObject.getKey()), playerObject.getValue().getAsJsonObject().get("last-nickname").getAsString());
                    playerNicknames.put(UUID.fromString(playerObject.getKey()), newPlayerNicknames);
                    playerLastIp.put(UUID.fromString(playerObject.getKey()), playerObject.getValue().getAsJsonObject().get("last-ip").getAsString());
                    playerIps.put(UUID.fromString(playerObject.getKey()), newPlayerIps);
                }
                WLog.info(plugin, "Данные из файла 'playerData.json' загружены");
            } catch (FileNotFoundException e) {
                WLog.error(plugin, "Произошла ошибка при загрузке данных из файла 'playerData.json'");
                WLog.error(plugin, e.getMessage());
            }
        });
    }

    public void savePlayerNickname(@NotNull final OfflinePlayer player, @NotNull final String nickName)
    {
        if (!player.hasPlayedBefore())
            return;

        UUID playerUUID = player.getUniqueId();

        Set<String> newPlayerNicknames = new HashSet<>();

        if (playerNicknames.containsKey(playerUUID))
            newPlayerNicknames = playerNicknames.get(playerUUID);

        if (newPlayerNicknames.contains(nickName))
        {
            WLog.warn(plugin, "Никнейм '" + nickName + "' игрока '" + player.getName() + "' уже был сохранён");
            return;
        }

        newPlayerNicknames.add(nickName);
        playerNicknames.put(playerUUID, newPlayerNicknames);
        playerLastNickname.put(playerUUID, nickName);

        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.has(playerUUID.toString()) ?
                        playerObjects.getAsJsonObject(playerUUID.toString()) : new JsonObject();
                JsonArray playerNicknamesJson = playerObject.has("nicknames") ?
                        playerObject.getAsJsonArray("nicknames") : new JsonArray();
                playerNicknamesJson.add(nickName);
                playerObject.add("nicknames", playerNicknamesJson);
                playerObject.addProperty("last-nickname", nickName);
                playerObjects.add(player.getUniqueId().toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                WLog.info(plugin, "Успешно сохранён ник '" + nickName + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error(plugin, "Произошла ошибка при записи никнейма в файл 'playerData.json'");
                WLog.error(plugin, "Игрок: " + player.getName());
                WLog.error(plugin, "Сохраняемый никнейм: " + nickName);
                WLog.error(plugin, e.getMessage());
            }
        });
    }

    public void removePlayerNickname(@NotNull final OfflinePlayer player, @NotNull final String nickName)
    {
        if (!player.hasPlayedBefore())
            return;

        UUID playerUUID = player.getUniqueId();

        Set<String> newPlayerNicknames;

        if (playerNicknames.containsKey(playerUUID))
        {
            newPlayerNicknames = playerNicknames.get(playerUUID);
        }
        else
        {
            WLog.warn(plugin, "Игрок '" + player.getName() + "' не найден в базе данных никнеймов");
            return;
        }

        if (!newPlayerNicknames.contains(nickName))
        {
            WLog.warn(plugin, "Никнейм '" + nickName + "' игрока '" + player.getName() + "' не найден");
            return;
        }

        newPlayerNicknames.remove(nickName);
        playerNicknames.put(playerUUID, newPlayerNicknames);
        if (playerLastNickname.get(playerUUID).equals(nickName))
        {
            playerLastNickname.put(playerUUID, "");
        }


        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.getAsJsonObject(playerUUID.toString());
                JsonArray playerNicknamesJson = playerObject.getAsJsonArray("nicknames");
                for (JsonElement playerNicknameJson : playerNicknamesJson.asList())
                {
                    if (playerNicknameJson.getAsString().equals(nickName))
                    {
                        playerNicknamesJson.remove(playerNicknameJson);
                        break;
                    }
                }
                playerObject.add("nicknames", playerNicknamesJson);
                if (playerObject.get("last-nickname").getAsString().equals(nickName))
                {
                    playerObject.addProperty("last-nickname", "");
                }
                playerObjects.add(playerUUID.toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                WLog.info(plugin, "Успешно удалён ник '" + nickName + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error(plugin, "Произошла ошибка при удалении никнейма из файла 'playerData.json'");
                WLog.error(plugin, "Игрок: " + player.getName());
                WLog.error(plugin, "Удаляемый никнейм: " + nickName);
                WLog.error(plugin, e.getMessage());
            }
        });
    }

    public void clearPlayerNicknames(@NotNull final OfflinePlayer player)
    {
        if (!player.hasPlayedBefore())
            return;

        UUID playerUUID = player.getUniqueId();

        if (!playerNicknames.containsKey(playerUUID))
        {
            WLog.warn(plugin, "Игрок '" + player.getName() + "' не найден в базе данных никнеймов");
            return;
        }

        if (playerNicknames.get(playerUUID).isEmpty())
        {
            WLog.warn(plugin, "Игрок '" + player.getName() + "' не имеет никнеймов в базе данных");
            return;
        }

        playerNicknames.put(playerUUID, new HashSet<>());
        playerLastNickname.put(playerUUID, "");

        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.getAsJsonObject(playerUUID.toString());
                JsonArray playerNicknamesJson = new JsonArray();
                playerObject.add("nicknames", playerNicknamesJson);
                playerObject.addProperty("last-nickname", "");
                playerObjects.add(player.getUniqueId().toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                WLog.info(plugin, "Успешно очищены никнеймы игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error(plugin, "Произошла ошибка при очищении никнеймов из файла 'playerData.json'");
                WLog.error(plugin, "Игрок: " + player.getName());
                WLog.error(plugin, e.getMessage());
            }
        });
    }

    public void savePlayerIp(@NotNull final OfflinePlayer player, @NotNull final String ip)
    {
        if (!player.hasPlayedBefore())
            return;

        UUID playerUUID = player.getUniqueId();

        Set<String> newPlayerIps = new HashSet<>();

        if (playerIps.containsKey(playerUUID))
            newPlayerIps = playerIps.get(playerUUID);

        if (newPlayerIps.contains(ip))
        {
            WLog.warn(plugin, "IP '" + ip + "' игрока '" + player.getName() + "' уже был сохранён");
            return;
        }

        newPlayerIps.add(ip);
        playerIps.put(playerUUID, newPlayerIps);
        playerLastIp.put(playerUUID, ip);

        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.has(playerUUID.toString()) ?
                        playerObjects.getAsJsonObject(playerUUID.toString()) : new JsonObject();
                JsonArray playerIpsJson = playerObject.has("ips") ?
                        playerObject.getAsJsonArray("ips") : new JsonArray();
                playerIpsJson.add(ip);
                playerObject.add("ips", playerIpsJson);
                playerObject.addProperty("last-ip", ip);
                playerObjects.add(player.getUniqueId().toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                WLog.info(plugin, "Успешно сохранён IP '" + ip + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error(plugin, "Произошла ошибка при записи IP в файл 'playerData.json'");
                WLog.error(plugin, "Игрок: " + player.getName());
                WLog.error(plugin, "Сохраняемый IP: " + ip);
                WLog.error(plugin, e.getMessage());
            }
        });
    }

    public void removePlayerIps(@NotNull final OfflinePlayer player, @NotNull final String ip)
    {
        if (!player.hasPlayedBefore())
            return;

        UUID playerUUID = player.getUniqueId();

        Set<String> newPlayerIps;

        if (playerIps.containsKey(playerUUID))
        {
            newPlayerIps = playerIps.get(playerUUID);
        }
        else
        {
            WLog.warn(plugin, "Игрок '" + player.getName() + "' не найден в базе данных IP");
            return;
        }


        if (!newPlayerIps.contains(ip))
        {
            WLog.warn(plugin, "IP '" + ip + "' игрока '" + player.getName() + "' не найден");
            return;
        }

        newPlayerIps.remove(ip);
        playerIps.put(playerUUID, newPlayerIps);
        if (playerLastIp.get(playerUUID).equals(ip))
        {
            playerLastIp.put(playerUUID, "");
        }


        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.getAsJsonObject(playerUUID.toString());
                JsonArray playerIpsJson = playerObject.getAsJsonArray("ips");
                for (JsonElement playerIpJson : playerIpsJson.asList())
                {
                    if (playerIpJson.getAsString().equals(ip))
                    {
                        playerIpsJson.remove(playerIpJson);
                        break;
                    }
                }
                playerObject.add("ips", playerIpsJson);
                if (playerObject.get("last-ip").getAsString().equals(ip))
                {
                    playerObject.addProperty("last-ip", "");
                }
                playerObjects.add(player.getUniqueId().toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                WLog.info(plugin, "Успешно удалён IP '" + ip + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error(plugin, "Произошла ошибка при удалении IP из файла 'playerData.json'");
                WLog.error(plugin, "Игрок: " + player.getName());
                WLog.error(plugin, "Удаляемый IP: " + ip);
                WLog.error(plugin, e.getMessage());
            }
        });
    }

    public void clearPlayerIps(@NotNull final OfflinePlayer player)
    {
        if (!player.hasPlayedBefore())
            return;

        UUID playerUUID = player.getUniqueId();

        if (!playerIps.containsKey(playerUUID))
        {
            WLog.warn(plugin, "Игрок '" + player.getName() + "' не найден в базе данных IP");
            return;
        }

        if (playerIps.get(playerUUID).isEmpty())
        {
            WLog.warn(plugin, "Игрок '" + player.getName() + "' не имеет IP в базе данных");
            return;
        }

        playerIps.put(playerUUID, new HashSet<>());
        playerLastIp.put(playerUUID, "");

        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.getAsJsonObject(playerUUID.toString());
                JsonArray playerIpsJson = new JsonArray();
                playerObject.add("ips", playerIpsJson);
                playerObject.addProperty("last-ip", "");
                playerObjects.add(player.getUniqueId().toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                WLog.info(plugin, "Успешно очищены IP игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error(plugin, "Произошла ошибка при очищении IP из файла 'playerData.json'");
                WLog.error(plugin, "Игрок: " + player.getName());
                WLog.error(plugin, e.getMessage());
            }
        });
    }
}
