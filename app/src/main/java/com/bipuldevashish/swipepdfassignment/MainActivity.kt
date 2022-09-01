package com.bipuldevashish.swipepdfassignment

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_CODE = 101
        private const val TAG = "MainActivity"
    }

    // declaring width and height
    // for our PDF file.
    private var pageHeight = 1120
    private var pageWidth = 792


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkPermissions()) {
            lifecycleScope.launch {
                generatePDF()
            }
        } else {
            requestPermission()
        }

    }

    // on below line we are calling
    // on request permission result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // on below line we are checking if the
        // request code is equal to permission code.
        if (requestCode == PERMISSION_CODE) {

            // on below line we are checking if result size is > 0
            if (grantResults.isNotEmpty()) {

                // on below line we are checking
                // if both the permissions are granted.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]
                    == PackageManager.PERMISSION_GRANTED
                ) {

                    // if permissions are granted we are displaying a toast message.
                    Toast.makeText(this@MainActivity, "Permission Granted..", Toast.LENGTH_SHORT).show()


                    lifecycleScope.launch {
                        generatePDF()
                    }

                } else {

                    // if permissions are not granted we are
                    // displaying a toast message as permission denied.
                    Toast.makeText(this@MainActivity, "Permission Denied..", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        // on below line we are creating a variable for both of our permissions.

        // on below line we are creating a variable for
        // writing to external storage permission
        val writeStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            WRITE_EXTERNAL_STORAGE
        )

        // on below line we are creating a variable
        // for reading external storage permission
        val readStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            READ_EXTERNAL_STORAGE
        )

        // on below line we are returning true if both the
        // permissions are granted anf returning false
        // if permissions are not granted.
        return writeStoragePermission == PackageManager.PERMISSION_GRANTED
                && readStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    // on below line we are creating a function to request permission.
    private fun requestPermission() {

        // on below line we are requesting read and write to
        // storage permission for our application.
        ActivityCompat.requestPermissions(
            this,
            arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), PERMISSION_CODE
        )
    }

    private suspend fun generatePDF() {

        withContext(Dispatchers.Default) {

            val pdfDocument = PdfDocument()
            val paint = Paint()

            val myPageInfo: PdfDocument.PageInfo? =
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

            val myPage: PdfDocument.Page = pdfDocument.startPage(myPageInfo)

            val canvas: Canvas = myPage.canvas
            paint.style = Paint.Style.STROKE
            paint.color = ContextCompat.getColor(this@MainActivity, R.color.black)
            canvas.drawRect(20F, 20F, 772F, 500F, paint)


            val mTextPaint = TextPaint()
            val mTextLayout = StaticLayout(
                getString(R.string.english_description),
                mTextPaint,
                canvas.width - 75,
                Layout.Alignment.ALIGN_NORMAL,
                1.1f,
                5.0f,
                true
            )

            val textX = 40F
            val textY = 40F

            canvas.translate(textX, textY)
            mTextLayout.draw(canvas)

            canvas.drawLine(-20F, 140F, 732F, 140F, paint)

            val mTextLayoutHindi = StaticLayout(
                getString(R.string.hindi_desc),
                mTextPaint,
                canvas.width - 75,
                Layout.Alignment.ALIGN_NORMAL,
                1.1f,
                5.0f,
                true
            )

            val txtX = 0F
            val txtY = 160F
            canvas.translate(txtX, txtY)
            mTextLayoutHindi.draw(canvas)
            canvas.save()

            pdfDocument.finishPage(myPage)

            // below line is used to set the name of
            // our PDF file and its path.
            val path = Environment.getExternalStorageDirectory().path + "/Download/test.pdf"
            val file = File(path)


            try {

                pdfDocument.writeTo(FileOutputStream(file))
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "PDF file generated..", Toast.LENGTH_SHORT)
                        .show()
                }

            } catch (e: Exception) {
                // below line is used
                // to handle error
                e.printStackTrace()

                Log.d(TAG, "generatePDF caused due to: $e")

                // on below line we are displaying a toast message as fail to generate PDF
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Fail to generate PDF file..",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            }

            pdfDocument.close()
            openPDF()
        }


    }

    private fun openPDF() {

        // Get the File location and file name.
        val path = Environment.getExternalStorageDirectory().path + "/Download/test.pdf"
        val file = File(path)

        // Get the URI Path of file.
        val uriPdfPath: Uri =
            FileProvider.getUriForFile(this@MainActivity, applicationContext.packageName + ".provider", file)


        // Start Intent to View PDF from the Installed Applications.
        val pdfOpenIntent = Intent(Intent.ACTION_VIEW)
        pdfOpenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        pdfOpenIntent.clipData = ClipData.newRawUri("", uriPdfPath)
        pdfOpenIntent.setDataAndType(uriPdfPath, "application/pdf")
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            startActivity(pdfOpenIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(this@MainActivity, "There is no app to load corresponding PDF", Toast.LENGTH_LONG)
                .show()
        }
    }
}