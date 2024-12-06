package com.terfess.miradioyopal.servicios.room

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.terfess.miradioyopal.servicios.room.model.Station
import com.terfess.miradioyopal.servicios.room.model.Version

@Dao
interface StationDao {
    @Query("SELECT * FROM station WHERE idStation=:id")
    fun getStation(id: Int): Station

    @Query("SELECT * FROM station")
    fun getAllStation():MutableList<Station>

    @Insert
    fun insertStation(radio: Station)

    //at update (delete and re write)
    @Query("DELETE FROM station")
    fun deleteStationTable()
}

@Dao
interface VersionDao {

    @Query("SELECT numVersion FROM version WHERE idVersion = 1")
    fun getVersion(): Int?

    @Update
    fun updateVersion(version: Version)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVersion(version: Version)
}

@Database(
    entities = [
        Station::class,
        Version::class
    ], version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao
    abstract fun versionDao(): VersionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database_radios"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}