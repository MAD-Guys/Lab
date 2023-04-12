package it.polito.mad.lab2.LocalDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import it.polito.mad.lab2.User
import it.polito.mad.lab2.UserDao

@Database(entities = arrayOf(User::class, Sport::class, SportCenter::class, UserSport::class, Playground::class, PlaygroundSport::class, PlaygroundReservation::class, Equipment::class, EquipmentReservation::class),  version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao


    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            AppDatabase::class.java, "mad.db")
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
