package com.example.cvmaker.adaptor

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cvmaker.R
import com.example.cvmaker.databinding.ProfileItemBinding
import com.example.cvmaker.model.workingmodels.CvModelRequestDb
import com.example.cvmaker.model.workingmodels.CvProfileItem

class NewProfileAdapter : RecyclerView.Adapter<NewProfileAdapter.ViewHolder>() {

    private val dataList: MutableList<CvProfileItem> = mutableListOf()
    var listener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("NewProfileAdapter", "onCreateViewHolder called")
        val binding =
            ProfileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(
            "NewProfileAdapter",
            "onBindViewHolder position: $position, size: ${dataList.size}"
        )
        if (position < dataList.size) {
            holder.bind(dataList[position])
        } else {
            Log.e("NewProfileAdapter", "Invalid position: $position for size: ${dataList.size}")
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class ViewHolder(private val binding: ProfileItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var lastClickTime = 0L
        private val clickDelay = 1000L // ms

        fun bind(item: CvProfileItem) {
            if (absoluteAdapterPosition == RecyclerView.NO_POSITION) return

            // Popup menu (3 dots)
            binding.popupMenu.setOnClickListener {
                val now = System.currentTimeMillis()
                if (now - lastClickTime >= clickDelay) {
                    lastClickTime = now
                    listener?.onPopMenu(absoluteAdapterPosition, dataList[absoluteAdapterPosition])
                }
            }

            // Whole card click
            binding.itsACons.setOnClickListener {
                val now = System.currentTimeMillis()
                if (now - lastClickTime >= clickDelay) {
                    lastClickTime = now
                    listener?.onItemClick(absoluteAdapterPosition, dataList[absoluteAdapterPosition])
                }
            }

            bindUi(item)
        }

        private fun bindUi(item: CvProfileItem) {
            val cv: CvModelRequestDb = item.data

            val name = cv.personalDetails?.name ?: "Untitled Profile"
            binding.profileName.text = name.trim()
            binding.profileDate.text = item.updatedAt ?: ""

            val imageUriString = cv.personalDetails?.imageUri

            if (!imageUriString.isNullOrBlank()) {
                val uri = imageUriString.toUri()
                Glide.with(binding.root.context)
                    .load(uri)
                    .placeholder(R.drawable.cv_place_holder)
                    .error(R.drawable.cv_place_holder)
                    .into(binding.profilePhoto)
            } else {
                Glide.with(binding.root.context)
                    .load(R.drawable.cv_place_holder)
                    .into(binding.profilePhoto)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newList: List<CvProfileItem>) {
        Log.d("NewProfileAdapter", "setData size: ${newList.size}")
        dataList.clear()
        dataList.addAll(newList)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): CvProfileItem? =
        if (position in dataList.indices) dataList[position] else null

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.listener = onClickListener
    }

    interface OnClickListener : View.OnClickListener {
        fun onPopMenu(position: Int, item: CvProfileItem)
        fun onItemClick(position: Int, item: CvProfileItem)
    }
}
