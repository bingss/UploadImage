package com.example.uploadimagetest.fragments.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uploadimagetest.R
import com.example.uploadimagetest.data.PointData
import com.example.uploadimagetest.listener.PointClickListener

class PointAdapter(private val points : List<PointData>,
                   private val listener: PointClickListener?
) :RecyclerView.Adapter<PointViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        return PointViewHolder.from(parent,listener)
    }

    override fun getItemCount(): Int {
        return points.size
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        val item = points[position]
        holder.bind(item,position)
    }

}

class PointViewHolder private constructor(
    private val itemView: View,
    private val listener: PointClickListener?
) : RecyclerView.ViewHolder(itemView){

    private val pointNameTextView = itemView.findViewById<TextView>(R.id.point_name)
    private val stateImgView = itemView.findViewById<ImageView>(R.id.state_imageView)
    private val deleteButton = itemView.findViewById<ImageButton>(R.id.delete_imageButton)

    fun bind(item: PointData,position: Int){
        pointNameTextView.text = item.PointNumber
        //圖片加載
        if (item.ImgPath.size > 0){
            Glide.with(itemView.context).load(item.ImgPath[0])
                .error(R.drawable.baseline_null_24)
                .into(stateImgView)
        }else{
            stateImgView.setImageResource(R.drawable.baseline_null_24)
        }


        if(listener != null){
            itemView.setOnClickListener {
                listener.onRowClick(item.PointNumber,position)
            }
            deleteButton.setOnClickListener {
                listener.onDeleteButtonClick(item.PointNumber,position)
            }
        }


    }

    companion object{
        fun from(parent: ViewGroup,listener: PointClickListener?): PointViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater
                .inflate(R.layout.point_card, parent, false)
            return PointViewHolder(view,listener)

        }
    }

}