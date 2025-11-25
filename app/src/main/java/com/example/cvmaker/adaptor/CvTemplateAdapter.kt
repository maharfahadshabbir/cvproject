package com.example.cvmaker.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.cvmaker.R
import com.example.cvmaker.cv.CvTemplateOption

class CvTemplateAdapter(
    private val templates: List<CvTemplateOption>,
    private val onSelect: (CvTemplateOption) -> Unit
) : RecyclerView.Adapter<CvTemplateAdapter.TemplateViewHolder>() {

    inner class TemplateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imgTemplate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_template, parent, false)
        return TemplateViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val template = templates[position]
        holder.imageView.setImageResource(template.imageRes)
        holder.imageView.alpha = if (template.isSelected) 1f else 0.5f

        holder.imageView.setOnClickListener {
            templates.forEach { it.isSelected = false } // deselect all
            template.isSelected = true
            notifyDataSetChanged()
            onSelect(template)
        }
    }

    override fun getItemCount(): Int = templates.size
}
