package me.wyne.twinkies.wstorage;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;

public interface Storage {

    /**
     * Create folder for storage file.
     */
    void createStorageFolder();
    /**
     * Create storage file.
     */
    void createStorageFile();
    /**
     * Load data from storage file.
     */
    void loadData();

    /**
     * Get element from {@link HashMap}. May be used to add extra logic to data query.
     * HashMap is used because data is often stored as key:value.
     * @param data {@link HashMap} to get element from
     * @param key {@link HashMap} key to get element from
     * @return Element of {@link ValType} from data or null
     * @param <KeyType> Data {@link HashMap} key type
     * @param <ValType> Data {@link HashMap} value type
     */
    @Nullable
    <KeyType, ValType> ValType get(@NotNull final HashMap<KeyType, ValType> data, @NotNull final KeyType key);
    /**
     * Get {@link Collection} from {@link HashMap}. May be used to add extra logic to data query.
     * HashMap is used because data is often stored as key:value.
     * @param data {@link HashMap} to get {@link Collection} from
     * @param key {@link HashMap} key to get {@link Collection} from
     * @return {@link Collection} from data or null
     * @param <KeyType> Data {@link HashMap} key type
     * @param <ValType> {@link Collection} value type
     */
    @Nullable
    <KeyType, ValType> Collection<ValType> getCollection(@NotNull final HashMap<KeyType, ? extends Collection<ValType>> data, @NotNull final KeyType key);

    /**
     * Save element to data {@link HashMap} and to storageFile to given path.
     * Set path as null to save value directly to key in storageFile.
     * Set data as null to save value only to storageFile.
     * @param data {@link HashMap} to save element to
     * @param key {@link HashMap} key to save element to
     * @param value Value to save
     * @param path Path in storageFile to save element to
     * @param <KeyType> Data {@link HashMap} key type
     * @param <ValType> Data {@link HashMap} value type
     */
    <KeyType, ValType> void save(@Nullable HashMap<KeyType, ValType> data, @NotNull final KeyType key, @NotNull final ValType value, @Nullable final String path);
    /**
     * Save element to {@link Collection} in data {@link HashMap} and to storageFile to given path.
     * @param data {@link HashMap} to save {@link Collection} to
     * @param key {@link HashMap} key to {@link Collection} to save element to
     * @param value Value to save
     * @param path Path to {@link com.google.gson.JsonArray} in storageFile to save element to
     * @return True/False if save succeed/failed
     * @param <KeyType> Data {@link HashMap} key type
     * @param <ValType> {@link Collection} value type
     * @param <ColType> Type of {@link Collection}
     */
    <KeyType, ValType, ColType extends Collection<ValType>> boolean saveCollection(@NotNull HashMap<KeyType, ColType> data, @NotNull final KeyType key, @NotNull final ValType value, @NotNull final String path);

    /**
     * Remove element from data {@link HashMap} and from storageFile from given path.
     * Set path as null to remove whole key from storageFile.
     * Set data as null to remove value only from storageFile.
     * @param data {@link HashMap} to remove element from
     * @param key {@link HashMap} key to remove element from
     * @param path Path in storageFile to remove element from
     * @return True/False if remove succeed/failed
     * @param <KeyType> Data {@link HashMap} key type
     * @param <ValType> Data {@link HashMap} value type
     */
    <KeyType, ValType> boolean remove(@Nullable HashMap<KeyType, ValType> data, @NotNull final KeyType key, @Nullable final String path);

    /**
     * Remove element from {@link Collection} in data {@link HashMap} and from storageFile from given path.
     * @param data {@link HashMap} to remove {@link Collection} element from
     * @param key {@link HashMap} key to {@link Collection} to remove element from
     * @param value Value to remove
     * @param path Path to {@link com.google.gson.JsonArray} in storageFile to remove element from
     * @return True/False if remove succeed/failed
     * @param <KeyType> Data {@link HashMap} key type
     * @param <ValType> {@link Collection} value type
     * @param <ColType> Type of {@link Collection}
     */
    <KeyType, ValType, ColType extends Collection<ValType>> boolean removeCollection(@NotNull HashMap<KeyType, ColType> data, @NotNull final KeyType key, @NotNull final ValType value, @NotNull final String path);

    /**
     * Clear {@link Collection} in data {@link HashMap} and in storageFile in given path.
     * @param data {@link HashMap} to clear {@link Collection} from
     * @param key {@link HashMap} key to {@link Collection} to clear
     * @param path Path to {@link com.google.gson.JsonArray} in storageFile to clear
     * @return True/False if clear succeed/failed
     * @param <KeyType> Data {@link HashMap} key type
     * @param <ValType> {@link Collection} value type
     * @param <ColType> Type of {@link Collection}
     */
    <KeyType, ValType, ColType extends Collection<ValType>> boolean clearCollection(@NotNull HashMap<KeyType, ColType> data, @NotNull final KeyType key, final String path);
}
