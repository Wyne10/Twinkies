package me.wyne.twinkies.wstorage;

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

}
