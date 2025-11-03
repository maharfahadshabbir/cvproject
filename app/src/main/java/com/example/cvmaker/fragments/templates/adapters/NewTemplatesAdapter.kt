//package com.example.cvmaker.fragments.templates.adapters
//
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.aicvmaker.models.ThumbnailResponseItem
//import com.example.aicvmaker.retrofit.RetrofitClient
//import com.example.cvmaker.R
//import com.example.cvmaker.databinding.ItemForTemplateBinding
//import com.example.cvmaker.databinding.ItemForTemporaryTemplateBinding
//import com.example.cvmaker.model.ThumbnailResponseItem
//
//class NewTemplatesAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    private var templateList = mutableListOf<ThumbnailResponseItem>()
//    var clickListener:ListenerInterface?=null
//    var selectedLayout = 0
//
//
////    var Listener_obj: Listener_Interface? = null
//
//    inner class TemplateViewHolder(val firstBinding: ItemForTemplateBinding) : RecyclerView.ViewHolder(firstBinding.root) {}
//    inner class TemporaryViewHolder(val secondBinding: ItemForTemporaryTemplateBinding) : RecyclerView.ViewHolder(secondBinding.root) {}
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//
//        val layoutInflater = LayoutInflater.from(parent.context)
//        val viewHolder = when (selectedLayout) {
//            0 -> {
//                TemplateViewHolder(ItemForTemplateBinding.inflate(layoutInflater, parent, false))
//            }
//
//            1 -> {
//                TemporaryViewHolder(ItemForTemporaryTemplateBinding.inflate(layoutInflater, parent, false))
//            }
//            else ->
//                TemporaryViewHolder(ItemForTemporaryTemplateBinding.inflate(layoutInflater, parent, false))
//        }
//        return viewHolder
//    }
//
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val item = templateList[position]
//        when (selectedLayout) {
//            0 -> {
//                val templateViewHolder = holder as TemplateViewHolder
//                item.thumbnail.let {
//
//                    templateViewHolder.firstBinding.templatePosition.text = (position+1).toString()
//
//                    Log.d("TAG_thumbnails_onbind", "onViewCreated: ${RetrofitClient.BASE_URL_Media+ it }}")
//
//                    Glide.with(holder.itemView.context)
//                        .load(RetrofitClient.BASE_URL_Media+it) // image url
//                        .placeholder(R.drawable.cv_place_holder) // any placeholder to load at start
//                        .error(R.drawable.cv_place_holder)  // any image in case of error
//                        .into(templateViewHolder.firstBinding.templateImageView)
//
//
//                    // templateViewHolder.firstBinding.templateImageView.setImageResource(Re)
//
//
//                }
//                templateViewHolder.itemView.setOnClickListener {
//                    clickListener?.updateVisibility(position,item)
//                    clickListener?.updateImage(item,position)
//
//
//                }
//            }
//            1 -> {
//                Log.i("my_testing", "onBindViewHolder: ${position}")
//                val temporaryViewHolder = holder as TemporaryViewHolder
//                item.thumbnail?.let {
//                    Log.d("TAG_thumbnails_onbind", "onViewCreated: ${RetrofitClient.BASE_URL+ it }}")
//
//
//                    Glide.with(holder.itemView.context)
//                        .load(RetrofitClient.BASE_URL_Media+it) // image url
//                        .placeholder(R.drawable.cv_place_holder) // any placeholder to load at start
//                        .error(R.drawable.cv_place_holder)  // any image in case of error
//                        .into(temporaryViewHolder.secondBinding.templateImageView)
//
//                  //  temporaryViewHolder.secondBinding.templateImageView.setImageResource(it)
//
//                }
//
//                temporaryViewHolder.itemView.setOnClickListener {
//                    clickListener?.updateVisibility(position, item)
//                    clickListener?.updateImage(item,position)
//                }
//            }
//        }
//
//
//
//        }
//    override fun getItemCount(): Int {
//        return templateList.size
//    }
//
//    fun setData(templateList: MutableList<ThumbnailResponseItem>) {
//        this.templateList.clear()
//        this.templateList.addAll(templateList)
//        notifyDataSetChanged()
//        Log.i("my_testing", "setData: ${templateList.size}")
//    }
//
//
//    fun updateUi(templateList: MutableList<ThumbnailResponseItem>) {
//        this.templateList.clear()
//        this.templateList.addAll(templateList)
//        notifyDataSetChanged()
//
//    }
//    interface ListenerInterface {
//        fun updateVisibility(position: Int, item: ThumbnailResponseItem)
//        fun updateImage(templateRv:ThumbnailResponseItem,position: Int)
//    }
//}
