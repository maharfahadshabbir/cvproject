package com.example.cvmaker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cvmaker.data.dao.DatabaseDaos
import com.example.cvmaker.model.ThumbNailsFavoriteModel
import com.example.cvmaker.model.profilemodels.FavoriteModel
import com.example.cvmaker.model.profilemodels.RecycleBin
import com.example.cvmaker.model.profilemodels.profile.ProfileDb
import com.example.cvmaker.typeConvertor.CvModelRequestConverter
import com.example.cvmaker.typeConvertor.CvModelRequestEntity

@Database(entities = [CvModelRequestEntity::class, FavoriteModel::class, RecycleBin::class, ThumbNailsFavoriteModel::class, ProfileDb::class], version =5, exportSchema = false)
@TypeConverters(CvModelRequestConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun databaseDaos(): DatabaseDaos // Add this line
}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE cv_model_requests ADD COLUMN age INTEGER")    }
}
