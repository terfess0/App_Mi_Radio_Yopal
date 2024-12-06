package com.terfess.miradioyopal.servicios.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "version"
)
data class Version(
    @PrimaryKey val idVersion: Int = 1,
    val numVersion: Int?
)
