package com.example.uploadimagetest.listener

import com.example.uploadimagetest.fragments.adapter.PointAdapter

interface PointClickListener {
    fun onRowClick(pointNum:String,position: Int){}

    fun onDeleteButtonClick(pointNum:String,position: Int){}

    fun onDeleteImgClick(imgPaths:MutableList<String>,position: Int)
}