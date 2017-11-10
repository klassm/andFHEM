package li.klass.fhem.dagger

import android.app.Application
import android.arch.persistence.room.Room
import dagger.Module
import dagger.Provides
import li.klass.fhem.AndFHEMDatabase
import li.klass.fhem.fcm.history.data.change.FcmHistoryChangeDao
import li.klass.fhem.fcm.history.data.message.FcmHistoryMessageDao
import javax.inject.Singleton

@Module
class DatabaseModule(application: Application) {
    private val database = Room.databaseBuilder(application, AndFHEMDatabase::class.java, "application-db").build()

    @Singleton
    @Provides
    fun provideRoomDatabase(): AndFHEMDatabase = database

    @Singleton
    @Provides
    fun provideFcmHistoryMessagesDao(): FcmHistoryMessageDao = database.getFcmHistoryMessagesDao()

    @Singleton
    @Provides
    fun provideFcmHistoryUpdatesDao(): FcmHistoryChangeDao = database.getFcmHistoryUpdatesDao()
}