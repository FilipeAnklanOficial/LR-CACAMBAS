package com.lrcacambas.data.local.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseProvider {

    private static volatile AppDatabase INSTANCE;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE locacoes ADD COLUMN valorTotalCalculado INTEGER"
            );
        }
    };

    private DatabaseProvider() {
    }

    public static AppDatabase getDatabase(Context context) {

        if (INSTANCE == null) {
            synchronized (DatabaseProvider.class) {
                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "lr_cacambas_database"
                            )
                            .addMigrations(MIGRATION_1_2)
                            .build();

                }
            }
        }

        return INSTANCE;
    }
}