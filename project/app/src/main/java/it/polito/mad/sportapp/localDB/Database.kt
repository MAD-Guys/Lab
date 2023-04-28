package it.polito.mad.sportapp.localDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import java.util.concurrent.Executor
import java.util.concurrent.Executors
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

    abstract fun playgroundSportDao(): PlaygroundSportDao // PlaygroundSportDB


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Executors.newSingleThreadExecutor().execute {
                    INSTANCE.let {
                        it?.userDao()
                            ?.insert(User(0, "John", "Doe", "johndoe", "Male", 25, "Turin", "Bio"))
                        it?.sportCenterDao()?.insertAllSportCenter(
                            SportCenter(
                                0,
                                "Sport Center 1",
                                "Via Roma 1",
                                "Cool",
                                "123456789",
                                "08:00:00",
                                "20:00:00"
                            ),
                            SportCenter(
                                0,
                                "Sport Center 2",
                                "Via Roma 2",
                                "Cool",
                                "123456789",
                                "08:00:00",
                                "20:00:00"
                            )
                        )
                        it?.sportDao()?.insertAll(
                            Sport(0, "Tennis", 4),
                            Sport(0, "Basketball", 7),
                            Sport(0, "11-a-side-soccer", 11),
                            Sport(0, "Volleyball", 12)
                        )
                        it?.playgroundSportDao()?.insertAll(
                            PlaygroundSport(0, 1, 1, "Playground 1", 12.0F),
                            PlaygroundSport(0, 2, 1, "Playground 1 ", 12.0F),
                            PlaygroundSport(0, 3, 2, "Playground 3", 12.0F),
                            PlaygroundSport(0, 4, 2, "Playground 4", 12.0F)
                        )
                        it?.reservationDao()?.insertAll(
                            PlaygroundReservation(
                                0,
                                1,
                                1,
                                1,
                                1,
                                "2023-04-30T18:00:00",
                                "2023-04-30T18:30:00",
                                "2023-04-25T18:00:00",
                                22.5F
                            ),
                            PlaygroundReservation(
                                0,
                                2,
                                1,
                                2,
                                1,
                                "2023-04-29T18:00:00",
                                "2023-04-29T19:00:00",
                                "2023-04-25T18:00:00",
                                22.5F
                            ),
                            PlaygroundReservation(
                                0,
                                1,
                                1,
                                3,
                                2,
                                "2023-04-29T18:00:00",
                                "2023-04-29T19:30:00",
                                "2023-04-28T18:00:00",
                                22.5F
                            )
                        )
                    }
                }
            }
        }

        fun getInstance(context: Context): AppDatabase = (INSTANCE ?: synchronized(this) {
            val i = INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sportapp.db"
            ).addCallback(callback).build()

            INSTANCE = i
            INSTANCE
        })!!
    }
}

