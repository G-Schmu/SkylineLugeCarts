package com.previntagestudios.skylinelugecarts;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "SKYLINELUGECARTS";
        private static final int DATABASE_VERSION = 2;


        private static final String checksheetTable = "checksheet";
        private static final String cartlistTable = "cartlist";
        private static final String cartmanagerTable = "cartmanager";
        private static final String maintenanceTable = "maintenance";

        public DatabaseHelper (Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {

            database.execSQL("create table " + checksheetTable + " (checksheetID string PRIMARY KEY, amstaff string, pmstaff string, locked boolean) ");
            database.execSQL("create table " + cartmanagerTable + " (cartID int NOT NULL PRIMARY KEY, in_rotation boolean NOT NULL) ");
            database.execSQL("create table " + cartlistTable + " (checksheetID string NOT NULL, cartID int NOT NULL, in_rotation boolean NOT NULL, checked boolean default 0, " +
                    "overdue boolean default 0, maintenance boolean default 0, FOREIGN KEY(checksheetID) REFERENCES checksheet(checksheetID), FOREIGN KEY(cartID) REFERENCES carts(cartID))");
            database.execSQL("create table " + maintenanceTable + " (date string NOT NULL PRIMARY KEY, cartID int NOT NULL, formID int, reason string, comment string, " +
                    "FOREIGN KEY(cartID) REFERENCES carts(cartID), FOREIGN KEY(formID) REFERENCES checksheet(formID))");

            for(int i = 1; i<326; i++) {
                database.execSQL("INSERT INTO " + cartmanagerTable + " VALUES(" + i + ", 1)" );
            }
            ContentValues removefromrotation = new ContentValues();
            removefromrotation.put("in_rotation",false);
            int[] cartsoutofrotation = {1,8,14,23,34,46,49,53,67,81,84,87,
                    94,98,101,102,103,105,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,124,125,126,127,129,130,131,
                    133,134,135,137,138,139,140,144,147,159,162,169,171,172,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,
                    196,197,198,199,200,201,215,237,238,251,269,272,275,319};
            for(int i=0; i<cartsoutofrotation.length; i++)
                database.execSQL("UPDATE cartmanager SET in_rotation = 0 WHERE cartID = "+cartsoutofrotation[i]);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //Upgrade db if necessary
        }

    }
