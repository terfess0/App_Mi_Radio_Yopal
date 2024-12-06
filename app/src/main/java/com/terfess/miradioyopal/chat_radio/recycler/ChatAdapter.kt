package com.terfess.miradioyopal.chat_radio.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.terfess.miradioyopal.R
import com.terfess.miradioyopal.chat_radio.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userText: TextView = view.findViewById(R.id.userText)
        val messageText: TextView = view.findViewById(R.id.messageText)
        val time: TextView = view.findViewById(R.id.timeMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = messages[position]
        holder.userText.text = chat.user
        holder.messageText.text = chat.message

        val d = formatearTimestamp(chat.timestamp)
        holder.time.text = d
    }

    override fun getItemCount() = messages.size

    fun formatearTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }
}
