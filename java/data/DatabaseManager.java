/*
 * =====================================================
 * ANDROID INVENTORY MANAGEMENT APP
 * Ed Morrow
 * Southern New Hampshire University
 * CS499: Computer Science Capstone
 * Milestone 3: Databases Code Enhancement
 * Prof Brooke Goggin
 * 8 June 2023
 * =====================================================
 */

package com.example.cs499enhancedandroidapp.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.DatabaseUtils;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.Context;

import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/*
 * =====================================================
 * The database manager
 * =====================================================
 */
public class DatabaseManager extends SQLiteOpenHelper {

    private static DatabaseManager instance;
    private static final String DATABASE_NAME = "data.db";
    private static final int VERSION = 1;

    private static final class LoginDetailsTable {
        private static final String TABLE = "loginDetails";
        private static final String COL_ID = "_id";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD = "password";
        private static final String COL_ACTIVE = "active";
        private static final String COL_NOTIFICATIONS = "notifications";
        private static final String COL_SALT = "salt";
        private static final String COL_TIMESTAMP = "timestamp";
        private static final String COL_PASSCODE = "passcode";
    }

    private static final class InventoryDetailsTable {
        private static final String TABLE = "inventoryDetails";
        private static final String COL_ID = "_id";
        private static final String COL_DESCRIPTION = "description";
        private static final String COL_QUANTITY = "quantity";
    }

    /*
     * Method returns singleton instance of DatabaseManager
     * @param Context context: the context
     * @return: @DatabaseManger singleton instance
     */
    public static DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + LoginDetailsTable.TABLE + " (" +
                LoginDetailsTable.COL_ID + " integer primary key autoincrement, " +
                LoginDetailsTable.COL_USERNAME + " text, " +
                LoginDetailsTable.COL_PASSWORD + " text," +
                LoginDetailsTable.COL_ACTIVE + " integer," +
                LoginDetailsTable.COL_NOTIFICATIONS + " integer," +
                LoginDetailsTable.COL_SALT + " text, " +
                LoginDetailsTable.COL_TIMESTAMP + " text, " +
                LoginDetailsTable.COL_PASSCODE + " text)");

        db.execSQL("CREATE TABLE " + InventoryDetailsTable.TABLE + " (" +
                InventoryDetailsTable.COL_ID + " integer primary key autoincrement, " +
                InventoryDetailsTable.COL_DESCRIPTION + " text, " +
                InventoryDetailsTable.COL_QUANTITY + " integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + LoginDetailsTable.TABLE);
        db.execSQL("drop table if exists " + InventoryDetailsTable.TABLE);
        onCreate(db);
    }

    /*
     * method appends salt to password
     * @param String password: the unsalted password
     * @return String salted: the salted password
     */
    public String getSalt(String username) {

        String salt = "";

        SQLiteDatabase db = getReadableDatabase();

        String sql = "Select " + LoginDetailsTable.COL_SALT + " from " + LoginDetailsTable.TABLE +
                " WHERE " + LoginDetailsTable.COL_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{username});

        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            salt += cursor.getString(cursor.getColumnIndexOrThrow(LoginDetailsTable.COL_SALT));
        }
        cursor.close();

        return salt;
    }

    /*
     * Code for this method from Baeldung (2021). SHA-256 and SHA3-256 Hashing in Java.
     * Retrieved from https://www.baeldung.com/sha-256-hashing-java
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /*
     * method returns hashed version of salted password
     * @param String salted_password: the password with salt already appended
     * @return String hashed: the salted and hashed password
     */
    public static String hash(String salted_password) {

        String hashedString = salted_password;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(salted_password.getBytes(StandardCharsets.UTF_8));
            hashedString = bytesToHex(hashed);
            return hashedString;
        }

        catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown for incorrect algorithm: " + e);
        }

        return hashedString;
    }

    /*
     * Method authenticates user credentials
     * @param String username: the username
     * @param String password: the password
     * @return boolean isAuthenticated: true is authenticated, false is not
     */
    public boolean authenticateLogin(String username, String password) {
        boolean isAuthenticated = false;
        String salt = getSalt(username);
        String saltedPassword = password + salt;
        String hashedSaltedPassword = hash(saltedPassword);

        SQLiteDatabase db = getReadableDatabase();

        String sql = "Select * from " + LoginDetailsTable.TABLE +
                " WHERE " + LoginDetailsTable.COL_USERNAME + " = ? AND " +
                LoginDetailsTable.COL_PASSWORD + " = ? ";

        Cursor cursor = db.rawQuery(sql, new String[]{username, hashedSaltedPassword});
        if (cursor.moveToFirst()) {
            isAuthenticated = true;
        }
        cursor.close();

        // indicate which user is currently active in the database
        ContentValues values = new ContentValues();
        // first set all values to zero
        values.put(LoginDetailsTable.COL_ACTIVE, 0);
        db.update(LoginDetailsTable.TABLE, values, null, null);
        // then set the value corresponding to the active user to 1
        values.put(LoginDetailsTable.COL_ACTIVE, 1);
        db.update(LoginDetailsTable.TABLE, values,  LoginDetailsTable.COL_USERNAME + "= ?",
                new String[] {username});

        // CODE TO CREATE RANDOM SIX-DIGIT CODE AND ADD IT AND ITS TIME-STAMP TO DATABASE

        return isAuthenticated;
    }

    /*
     * method verifies username exists in database
     * @param String username - the username
     * @return Boolean isVerified: true is verified, false is not
     */
    public boolean verifyUsername(String username) {
        boolean isVerified = false;

        SQLiteDatabase db = getReadableDatabase();

        String sql = "Select * from " + LoginDetailsTable.TABLE +
                " WHERE " + LoginDetailsTable.COL_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{username});
        if (cursor.moveToFirst()) {
            isVerified = true;
        }
        cursor.close();

        return isVerified;
    }

    /*
     * method generates random six-digit passcode and adds to database
     * together with a timestamp of its creation
     */
    public void setPasscode() {

        Random random = new Random();
        int upperbound = 999999;

        int code = random.nextInt(upperbound) + 1;
        String passcode = Integer.toString(code);
        while (passcode.length() < 6) {
            passcode = "0" + passcode;
        }
        long timestamp = System.currentTimeMillis();

        SQLiteDatabase db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(LoginDetailsTable.COL_PASSCODE, passcode);
        values.put(LoginDetailsTable.COL_TIMESTAMP, timestamp);
        db.update(LoginDetailsTable.TABLE, values,  LoginDetailsTable.COL_ACTIVE + "= ?",
                new String[] {String.valueOf(1)});
    }

    /*
     * method returns passcode of active user
     * @return String passcode: the passcode generated for the active user
     */
    public String getPasscode() {

        String passcode = "000000";

        SQLiteDatabase db = getReadableDatabase();

        String sql = "Select " + LoginDetailsTable.COL_PASSCODE + " from " + LoginDetailsTable.TABLE +
                " WHERE " + LoginDetailsTable.COL_ACTIVE + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(1)});

        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            passcode = cursor.getString(cursor.getColumnIndexOrThrow(LoginDetailsTable.COL_PASSCODE));
        }
        cursor.close();

        return passcode;
    }

    /*
     * method returns timestamp of (current) passcode generated for the active user
     * @return long timestamp: the timestamp from the passcode
     */
    public long getTimestamp() {

        long timestamp = 0;

        SQLiteDatabase db = getReadableDatabase();

        String sql = "Select " + LoginDetailsTable.COL_TIMESTAMP + " from " + LoginDetailsTable.TABLE +
                " WHERE " + LoginDetailsTable.COL_ACTIVE + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(1)});

        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            timestamp = cursor.getInt(cursor.getColumnIndexOrThrow(LoginDetailsTable.COL_TIMESTAMP));
        }
        cursor.close();

        return timestamp;
    }

    /*
     * Method authenticates user passcode
     * @param String code: the code the user entered
     * @return boolean isAuthenticated: true is authenticated, false is not
     */
    public boolean authenticateSMS(String code) {

        boolean isAuthenticated = false;

        String passcode = getPasscode();
        long passcodeCreated = getTimestamp();
        long codeEntered = System.currentTimeMillis();

        // user has 3 minutes (= 180,000ms) to enter correct code
        if (!code.equals("000000") & code.equals(passcode)) {
            isAuthenticated = true;
            if (passcodeCreated != 0 && codeEntered - passcodeCreated < 180000) {
                isAuthenticated = true;
            }
        }
        return isAuthenticated;
    }

    /*
     * method adds a new user to the database
     * @param String username: the new user's username
     * @param String password: the user's password
     */
    public void addUser(String username, String salt, String password) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LoginDetailsTable.COL_USERNAME, username);
        values.put(LoginDetailsTable.COL_PASSWORD, password);
        values.put(LoginDetailsTable.COL_SALT, salt);
        values.put(LoginDetailsTable.COL_ACTIVE, 0);
        values.put(LoginDetailsTable.COL_NOTIFICATIONS, 0);

        db.insert(LoginDetailsTable.TABLE, null, values);
    }

    /*
     * returns status of notifications permission
     * @return Boolean enable: true indicates notifications enabled, false disabled
     */
    public boolean getNotificationStatus() {

        // assume false until verified true
        boolean enabled = false;

        SQLiteDatabase db = getReadableDatabase();

        String sql = "Select " + LoginDetailsTable.COL_NOTIFICATIONS + " from " + LoginDetailsTable.TABLE +
                " WHERE " + LoginDetailsTable.COL_ACTIVE + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(1)});

        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            enabled = cursor.getInt(cursor.getColumnIndexOrThrow(LoginDetailsTable.COL_NOTIFICATIONS)) == 1;
        }
        cursor.close();

        return enabled;
    }

    /*
     * method sets users notifications permission in the database
     * @param boolean enabled: true indicates notifications enabled, false disabled
     */
    public void setNotificationStatus(boolean enabled) {

        int isEnabled = 0;
        if (enabled) {
            isEnabled = 1;
        }

        SQLiteDatabase db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(LoginDetailsTable.COL_NOTIFICATIONS, isEnabled);
        db.update(LoginDetailsTable.TABLE, values,  LoginDetailsTable.COL_ACTIVE + "= ?",
                new String[] {String.valueOf(1)});
    }

    /*
     * method verifies an item exists in the database
     * @param String description: the item description
     * @return boolean exists: true indicates item exists, false that it does not
     */
    public boolean itemExists(String description) {
        boolean exists = false;

        SQLiteDatabase db = getReadableDatabase();

        String sql = "Select * from " + InventoryDetailsTable.TABLE +
                " WHERE " + InventoryDetailsTable.COL_DESCRIPTION + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{description});
        if (cursor.moveToFirst()) {
            exists = true;
        }
        cursor.close();

        return exists;
    }

    /*
     * method returns quantity of item
     * @param String id: the item's id
     * @return int quantity: the item's quantity
     */
    public String getQuantityById(String id) {

        // return zero if id not found
        int quantity = 0;

        SQLiteDatabase db = getReadableDatabase();

        String sql = "Select " + InventoryDetailsTable.COL_QUANTITY + " from " + InventoryDetailsTable.TABLE +
                " WHERE " + InventoryDetailsTable.COL_ID + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{id});

        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDetailsTable.COL_QUANTITY));
        }
        cursor.close();

        return String.valueOf(quantity);
    }

    /*
     * method returns description of item
     * @param String id: the item's id
     * @return String description: description of the item
     */
    public String getDescriptionById(String id) {

        // return empty string if id not found
        String description = "";

        SQLiteDatabase db = getReadableDatabase();

        String sql = "Select " + InventoryDetailsTable.COL_DESCRIPTION + " from " + InventoryDetailsTable.TABLE +
                " WHERE " + InventoryDetailsTable.COL_ID + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{id});

        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            description = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDetailsTable.COL_DESCRIPTION));
        }
        cursor.close();

        return description;
    }

    /*
     * method returns total number of items in database
     * @return int items: the number of items
     */
    public long countEntries() {

        SQLiteDatabase db = getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, InventoryDetailsTable.TABLE, InventoryDetailsTable.COL_QUANTITY + " > ?"
                , new String[] {String.valueOf(0)});
    }

    /*
     * method returns highest id key in database
     * @return int highestID: the highest id number
     */
    public int getHighestId() {

        SQLiteDatabase db = getReadableDatabase();
        return (int) DatabaseUtils.longForQuery(db, "SELECT MAX(" + InventoryDetailsTable.COL_ID + ") FROM " + InventoryDetailsTable.TABLE, null);
    }

    /*
     * method adds a new item to the inventory
     * @param String description: the item description
     * @param String quantity: the quantity of the item
     */
    public void addInventoryItem(String description, String quantity) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(InventoryDetailsTable.COL_DESCRIPTION, description);
        values.put(InventoryDetailsTable.COL_QUANTITY, quantity);

        db.insert(InventoryDetailsTable.TABLE, null, values);
    }

    /*
     * method updates the quantity of an item already in the database
     * @param String description: the item description
     * @param String quantity: the (new) quantity of the item
     */
    public void updateInventoryItem(String description, String quantity) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(InventoryDetailsTable.COL_QUANTITY, quantity);

        db.update(InventoryDetailsTable.TABLE, values, InventoryDetailsTable.COL_DESCRIPTION + "= ?",
                new String[] {description});
    }

    /*
     * method removes an item from the database
     * @param long id: the item's id number
     */
    public void deleteInventoryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(InventoryDetailsTable.TABLE, InventoryDetailsTable.COL_ID + " =?",
                new String[] { Long.toString(id) });
    }
}