package me.wyne.twinkies.wstorage;

import com.google.gson.*;
import me.wyne.twinkies.wlog.WLog;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class JsonStorage implements Storage {

    protected final Plugin plugin;

    protected final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    protected final File storageFile;
    protected final ExecutorService executorService;

    public JsonStorage(@NotNull final Plugin plugin, @NotNull final String filePath)
    {
        this.plugin = plugin;
        storageFile = new File(plugin.getDataFolder(), filePath);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void createStorageFolder()
    {
        if (!plugin.getDataFolder().exists()) {
            WLog.info("Создание папки плагина...");
            plugin.getDataFolder().mkdirs();
            WLog.info("Папка плагина создана");
        }
    }

    public void createStorageFile()
    {
        if (!storageFile.exists()) {
            WLog.info("Создание файла '" + storageFile.getName() + "'...");
            try {
                if (storageFile.createNewFile()) {
                    PrintWriter writer = new PrintWriter(storageFile);
                    writer.write("{ }");
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                WLog.error("Произошла ошибка при создании файла '" + storageFile.getName() + "'");
                WLog.error(e.getMessage());
            }
            WLog.info("Файл '" + storageFile.getName() + "' создан");
        }
    }

    /**
     * Get element from {@link HashMap}. If HashMap doesn't have given key it will return null.
     * HashMap is used because data is often stored as key:value.
     * @param data {@link HashMap} to get element from
     * @param key {@link HashMap} key to get element from
     * @return Element of {@link ValType} from data or null
     * @param <KeyType> Data {@link HashMap} key type
     * @param <ValType> Data {@link HashMap} value type
     */
    @Nullable
    public <KeyType, ValType> ValType get(@NotNull final HashMap<KeyType, ValType> data, @NotNull final KeyType key)
    {
        if (!data.containsKey(key))
            return null;
        return data.get(key);
    }
    /**
     * Get {@link Collection} from {@link HashMap}. If HashMap doesn't have given key or retrieved {@link Collection} is empty it will return null.
     * HashMap is used because data is often stored as key:value.
     * @param data {@link HashMap} to get {@link Collection} from
     * @param key {@link HashMap} key to get {@link Collection} from
     * @return {@link Collection} from data or null
     * @param <KeyType> Data {@link HashMap} key type
     * @param <ValType> {@link Collection} value type
     */
    @Nullable
    public <KeyType, ValType> Collection<ValType> getCollection(@NotNull final HashMap<KeyType, ? extends Collection<ValType>> data, @NotNull final KeyType key)
    {
        if (!data.containsKey(key) || data.get(key).isEmpty())
            return null;
        return data.get(key);
    }

    public <KeyType, ValType> void save(@NotNull HashMap<KeyType, ValType> data, @NotNull final KeyType key, @NotNull final ValType value, @NotNull final String path)
    {
        data.put(key, value);

        executorService.execute(() -> {
            try
            {
                JsonObject datas = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject dataObject = datas.has(key.toString()) ?
                        datas.getAsJsonObject(key.toString()) : new JsonObject();
                dataObject.add(path, gson.toJsonTree(value));
                datas.add(key.toString(), dataObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(datas));
                writer.flush();
                writer.close();
                WLog.info("Сохранено значение '" + value + "' ключа '" + key + "' по пути '" + path + "'");
            }
            catch (FileNotFoundException e)
            {
                WLog.error("Произошла ошибка при записи значения в файл '" + storageFile.getName() + "'");
                WLog.error("Ключ: " + key);
                WLog.error("Значение: " + value);
                WLog.error("Путь: " + path);
                WLog.error(e.getMessage());
            }
        });
    }
    public <KeyType, ValType, ColType extends Collection<ValType>> boolean saveCollection(@NotNull HashMap<KeyType, ColType> data, @NotNull final KeyType key, @NotNull final ValType value, @NotNull final String path)
    {
        Collection<ValType> newCollection = new HashSet<ValType>();

        if (data.containsKey(key))
            newCollection = data.get(key);

        if (newCollection.contains(key))
        {
            WLog.warn("Значение '" + value + "' коллекции ключа '" + key + "' уже было сохранено");
            WLog.warn("Путь: " + path);
            return false;
        }

        newCollection.add(value);
        data.put(key, (ColType) newCollection);

        executorService.execute(() -> {
            try
            {
                JsonObject datas = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject dataObject = datas.has(key.toString()) ?
                        datas.getAsJsonObject(key.toString()) : new JsonObject();
                JsonArray collectionObject = dataObject.has(path) ?
                        dataObject.getAsJsonArray(path) : new JsonArray();
                collectionObject.add(gson.toJsonTree(value));
                dataObject.add(path, collectionObject);
                datas.add(key.toString(), dataObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(datas));
                writer.flush();
                writer.close();
                WLog.info("Сохранено значение '" + value + "' коллекции ключа '" + key + "'по пути '" + path + "'");
            }
            catch (FileNotFoundException e)
            {
                WLog.error("Произошла ошибка при записи значения в файл '" + storageFile.getName() + "'");
                WLog.error("Ключ: " + key);
                WLog.error("Значение: " + value);
                WLog.error("Путь: " + path);
                WLog.error(e.getMessage());
            }
        });
        return true;
    }

    public <KeyType, ValType> boolean remove(@NotNull HashMap<KeyType, ValType> data, @NotNull final KeyType key, @NotNull final String path)
    {
        if (!data.containsKey(key))
        {
            WLog.warn("Значение ключа '" + key + "' не найдено");
            WLog.warn("Путь: " + path);
            return false;
        }

        data.remove(key);
        executorService.execute(() -> {
            try {
                JsonObject datas = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject dataObject = datas.getAsJsonObject(key.toString());
                dataObject.remove(path);
                datas.add(key.toString(), dataObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(datas));
                writer.flush();
                writer.close();
                WLog.info("Удалено значение ключа '" + key + "' по пути '" + path + "'");
            } catch (FileNotFoundException e) {
                WLog.error("Произошла ошибка при удалении значения из файла '" + storageFile.getName() + "'");
                WLog.error("Ключ: " + key);
                WLog.error("Путь: " + path);
                WLog.error(e.getMessage());
            }
        });
        return true;
    }
    public <KeyType, ValType, ColType extends Collection<ValType>> boolean removeCollection(@NotNull HashMap<KeyType, ColType> data, @NotNull final KeyType key, @NotNull final ValType value, @NotNull final String path)
    {
        Collection<ValType> newCollection;

        if (data.containsKey(key))
        {
            newCollection = data.get(key);
        }
        else
        {
            WLog.warn("Значение ключа '" + key + "' не найдено");
            WLog.warn("Путь: " + path);
            return false;
        }

        if (!newCollection.contains(value))
        {
            WLog.warn("Значение '" + value + "' коллекции ключа '" + key + "' не найдено");
            WLog.warn("Путь: " + path);
            return false;
        }

        newCollection.remove(value);
        data.put(key, (ColType) newCollection);

        executorService.execute(() -> {
            try {
                JsonObject datas = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject dataObject = datas.getAsJsonObject(key.toString());
                JsonArray collectionObject = dataObject.getAsJsonArray(path);
                for (JsonElement collectionElement : collectionObject.asList())
                {
                    if (collectionElement.getAsString().equals(value.toString()))
                    {
                        collectionObject.remove(collectionElement);
                        break;
                    }
                }
                dataObject.add(path, collectionObject);
                datas.add(key.toString(), dataObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(datas));
                writer.flush();
                writer.close();
                WLog.info("Удалено значение '" + value + "' коллекции ключа '" + key + "' по пути '" + path + "'");
            } catch (FileNotFoundException e) {
                WLog.error("Произошла ошибка при удалении значения из файла '" + storageFile.getName() + "'");
                WLog.error("Ключ: " + key);
                WLog.error("Значение: " + value);
                WLog.error("Путь: " + path);
                WLog.error(e.getMessage());
            }
        });
        return true;
    }

    public <KeyType, ValType, ColType extends Collection<ValType>> boolean clearCollection(@NotNull HashMap<KeyType, ColType> data, @NotNull final KeyType key, @NotNull final String path)
    {
        if (!data.containsKey(key))
        {
            WLog.warn("Значение ключа '" + key + "' не найдено");
            WLog.warn("Путь: " + path);
            return false;
        }

        if (data.get(key).isEmpty())
        {
            WLog.warn("Коллекция ключа '" + key + "' не имеет элементов");
            WLog.warn("Путь: " + path);
            return false;
        }

        data.put(key, (ColType) Collections.emptySet());

        executorService.execute(() -> {
            try {
                JsonObject datas = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                JsonObject dataObject = datas.getAsJsonObject(key.toString());
                dataObject.add(path, new JsonArray());
                datas.add(key.toString(), dataObject);
                PrintWriter writer = new PrintWriter(storageFile);
                writer.write(gson.toJson(datas));
                writer.flush();
                writer.close();
                WLog.info("Очищена коллекция ключа '" + key + "' по пути '" + path + "'");
            } catch (FileNotFoundException e) {
                WLog.error("Произошла ошибка при удалении значения из файла '" + storageFile.getName() + "'");
                WLog.error("Ключ: " + key);
                WLog.error("Путь: " + path);
                WLog.error(e.getMessage());
            }
        });
        return true;
    }
}
