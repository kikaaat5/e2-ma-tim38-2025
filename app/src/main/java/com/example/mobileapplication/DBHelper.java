package com.example.mobileapplication;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

    public class DBHelper extends SQLiteOpenHelper {
        private static final String TAG = "DBHelper";
        private static final String DATABASE_NAME = "app.db";
        private static final int DATABASE_VERSION = 1;

        public static final String TABLE_USERS = "users";
        public static final String COL_ID = "id";
        public static final String COL_EMAIL = "email";
        public static final String COL_USERNAME = "username";
        public static final String COL_PASSWORD = "password"; // store hashed password
        public static final String COL_ISADMIN = "is_admin";
        public static final String COL_ACTIVATED = "activated";

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_EMAIL + " TEXT UNIQUE, " +
                    COL_USERNAME + " TEXT UNIQUE, " +
                    COL_PASSWORD + " TEXT, " +
                    COL_ISADMIN + " INTEGER DEFAULT 0, " +
                    COL_ACTIVATED + " INTEGER DEFAULT 1" +
                    ");";
            db.execSQL(createUsers);

            // Insert default admin user (only for testing)
            insertAdminIfNotExists(db);
        }

        private void insertAdminIfNotExists(SQLiteDatabase db) {
            // Admin credentials (example) - change if you want
            String adminEmail = "admin@example.com";
            String adminUsername = "admin";
            String adminPasswordPlain = "Admin123!"; // test password

            if (!userExistsByEmail(db, adminEmail)) {
                ContentValues cv = new ContentValues();
                cv.put(COL_EMAIL, adminEmail);
                cv.put(COL_USERNAME, adminUsername);
                cv.put(COL_PASSWORD, sha256(adminPasswordPlain)); // store hashed
                cv.put(COL_ISADMIN, 1);
                cv.put(COL_ACTIVATED, 1);
                long id = db.insert(TABLE_USERS, null, cv);
                Log.i(TAG, "Inserted admin user id=" + id);
            } else {
                Log.i(TAG, "Admin already exists, skipping insert");
            }
        }

        private boolean userExistsByEmail(SQLiteDatabase db, String email) {
            Cursor c = db.query(TABLE_USERS, new String[]{COL_ID},
                    COL_EMAIL + "=?", new String[]{email},
                    null, null, null);
            boolean exists = (c != null && c.getCount() > 0);
            if (c != null) c.close();
            return exists;
        }

        // Simple SHA-256 hash - acceptable for local testing only
        public static String sha256(String base) {
            try{
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(base.getBytes("UTF-8"));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if(hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString();
            } catch(Exception ex){
                throw new RuntimeException(ex);
            }
        }

        // Public helper for login check
        public boolean checkLogin(String emailOrUsername, String passwordPlain) {
            SQLiteDatabase db = this.getReadableDatabase();
            String hashed = sha256(passwordPlain);
            Cursor c = db.query(TABLE_USERS,
                    new String[]{COL_ID, COL_ISADMIN, COL_ACTIVATED},
                    "(" + COL_EMAIL + "=? OR " + COL_USERNAME + "=?) AND " + COL_PASSWORD + "=?",
                    new String[]{emailOrUsername, emailOrUsername, hashed},
                    null, null, null);

            boolean ok = false;
            if (c != null) {
                ok = c.getCount() > 0;
                c.close();
            }
            return ok;
        }

        // Optionally: fetch user by email
        public Cursor getUserByEmail(String email) {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.query(TABLE_USERS, null, COL_EMAIL + "=?", new String[]{email}, null, null, null);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // handle migrations as needed
        }
    }


