package com.terfess.miradioyopal.chat_radio.model

data class ChatMessage(
    val user: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
