package me.wyne.twinkies.wstorage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.wyne.twinkies.wlog.WLog;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
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

    @Nullable
    public <KeyType, RetType> RetType get(HashMap<KeyType, RetType> data, KeyType toGet)
    {
        if (!data.containsKey(toGet))
            return null;
        return data.get(toGet);
    }

    @Nullable
    public <KeyType, RetType extends Collection> RetType getCollection(HashMap<KeyType, RetType> data, KeyType toGet)
    {
        if (!data.containsKey(toGet) || data.get(toGet).isEmpty())
            return null;
        return data.get(toGet);
    }
}
