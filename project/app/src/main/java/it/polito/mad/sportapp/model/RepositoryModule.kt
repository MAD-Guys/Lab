package it.polito.mad.sportapp.model

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {

    // Fire repository provider
    @Provides
    @Singleton
    fun provideRepository(): IRepository {
        return FireRepository.getInstance()
    }
}