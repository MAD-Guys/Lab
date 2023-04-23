package it.polito.mad.sportapp.localDB

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.polito.mad.sportapp.localDB.dao.EquipmentDao
import it.polito.mad.sportapp.localDB.dao.ReservationDao
import it.polito.mad.sportapp.localDB.dao.SportCenterDao
import it.polito.mad.sportapp.localDB.dao.SportDao
import it.polito.mad.sportapp.localDB.dao.UserDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    // DAO providers
    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideSportDao(appDatabase: AppDatabase): SportDao {
        return appDatabase.sportDao()
    }

    @Provides
    fun provideSportCenterDao(appDatabase: AppDatabase): SportCenterDao {
        return appDatabase.sportCenterDao()
    }

    @Provides
    fun provideEquipmentDao(appDatabase: AppDatabase): EquipmentDao {
        return appDatabase.equipmentDao()
    }

    @Provides
    fun provideReservationDao(appDatabase: AppDatabase): ReservationDao {
        return appDatabase.reservationDao()
    }

    // Database provider
    @Provides
    @Singleton
    fun provideDatabase(context: Context): AppDatabase? {
        return AppDatabase.getInstance(context)
    }

}