package com.example.uploadimagetest.fragments.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uploadimagetest.R
import com.example.uploadimagetest.listener.PointClickListener

class ImgAdapter(private val imgPaths : MutableList<String>,
                 private val listener: PointClickListener?
) : RecyclerView.Adapter<ImgViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImgViewHolder {
        return ImgViewHolder.from(parent,listener)
    }

    override fun getItemCount(): Int {
        return imgPaths.size
    }

    override fun onBindViewHolder(holder: ImgViewHolder, position: Int) {
        holder.bind(imgPaths,position)
    }

}

class ImgViewHolder private constructor(
    private val itemView: View,
    private val listener: PointClickListener?
) : RecyclerView.ViewHolder(itemView){

    private val imgView = itemView.findViewById<ImageView>(R.id.img_item)
    private val delImgButton = itemView.findViewById<ImageView>(R.id.del_imgButton)

    fun bind(imgPaths: MutableList<String>,position: Int){
        //圖片加載
        Glide.with(itemView.context).load(imgPaths[position])
            .error(R.drawable.baseline_null_24)
            .into(imgView)

        if (listener != null){
            delImgButton.setOnClickListener {
                listener.onDeleteImgClick(imgPaths,position)
            }
        }
    }

    companion object{
        fun from(parent: ViewGroup,listener: PointClickListener?): ImgViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater
                .inflate(R.layout.img_card, parent, false)
            return ImgViewHolder(view,listener)

        }
    }

}