package it.polito.mad.sportapp.localDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Provides
import it.polito.mad.sportapp.entities.Equipment
import it.polito.mad.sportapp.entities.EquipmentReservation
import it.polito.mad.sportapp.entities.PlaygroundReservation
import it.polito.mad.sportapp.entities.PlaygroundSport
import it.polito.mad.sportapp.entities.SportCenter
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.UserSport
import it.polito.mad.sportapp.localDB.dao.*
import javax.inject.Singleton


@Database(
    entities = [User::class,
        Sport::class,
        SportCenter::class,
        UserSport::class,
        PlaygroundSport::class,
        PlaygroundReservation::class,
        Equipment::class,
        EquipmentReservation::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao // UserDB
    abstract fun sportDao(): SportDao // SportDB
    abstract fun sportCenterDao(): SportCenterDao // SportCenter

    abstract fun equipmentDao(): EquipmentDao // EquipmentDB

    abstract fun reservationDao(): ReservationDao // PlaygroundDB


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

         fun getInstance(context: Context): AppDatabase? = (INSTANCE ?: synchronized(this) {
            val i = INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sportapp.db"
            ).build()
            INSTANCE = i
            INSTANCE

        })!!


    }

}

