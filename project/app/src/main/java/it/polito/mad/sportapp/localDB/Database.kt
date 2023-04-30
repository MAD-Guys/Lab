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
    entities = [User::class, Sport::class, SportCenter::class, UserSport::class, PlaygroundSport::class, PlaygroundReservation::class, Equipment::class, EquipmentReservation::class],
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
                                "PlayHard Sports Arena",
                                "Via Roma 1, Turin",
                                "PlayHard Sports Arena is where champions are made. This arena features top-quality sports facilities, professional coaching, and a competitive atmosphere for athletes who are serious about their game. Whether it's team sports or individual disciplines, PlayHard Sports Arena provides the ideal platform for athletes to showcase their skills and strive for greatness.",
                                "123456789",
                                "08:00:00",
                                "20:00:00"
                            ),
                            SportCenter(
                                0,
                                "FitZone Sports Facility",
                                "Via Sacchi 2, Turin",
                                "t FitZone Sports Facility, fitness and sports excellence go hand in hand. This facility is equipped with state-of-the-art fitness equipment, dedicated training areas, and professional coaches to help individuals reach their peak performance. From individual workouts to team training sessions, FitZone provides the ideal environment for athletes to push their limits and achieve their fitness goals.",
                                "123456789",
                                "08:00:00",
                                "20:00:00"
                            ),
                            SportCenter(
                                0,
                                "Sportsville Recreation Center",
                                "Via Cavour 27, Turin",
                                " Recreation Center is the go-to place for recreational sports and leisure activities. Whether you're a seasoned athlete or simply looking to unwind, this center offers a wide array of options, from casual games to organized tournaments. Discover the thrill of sports and enjoy quality leisure time at Sportsville Recreation Center.",
                                "123456789",
                                "08:00:00",
                                "20:00:00"
                            ),
                            SportCenter(
                                0,
                                "PlayZone Sports Center",
                                "Via Zante 94, Turin",
                                "Step into a world of excitement and play at PlayZone Sports Center. Offering a variety of indoor and outdoor sports options, including basketball, tennis, and volleyball, this center provides a vibrant and dynamic environment where people of all ages can come together to enjoy sports, build friendships, and lead an active lifestyle.",
                                "123456789",
                                "08:00:00",
                                "20:00:00"
                            ),

                            )
                        it?.sportDao()?.insertAll(
                            Sport(0, "Tennis", 4),
                            Sport(0, "Table Tennis", 2),
                            Sport(0, "Padel", 4),
                            Sport(0, "Basketball", 14),
                            Sport(0, "11-a-side-soccer", 22),
                            Sport(0, "Volleyball", 12),
                            Sport(0, "Beach Volley", 10),
                            Sport(0, "5-a-side-soccer", 10),
                            Sport(0, "8-a-side-soccer", 16),
                            Sport(0, "Mini Golf", 2),
                        )
                        it?.playgroundSportDao()?.insert(
                            PlaygroundSport(0, 1, 1, "Tennis Court", 12.0F)
                        )
                        it?.playgroundSportDao()?.insert(
                            PlaygroundSport(0, 1, 1, "Tennis Court 2", 12.0F)
                        )
                        it?.playgroundSportDao()?.insert(
                            PlaygroundSport(0, 2, 2, "Table Tennis Arena", 12.0F)
                        )
                        it?.playgroundSportDao()?.insert(
                            PlaygroundSport(0, 2, 3, "Table Magic", 12.0F)
                        )
                        it?.playgroundSportDao()?.insert(
                            PlaygroundSport(0, 3, 4, "Table Magic", 12.0F)
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
                            ), PlaygroundReservation(
                                0,
                                2,
                                1,
                                1,
                                1,
                                "2023-04-29T18:00:00",
                                "2023-04-29T19:00:00",
                                "2023-04-25T18:00:00",
                                22.5F
                            ), PlaygroundReservation(
                                0,
                                3,
                                1,
                                2,
                                2,
                                "2023-04-29T18:00:00",
                                "2023-04-29T19:30:00",
                                "2023-04-28T18:00:00",
                                22.5F
                            ),
                            PlaygroundReservation(
                                0,
                                4,
                                1,
                                2,
                                3,
                                "2023-04-30T19:00:00",
                                "2023-04-30T20:30:00",
                                "2023-04-28T18:00:00",
                                55.50F
                            ),
                            PlaygroundReservation(
                                0,
                                5,
                                1,
                                3,
                                4,
                                "2023-05-05T08:30:00",
                                "2023-05-05T09:30:00",
                                "2023-04-28T18:00:00",
                                50F
                            )
                        )
                    }
                }
            }
        }

        fun getInstance(context: Context): AppDatabase = (INSTANCE ?: synchronized(this) {
            val i = INSTANCE ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "sportapp.db"
            ).addCallback(callback).build()

            INSTANCE = i
            INSTANCE
        })!!
    }
}

