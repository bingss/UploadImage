package com.example.uploadimagetest.data

data class PolyFeatures(
    val features: List<PolyFeature>,
    val type: String
)

data class PolyFeature(
    val geometry: PolyGeometry,
    val properties: PolyProperties,
    val type: String
)

data class PolyGeometry(
    val coordinates: List<List<List<Double>>>,
    val type: String
)

data class PolyProperties(
    val ATTR: String,
    val COLOR: String,
    val TYPE: String
)