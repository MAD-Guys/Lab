package it.polito.mad.sportapp.localDB

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideSportDao(appDatabase: AppDatabase): SportDao {
        return appDatabase.sportDao()
    }

    @Provides
    @Singleton
    fun provideSportCenterDao(appDatabase: AppDatabase): SportCenterDao {
        return appDatabase.sportCenterDao()
    }

    @Provides
    @Singleton
    fun provideEquipmentDao(appDatabase: AppDatabase): EquipmentDao {
        return appDatabase.equipmentDao()
    }

    @Provides
    @Singleton
    fun provideReservationDao(appDatabase: AppDatabase): ReservationDao {
        return appDatabase.reservationDao()
    }

    // Database provider
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext : Context): AppDatabase? {
        return AppDatabase.getInstance(appContext)
    }

}