package com.madhan.cameraruntimepermission

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_layout.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private var fileUri: Uri? = null //This is the URI of the file where the image is stored
    private lateinit var tvImagePath: TextView
    private lateinit var sbMessage: Snackbar

    companion object { //To hold the constant values
        private const val REQUEST_CAMERA_AND_STORAGE_PERMISSION = 123
        private const val REQUEST_CODE_OPEN_CAMERA = 124
        private const val REQUEST_CODE_APP_SETTINGS = 125
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        tvImagePath = findViewById(R.id.tv_image_path)
        ivImage.setOnClickListener({
            takePhoto()
        })
    }

    /**
     * Check for necessary conditions and open camera to take photo
     */
    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showMessage()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA_AND_STORAGE_PERMISSION)
            }
        } else {
            openCamera()
        }
    }

    /**
     * Show the message regarding the necessity of required permissions to the user
     */
    private fun showMessage() {
        sbMessage = Snackbar.make(coordinator, "Need Camera permission to take photo and storage permission to store the photo", Snackbar.LENGTH_INDEFINITE).setAction("Settings") {
            val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            settingsIntent.data = uri
            startActivityForResult(settingsIntent, REQUEST_CODE_APP_SETTINGS)
        }
        sbMessage.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA_AND_STORAGE_PERMISSION -> {
                if (grantResults.containsOnly(PackageManager.PERMISSION_GRANTED)) {
                    openCamera()
                } else {
                    Toast.makeText(this@MainActivity, "Permission for camera and storage is denied", Toast.LENGTH_SHORT).show()
                    showMessage()
                }
            }
        }
    }

    /**
     * Method to open the camera
     */
    private fun openCamera() {
        val photo: File = Utils.getTempFilePath(this)
        fileUri = if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photo)
        } else {
            Uri.fromFile(photo)
        }
        val selectCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        selectCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        selectCamera.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        startActivityForResult(selectCamera, REQUEST_CODE_OPEN_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_OPEN_CAMERA -> {
                    contentResolver.notifyChange(fileUri, null)
                    val resolver: ContentResolver = contentResolver
                    val bitmap: Bitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
                    Glide.with(this@MainActivity).load(bitmap).thumbnail(1f).into(ivImage)
                    tvImagePath.text = fileUri!!.toString()
                }

                REQUEST_CODE_APP_SETTINGS -> {
                    takePhoto()
                }
            }
        }
    }
}

//Extension function(containsOnly) for IntArray class in order to filter the required integer value only
fun IntArray.containsOnly(num: Int): Boolean = filter { it == num }.isNotEmpty()