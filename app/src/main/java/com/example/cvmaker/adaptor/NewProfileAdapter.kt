package com.example.cvmaker.adaptor

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cvmaker.R
import com.example.cvmaker.databinding.ProfileItemBinding
import com.example.cvmaker.model.profilemodels.ProfileModelUI
import java.text.SimpleDateFormat
import java.util.*

class NewProfileAdapter : RecyclerView.Adapter<NewProfileAdapter.ViewHolder>() {

    private val dataList: MutableList<ProfileModelUI> = mutableListOf()
    var listener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("ProfileAdapter", "onCreateViewHolder called")
        val binding = ProfileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("ProfileAdapter", "onBindViewHolder called for position: $position, dataList size: ${dataList.size}")
        if (position < dataList.size) {
            holder.bind(dataList[position])
        } else {
            Log.e("ProfileAdapter", "Invalid position: $position, dataList size: ${dataList.size}")
        }
    }

    override fun getItemCount(): Int {
        Log.d("ProfileAdapter", "getItemCount called, returning: ${dataList.size}")
        return dataList.size
    }

    inner class ViewHolder(private val binding: ProfileItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private var lastClickTime = 0L
        private val clickDelay = 1000L // milliseconds

        fun bind(item: ProfileModelUI) {
            Log.d("ProfileAdapter", "ViewHolder bind called for item: ${item.profileModelItem.first_name}")

            if (absoluteAdapterPosition == RecyclerView.NO_POSITION) {
                Log.w("ProfileAdapter", "absoluteAdapterPosition is NO_POSITION, returning early")
                return
            }

            // Debounced popup menu click
            binding.popupMenu.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                Log.d("ProfileAdapter", "PopupMenu clicked at position: $absoluteAdapterPosition, currentTime: $currentTime, lastClickTime: $lastClickTime")
                if (currentTime - lastClickTime >= clickDelay) {
                    lastClickTime = currentTime
                    Log.d("ProfileAdapter", "PopupMenu action triggered for position: $absoluteAdapterPosition")
                    listener?.onPopMenu(absoluteAdapterPosition, dataList[absoluteAdapterPosition])
                }
            }

            // Debounced item click
            binding.itsACons.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                Log.d("ProfileAdapter", "Item clicked at position: $absoluteAdapterPosition, currentTime: $currentTime, lastClickTime: $lastClickTime")
                if (currentTime - lastClickTime >= clickDelay) {
                    lastClickTime = currentTime
                    Log.d("ProfileAdapter", "Item action triggered for position: $absoluteAdapterPosition")
                    listener?.onItemClick(absoluteAdapterPosition, dataList[absoluteAdapterPosition])
                }
            }

            // Bind UI
            Log.d("ProfileAdapter", "Binding profile: ${item.profileModelItem.first_name} ${item.profileModelItem.last_name}, created_at: ${item.profileModelItem.created_at}")
            binding.profileName.text = "${item.profileModelItem.first_name} ${item.profileModelItem.last_name}"
            binding.profileDate.text = convertTimestampToDate(item.profileModelItem.created_at)

            Glide.with(binding.root.context)
                .load(item.profileModelItem.image)
                .placeholder(R.drawable.cv_place_holder)
                .error(R.drawable.cv_place_holder)
                .into(binding.profilePhoto)
        }
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    interface OnClickListener : View.OnClickListener {
        fun onPopMenu(position: Int, item: ProfileModelUI)
        fun onItemClick(position: Int, item: ProfileModelUI)
    }

    fun setData(newList: MutableList<ProfileModelUI>) {
        Log.d("ProfileAdapter", "setData called with ${newList.size} items")
        Log.d("ProfileAdapter", "Current dataList size before clear: ${dataList.size}")

        dataList.clear()
        dataList.addAll(newList)

        Log.d("ProfileAdapter", "DataList updated. New size: ${dataList.size}")
        Log.d("ProfileAdapter", "Calling notifyDataSetChanged()")

        notifyDataSetChanged()

        Log.d("ProfileAdapter", "notifyDataSetChanged() completed")
    }

    private fun convertTimestampToDate(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            dateFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }
}