package com.terfess.miradioyopal.recycler_view

data class DatoStation(
    var name : String,
    var linkStream : String,
    var linkFacebook : String,
    var photo : String
)

data class DatoStationLocal(
    var id : Int,
    var name : String,
    var linkStream : String,
    var linkFacebook : String,
    var photo : String
)
