package com.example.uploadimagetest.util

import android.graphics.Color
import com.example.uploadimagetest.R
import com.example.uploadimagetest.data.PointData
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import org.json.JSONObject

class MapManager(val googleMap: GoogleMap) {


    private val _layers  = mutableListOf<GeoJsonLayer>()
    val layers : List<GeoJsonLayer>
        get() = _layers


    fun initMap(polyJson: JSONObject,pointJson: JSONObject,pointList:List<PointData>) {
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.isBuildingsEnabled = false

        addLayer(polyJson,pointJson)
        setPolyStyle()
        setPointStyle(pointList)

        val latLng = _layers[1].features.first().geometry.geometryObject as LatLng
        moveZoom(latLng,17F)
    }


    fun updateMapPoint(currentPointNum:String,hasData:Boolean){
        if(_layers.size == 2){
            val feature = _layers[1].features.firstOrNull { it.getProperty("ATTR") == currentPointNum }
            if(feature != null){
                val pointStyle = GeoJsonPointStyle()
                if(hasData){
                    pointStyle.icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                }
                else{
                    pointStyle.icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                }
                pointStyle.title = feature.getProperty("ATTR")
                feature.setPointStyle(pointStyle)
            }
        }
    }

    private fun addLayer(polyJson: JSONObject,pointJson: JSONObject){
        _layers.add( GeoJsonLayer(googleMap, polyJson) )
        _layers[_layers.lastIndex].addLayerToMap()
        _layers.add( GeoJsonLayer(googleMap, pointJson) )
        _layers[_layers.lastIndex].addLayerToMap()
    }

    private fun setPolyStyle(){
        val polygonStyle = _layers[0].defaultPolygonStyle
        polygonStyle.strokeWidth = 0.3F
        val specificStyle = GeoJsonPolygonStyle()
        specificStyle.fillColor = R.color.fill_yellow
        _layers[0].features.forEach {
            if (it.getProperty("COLOR") == "true") {
                it.setPolygonStyle(specificStyle)
            }
        }

    }

    private fun setPointStyle(pointList:List<PointData>){
        _layers[1].features.forEach {
            val pointData = pointList.firstOrNull { point -> point.PointNumber == it.getProperty("ATTR") }
            val pointStyle = GeoJsonPointStyle()
            if(pointData != null){
                if(pointData.ImgPath.size > 0){
                    pointStyle.icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                }
                else{
                    pointStyle.icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                }
                pointStyle.title = it.getProperty("ATTR")
                it.setPointStyle(pointStyle)
            }
        }

    }

    private fun moveZoom(latLng: LatLng,zoom:Float){
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(zoom))
    }

    fun removePoint(pointNum: String) {
        if(_layers.size == 2){
            val delPoint = layers[1].features.firstOrNull { it.getProperty("ATTR") == pointNum }
            if(delPoint != null){
                layers[1].removeFeature(delPoint)
            }
        }
    }



}