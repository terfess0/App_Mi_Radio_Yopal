package com.terfess.miradioyopal.servicios.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "station",
    indices = [androidx.room.Index(value = ["idStation"])]
)

data class Station(
    @PrimaryKey(autoGenerate = true) val idStation: Int = 0,
    var idDocument: String,
    var name: String,
    var linkStream: String,
    var linkFacebook: String,
    var photo: String
)