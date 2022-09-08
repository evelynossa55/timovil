package com.cm.timovil2.backup;

import java.io.File;
import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 27/06/18.
 */

interface IBackUp {

    /**
     * It should be called async, create a json backUp inside external storage directory
     * @throws Exception if error
     */
    void makeBackUp() throws Exception;

    /**
     * Create a json backUp inside external storage directory, with the data
     * that could not be uploaded after sync
     * @param data is a list of the objects, each of one tells if was successfully uploaded to cloud
     *             or not
     * @throws Exception if error
     */
    <T> void makeBackUpAfterSync(ArrayList<T> data) throws Exception;

    /**
     * Read a json File, and returns a json string containing
     * current entries (tha are going to be put in the backup file), and
     * the ones already in the backup file
     * @param file json
     * @return String representation of the json file
     * @throws Exception if error
     */
    String getJsonFromFile(File file) throws Exception;

    /**
     * Read json back up file, creates an array of objects needed with the data from the file
     * and then returns the array
     * @param file json
     * @param <T> Object type
     * @return Array of object type
     * @throws Exception if error
     */
    <T> ArrayList<T> getLastBackUp(File file) throws Exception;

    /**
     * Iterate over sqliteData and search repeated entries in backUpData, if repeated then deletes
     * the entrie from backUpData
     * @param sqliteEntries data already in local sqlite
     * @param backUpEntries data already in backUp file
     * @param <T> object type
     * @return An ArrayList ob the object type, with no repeated entries
     * @throws Exception If error
     */
    <T> ArrayList<T> deleteRepeatedEntriesInBackUpList(ArrayList<T> sqliteEntries, ArrayList<T> backUpEntries) throws Exception;

    /**
     * Filter only pendent entries. Entries that need to be send to cloud
     * @param entries data
     * @param <T> object type
     * @return filtered entries
     * @throws Exception If error
     */
    <T> ArrayList<T> getOnlyPendentEntries(ArrayList<T> entries) throws Exception;

    /**
     * Iterates over the entries, and create a Json String
     * @param entries data
     * @param <T> object type
     * @return Json String representation of the entries
     * @throws Exception If error
     */
    <T> String createJsonFromEntries(ArrayList<T> entries) throws Exception;

    /**
     * Sync the entries stored in json backup to cloud
     */
    void syncBackup();

}
