package it.polito.mad.sportapp.LocalDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import it.polito.mad.sportapp.User
import it.polito.mad.sportapp.UserDao

@Database(entities = arrayOf(User::class, Sport::class, SportCenter::class, UserSport::class, Playground::class, PlaygroundSport::class, PlaygroundReservation::class, Equipment::class, EquipmentReservation::class),  version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sportDao(): SportDao
    abstract fun sportCenterDao(): SportCenterDao
    abstract fun userSportDao(): UserSportDao
    abstract fun playgroundDao(): PlaygroundDao
    abstract fun playgroundSportDao(): PlaygroundSportDao
    abstract fun playgroundReservationDao(): PlaygroundReservationDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun equipmentReservationDao(): EquipmentReservationDao



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
