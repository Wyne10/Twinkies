package me.wyne.twinkies.storage;

import com.google.gson.*;
import me.wyne.twinkies.Twinkies;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class PlayerStorage {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Twinkies plugin;

    private final File storageFile;

    private final HashMap<UUID, Set<String>> playerNicknames = new HashMap<>();
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
            plugin.getDataFolder().mkdirs();
        }
    }

    /**
     * Creates and formats json file.
     */
    public void createStorageFile()
    {
        if (!storageFile.exists()) {
            try {
                if (storageFile.createNewFile()) {
                    PrintWriter writer = new PrintWriter(storageFile);
                    writer.write("{ }");
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Произошла ошибка при создании файла 'playerData.json'");
                plugin.getLogger().severe(e.getMessage());
            }
        }
    }

    @Nullable
    public Set<String> getPlayerNicknames(@NotNull final OfflinePlayer player)
    {
        if (!playerNicknames.containsKey(player.getUniqueId()))
            return null;
        return playerNicknames.get(player.getUniqueId());
    }

    @Nullable
    public Set<String> getPlayerIps(@NotNull final OfflinePlayer player)
    {
        if (!playerIps.containsKey(player.getUniqueId()))
            return null;
        return playerIps.get(player.getUniqueId());
    }

    public void loadData()
    {
        executorService.execute(() -> {
            try {
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

                    playerNicknames.put(UUID.fromString(playerObject.getKey()), newPlayerNicknames);
                    playerIps.put(UUID.fromString(playerObject.getKey()), newPlayerIps);
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.SEVERE, "Произошла ошибка при загрузке данных из файла 'playerData.json'");
                plugin.getLogger().log(Level.SEVERE, e.getMessage());
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
            plugin.getLogger().warning("Никнейм '" + nickName + "' игрока '" + player.getName() + "' уже был сохранён");
            return;
        }

        newPlayerNicknames.add(nickName);
        playerNicknames.put(player.getUniqueId(), newPlayerNicknames);

        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.has(player.getUniqueId().toString()) ?
                        playerObjects.getAsJsonObject(player.getUniqueId().toString()) : new JsonObject();
                JsonArray playerNicknamesJson = playerObject.has("nicknames") ?
                        playerObject.getAsJsonArray("nicknames") : new JsonArray();
                playerNicknamesJson.add(nickName);
                playerObject.add("nicknames", playerNicknamesJson);
                playerObjects.add(player.getUniqueId().toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                plugin.getLogger().info("Успешно сохранён ник '" + nickName + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                plugin.getLogger().severe("Произошла ошибка при записи никнейма в файл 'playerData.json'");
                plugin.getLogger().severe("Игрок: " + player.getName());
                plugin.getLogger().severe("Сохраняемый никнейм: " + nickName);
                plugin.getLogger().severe(e.getMessage());
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
            plugin.getLogger().warning("Игрок '" + player.getName() + "' не найден в базе данных никнеймов");
            return;
        }

        if (!newPlayerNicknames.contains(nickName))
        {
            plugin.getLogger().warning("Никнейм '" + nickName + "' игрока '" + player.getName() + "' не найден");
            return;
        }

        newPlayerNicknames.remove(nickName);
        playerNicknames.put(player.getUniqueId(), newPlayerNicknames);

        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.getAsJsonObject(player.getUniqueId().toString());
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
                playerObjects.add(player.getUniqueId().toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                plugin.getLogger().info("Успешно удалён ник '" + nickName + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                plugin.getLogger().severe("Произошла ошибка при удалении никнейма из файла 'playerData.json'");
                plugin.getLogger().severe("Игрок: " + player.getName());
                plugin.getLogger().severe("Удаляемый никнейм: " + nickName);
                plugin.getLogger().severe(e.getMessage());
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
            plugin.getLogger().warning("Игрок '" + player.getName() + "' не найден в базе данных никнеймов");
            return;
        }

        if (playerNicknames.get(playerUUID).isEmpty())
        {
            plugin.getLogger().warning("Игрок '" + player.getName() + "' не имеет никнеймов в базе данных");
            return;
        }

        playerNicknames.put(player.getUniqueId(), new HashSet<>());

        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.getAsJsonObject(player.getUniqueId().toString());
                JsonArray playerNicknamesJson = new JsonArray();
                playerObject.add("nicknames", playerNicknamesJson);
                playerObjects.add(player.getUniqueId().toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                plugin.getLogger().info("Успешно очищены никнеймы игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                plugin.getLogger().severe("Произошла ошибка при очищении никнеймов из файла 'playerData.json'");
                plugin.getLogger().severe("Игрок: " + player.getName());
                plugin.getLogger().severe(e.getMessage());
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
            plugin.getLogger().warning("IP '" + ip + "' игрока '" + player.getName() + "' уже был сохранён");
            return;
        }

        newPlayerIps.add(ip);
        playerIps.put(player.getUniqueId(), newPlayerIps);

        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.has(player.getUniqueId().toString()) ?
                        playerObjects.getAsJsonObject(player.getUniqueId().toString()) : new JsonObject();
                JsonArray playerIpsJson = playerObject.has("ips") ?
                        playerObject.getAsJsonArray("ips") : new JsonArray();
                playerIpsJson.add(ip);
                playerObject.add("ips", playerIpsJson);
                playerObjects.add(player.getUniqueId().toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                plugin.getLogger().info("Успешно сохранён IP '" + ip + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                plugin.getLogger().severe("Произошла ошибка при записи IP в файл 'playerData.json'");
                plugin.getLogger().severe("Игрок: " + player.getName());
                plugin.getLogger().severe("Сохраняемый IP: " + ip);
                plugin.getLogger().severe(e.getMessage());
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
            plugin.getLogger().warning("Игрок '" + player.getName() + "' не найден в базе данных IP");
            return;
        }


        if (!newPlayerIps.contains(ip))
        {
            plugin.getLogger().warning("IP '" + ip + "' игрока '" + player.getName() + "' не найден");
            return;
        }

        newPlayerIps.remove(ip);
        playerIps.put(player.getUniqueId(), newPlayerIps);

        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.getAsJsonObject(player.getUniqueId().toString());
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
                playerObjects.add(player.getUniqueId().toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                plugin.getLogger().info("Успешно удалён IP '" + ip + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                plugin.getLogger().severe("Произошла ошибка при удалении IP из файла 'playerData.json'");
                plugin.getLogger().severe("Игрок: " + player.getName());
                plugin.getLogger().severe("Удаляемый IP: " + ip);
                plugin.getLogger().severe(e.getMessage());
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
            plugin.getLogger().warning("Игрок '" + player.getName() + "' не найден в базе данных IP");
            return;
        }

        if (playerIps.get(playerUUID).isEmpty())
        {
            plugin.getLogger().warning("Игрок '" + player.getName() + "' не имеет IP в базе данных");
            return;
        }

        playerIps.put(player.getUniqueId(), new HashSet<>());

        executorService.execute(() -> {
            try {
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject playerObject = playerObjects.getAsJsonObject(player.getUniqueId().toString());
                JsonArray playerIpsJson = new JsonArray();
                playerObject.add("ips", playerIpsJson);
                playerObjects.add(player.getUniqueId().toString(), playerObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(playerObjects));
                writer.flush();
                writer.close();
                plugin.getLogger().info("Успешно очищены IP игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                plugin.getLogger().severe("Произошла ошибка при очищении IP из файла 'playerData.json'");
                plugin.getLogger().severe("Игрок: " + player.getName());
                plugin.getLogger().severe(e.getMessage());
            }
        });
    }
}
