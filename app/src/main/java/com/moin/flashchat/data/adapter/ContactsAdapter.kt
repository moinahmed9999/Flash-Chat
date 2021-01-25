package com.moin.flashchat.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moin.flashchat.data.model.BasicUser
import com.moin.flashchat.databinding.LayoutContactBinding

class ContactsAdapter :
    ListAdapter<BasicUser, ContactsAdapter.ContactViewHolder>(BasicUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder =
        ContactViewHolder(
            LayoutContactBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) =
        holder.bind(getItem(position))

    class ContactViewHolder(private val binding: LayoutContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: BasicUser) {
            binding.tvContactName.text = user.displayName
            binding.tvContactPhoneNumber.text = user.phoneNumber
        }
    }
}

class BasicUserDiffCallback : DiffUtil.ItemCallback<BasicUser>() {
    override fun areItemsTheSame(oldItem: BasicUser, newItem: BasicUser): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: BasicUser, newItem: BasicUser): Boolean =
        oldItem == newItem
}