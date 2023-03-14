package me.wyne.twinkies.storage;

import com.google.gson.*;
import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.wlog.WLog;
import me.wyne.twinkies.wstorage.JsonStorage;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

                    playerLastNickname.put(UUID.fromString(playerObject.getKey()), playerObject.getValue().getAsJsonObject().get("last-nickname").getAsString());
                    playerNicknames.put(UUID.fromString(playerObject.getKey()), newPlayerNicknames);
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
            WLog.warn("Никнейм '" + nickName + "' игрока '" + player.getName() + "' уже был сохранён");
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
                WLog.info("Успешно сохранён ник '" + nickName + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error("Произошла ошибка при записи никнейма в файл '" + storageFile.getName() + "'");
                WLog.error("Игрок: " + player.getName());
                WLog.error("Сохраняемый никнейм: " + nickName);
                WLog.error(e.getMessage());
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
            WLog.warn("Игрок '" + player.getName() + "' не найден в базе данных никнеймов");
            return;
        }

        if (!newPlayerNicknames.contains(nickName))
        {
            WLog.warn("Никнейм '" + nickName + "' игрока '" + player.getName() + "' не найден");
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
                WLog.info("Успешно удалён ник '" + nickName + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error("Произошла ошибка при удалении никнейма из файла '" + storageFile.getName() + "'");
                WLog.error("Игрок: " + player.getName());
                WLog.error("Удаляемый никнейм: " + nickName);
                WLog.error(e.getMessage());
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
            WLog.warn("Игрок '" + player.getName() + "' не найден в базе данных никнеймов");
            return;
        }

        if (playerNicknames.get(playerUUID).isEmpty())
        {
            WLog.warn("Игрок '" + player.getName() + "' не имеет никнеймов в базе данных");
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
                WLog.info("Успешно очищены никнеймы игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error("Произошла ошибка при очищении никнеймов из файла '" + storageFile.getName() + "'");
                WLog.error("Игрок: " + player.getName());
                WLog.error(e.getMessage());
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
            WLog.warn("IP '" + ip + "' игрока '" + player.getName() + "' уже был сохранён");
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
                WLog.info("Успешно сохранён IP '" + ip + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error("Произошла ошибка при записи IP в файл '" + storageFile.getName() + "'");
                WLog.error("Игрок: " + player.getName());
                WLog.error("Сохраняемый IP: " + ip);
                WLog.error(e.getMessage());
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
            WLog.warn("Игрок '" + player.getName() + "' не найден в базе данных IP");
            return;
        }


        if (!newPlayerIps.contains(ip))
        {
            WLog.warn("IP '" + ip + "' игрока '" + player.getName() + "' не найден");
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
                WLog.info("Успешно удалён IP '" + ip + "' игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error("Произошла ошибка при удалении IP из файла '" + storageFile.getName() + "'");
                WLog.error("Игрок: " + player.getName());
                WLog.error("Удаляемый IP: " + ip);
                WLog.error(e.getMessage());
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
            WLog.warn("Игрок '" + player.getName() + "' не найден в базе данных IP");
            return;
        }

        if (playerIps.get(playerUUID).isEmpty())
        {
            WLog.warn("Игрок '" + player.getName() + "' не имеет IP в базе данных");
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
                WLog.info("Успешно очищены IP игрока '" + player.getName() + "'");
            } catch (FileNotFoundException e) {
                WLog.error("Произошла ошибка при очищении IP из файла '" + storageFile.getName() + "'");
                WLog.error("Игрок: " + player.getName());
                WLog.error(e.getMessage());
            }
        });
    }
}
