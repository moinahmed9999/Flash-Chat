package com.moin.flashchat.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.moin.flashchat.data.model.ChatPreview
import com.moin.flashchat.databinding.LayoutChatPreviewBinding

class ChatPreviewAdapter(
    options: FirestoreRecyclerOptions<ChatPreview>,
    private val clickListener: ChatClickListener
) : FirestoreRecyclerAdapter<ChatPreview, ChatPreviewAdapter.ChatViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder =
        ChatViewHolder(LayoutChatPreviewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: ChatPreview) =
        holder.bind(model, clickListener)

    class ChatViewHolder(private val binding: LayoutChatPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatPreview: ChatPreview, clickListener: ChatClickListener) {
            binding.apply {
                tvChatTitle.text = chatPreview.chatTitle
                tvChatLastMessage.text = chatPreview.lastMessage
                tvChatLastMsgTime.visibility = View.GONE

                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onChatClick(position, chatPreview)
                    }
                }
            }
        }
    }

    interface ChatClickListener {
        fun onChatClick(position: Int, chatPreview: ChatPreview)
    }
}