package com.example.uploadimagetest.data

data class PointFeatures(
    val features: List<PointFeature>,
    val type: String
)

data class PointFeature(
    val geometry: PointGeometry,
    val properties: PointProperties,
    val type: String
)

data class PointGeometry(
    val coordinates: List<Double>,
    val type: String
)

data class PointProperties(
    val ATTR: String,
    val COLOR: String,
    val TYPE: String
)