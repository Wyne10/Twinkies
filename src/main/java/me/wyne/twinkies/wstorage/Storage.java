package me.wyne.twinkies.wstorage;

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
     * Get element from {@link HashMap}.
     * HashMap is used because data is often stored as key:value.
     * @param data {@link HashMap} to get element from
     * @param key Key of element to get from data
     * @return Element of {@link RetType} from data or null
     * @param <KeyType> Data {@link HashMap} key type
     * @param <RetType> Element return type
     */
    @Nullable
    <KeyType, RetType> RetType get(HashMap<KeyType, RetType> data, KeyType key);
    /**
     * Get element as collection from {@link HashMap}.
     * HashMap is used because data is often stored as key:value.
     * @param data {@link HashMap} to get element from
     * @param key Key of element to get from data
     * @return Element of {@link RetType} extends Collection from data or null
     * @param <KeyType> Data {@link HashMap} key type
     * @param <RetType> Element as collection return type
     */
    @Nullable
    <KeyType, RetType extends Collection> RetType getCollection(HashMap<KeyType, RetType> data, KeyType key);

}
