package com.example.cvmaker.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cvmaker.R
import com.example.cvmaker.databinding.ItemLanguageBinding
import com.example.cvmaker.model.Language

class LanguageAdapter(
    private val items: List<Language>,
    private val onLanguageSelected: (Language) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    inner class LanguageViewHolder(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        val binding = holder.binding

        binding.flag.setImageResource(item.flagResId)
        binding.name.text = item.languageName
        binding.subtitle.text = item.subtitle

        if (item.isSelected) {
            binding.cardL.setCardBackgroundColor(ContextCompat.getColor(context, R.color.blue))
            binding.radio.setBackgroundResource(R.drawable.selected_tick)

            binding.name.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            binding.subtitle.setTextColor(ContextCompat.getColor(context, android.R.color.white))

        } else {
            binding.cardL.setCardBackgroundColor( ContextCompat.getColor(context, R.color.white))
            binding.radio.setBackgroundResource(R.drawable.radio_button_unchecked)

            binding.name.setTextColor(ContextCompat.getColor(context, R.color.black))
            binding.subtitle.setTextColor(ContextCompat.getColor(context, R.color.light_black))

        }

        holder.itemView.setOnClickListener {
            items.forEach { it.isSelected = false }
            item.isSelected = true
            notifyDataSetChanged()
            onLanguageSelected(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
