package li.klass.fhem.dagger

import android.app.Application
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import dagger.Module
import dagger.Provides
import li.klass.fhem.AndFHEMDatabase
import li.klass.fhem.fcm.history.data.FcmHistoryEntity
import li.klass.fhem.fcm.history.data.change.FcmHistoryChangeDao
import li.klass.fhem.fcm.history.data.change.FcmHistoryChangeEntity
import li.klass.fhem.fcm.history.data.message.FcmHistoryMessageDao
import li.klass.fhem.fcm.history.data.message.FcmHistoryMessageEntity
import javax.inject.Singleton

@Module
class DatabaseModule(application: Application) {
    private val database = Room.databaseBuilder(application, AndFHEMDatabase::class.java, "application-db")
            .addMigrations(object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE ${FcmHistoryMessageEntity.tableName} ADD COLUMN ${FcmHistoryEntity.columnSaveDatetime} TEXT")
                    database.execSQL("ALTER TABLE ${FcmHistoryChangeEntity.tableName} ADD COLUMN ${FcmHistoryEntity.columnSaveDatetime} TEXT")
                }
            })
            .build()

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