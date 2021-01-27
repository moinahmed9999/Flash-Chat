package com.moin.flashchat.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.moin.flashchat.data.model.Message
import com.moin.flashchat.databinding.LayoutMessageLeftBinding
import com.moin.flashchat.databinding.LayoutMessageRightBinding

class MessageAdapter(
        options: FirestoreRecyclerOptions<Message>
) : FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {

    companion object {
        private const val TYPE_SENT = 0
        private const val TYPE_RECEIVED = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            SentMessageViewHolder(LayoutMessageRightBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        } else {
            ReceivedMessageViewHolder(LayoutMessageLeftBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Message) {
        if (getItemViewType(position) == TYPE_SENT) {
            (holder as SentMessageViewHolder).bind(model)
        } else {
            (holder as ReceivedMessageViewHolder).bind(model)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message: Message = getItem(position)
        val uid = Firebase.auth.currentUser?.uid!!
        return if (message.senderId == uid) TYPE_SENT else TYPE_RECEIVED
    }

    class ReceivedMessageViewHolder(private val binding: LayoutMessageLeftBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.apply {
                tvMsgSender.text = message.senderName
                tvMsg.text = message.message
            }
        }
    }

    class SentMessageViewHolder(private val binding: LayoutMessageRightBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.apply {
                tvMsgSender.text = "You"
                tvMsg.text = message.message
            }
        }
    }
}