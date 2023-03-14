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
    @Nullable
    <KeyType, RetType> RetType get(HashMap<KeyType, RetType> data, KeyType toGet);
    @Nullable
    <KeyType, RetType extends Collection> RetType getCollection(HashMap<KeyType, RetType> data, KeyType toGet);

}
