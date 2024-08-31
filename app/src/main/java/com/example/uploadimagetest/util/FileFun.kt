package com.example.uploadimagetest.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.uploadimagetest.BuildConfig
import com.example.uploadimagetest.data.CameraPhoto
import com.example.uploadimagetest.data.PointData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects
import java.util.TimeZone


suspend fun zipPDF(context: Context, pointList: List<PointData>,pdfName :String):File{
    val savePath = "${context.filesDir.path}/img"
    if( !File(savePath).exists() ) {
        File(savePath).mkdirs()
    }

    val pdfFile = cratePDF(savePath,pointList,pdfName,context)
    val zip = zipFile(savePath,pdfFile)

    return zip
}

suspend fun deleteFile(path:String){
    withContext(Dispatchers.IO) {
        //刪除上一次上傳檔案
        val delFile = File(path)
        if (delFile.isDirectory) {
            for (file in delFile.listFiles()!!) file.delete()
        }
    }
}

suspend fun cratePDF (
    savePath: String,
    pointList: List<PointData>,
    pdfName: String,
    context: Context):File {

    return withContext(Dispatchers.IO) {
        val pdfFile = File(savePath,"${pdfName}.pdf")
        val writer = PdfWriter( FileOutputStream(pdfFile) )
        val pdfDocument = PdfDocument(writer)
        pdfDocument.defaultPageSize = com.itextpdf.kernel.geom.PageSize.A4
        val pdf = Document(pdfDocument)

        val pdfFont = PdfFontFactory.createFont("STSong-Light","UniGB-UCS2-H")

        pdf.add( Paragraph("案號：${pdfName}")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(pdfFont)
                    .setFontSize(16F) )

        val columnWidth = floatArrayOf(1f, 9f)
        val table = Table(columnWidth)
        table.setWidth(UnitValue.createPercentValue(100f))

        var cell = Cell().add( Paragraph("點號")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(pdfFont) )

        table.addCell(cell)

        cell = Cell().add(Paragraph("照片")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(pdfFont) )

        table.addCell(cell)

        pointList.filter { it.ImgPath.size > 0 }
            .forEach {
                it.ImgPath.forEachIndexed { idx, path ->

                    val originalImg = File( path )
                    val compressImg =Compressor.compress(context,originalImg){
                        default(width = 640,height = 480, format = Bitmap.CompressFormat.JPEG)
                        quality(75)
                    }

                    cell = Cell().add( Paragraph(it.PointNumber).setFont(pdfFont) )
                            .setVerticalAlignment(VerticalAlignment.MIDDLE)
                            .setTextAlignment(TextAlignment.CENTER)
                    table.addCell( cell )

                    val pdfImageData = ImageDataFactory.create(compressImg.path)
                    val pdfImage = Image(pdfImageData)
                                    .setHeight(350f)
                                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    table.addCell( pdfImage )
                }
            }
        pdf.add(table)
        pdf.close()
        pdfDocument.close()
        writer.close()
        pdfFile


    }
}

suspend fun zipFile(savePath:String,pdfFile: File):File{
    return withContext(Dispatchers.IO) {
        val zipFile = File(savePath,"LP_1.zip")
        val zos = java.util.zip.ZipOutputStream( FileOutputStream(zipFile).buffered() )
        val pdfEntry = java.util.zip.ZipEntry(pdfFile.name)
        zos.putNextEntry(pdfEntry)

        val fis = FileInputStream(pdfFile).buffered()
        val buffer = ByteArray(102400)
        var bytesRead = fis.read(buffer)
        while (bytesRead != -1) {
            zos.write(buffer, 0, bytesRead)
            bytesRead = fis.read(buffer)
        }
        fis.close()
        zos.closeEntry()
        zos.close()

        zipFile
    }
}


fun getPhotoFileUri(context: Context) : CameraPhoto {
    val savePath = "${context.filesDir.path}/img"
    if( !File(savePath).exists() ) {
        File(savePath).mkdirs()
    }
    val storageFile = File(savePath)
    val photoFile = File.createTempFile("images", ".jpg", storageFile)

    val fileProviderUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", photoFile)
    return CameraPhoto(fileProviderUri,photoFile.path)
}

