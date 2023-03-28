package it.polito.mad.lab2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.FileDescriptor
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {

    private var firstName = "John"
    private var lastName = "Doe"
    private var nickname = "@johndoe"
    private var sex = "Male"
    private var age: Int = 23
    private var bio =
        "I’m a Computer Engineering student from Latina. I love playing basketball and tennis with my friends, especially on the weekend."

    private lateinit var imageView: ImageView
    private var galleryUri: Uri? = null
    private var cameraUri: Uri? = null

    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data: Intent? = it.data
                galleryUri = data?.data
                imageView.setImageURI(galleryUri)
            }
        }

    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val inputImage: Bitmap? = cameraUri?.let { it1 -> uriToBitmap(it1) }
                val rotated: Bitmap? = inputImage?.let { it1 -> rotateBitmap(it1) }
                imageView.setImageBitmap(rotated)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        imageView = findViewById(R.id.profile_picture)

        val profileImageButton: ImageButton = findViewById(R.id.profile_image_button)
        registerForContextMenu(profileImageButton)

        profileImageButton.setOnClickListener() {

            //open the related context menu
            openContextMenu(profileImageButton)
        }

    }

    //Function that handles the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 112 && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else if (requestCode == 113 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryActivityResultLauncher.launch(galleryIntent)
        }

    }

    //Function that opens the camera
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")

        cameraUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)

        cameraActivityResultLauncher.launch(cameraIntent)
    }

    //This function takes URI of the image and returns a bitmap
    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    //rotate image if image captured on samsung devices
    //Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
    @SuppressLint("Range")
    fun rotateBitmap(input: Bitmap): Bitmap? {
        val orientationColumn = arrayOf(MediaStore.Images.Media.ORIENTATION)

        val cur: Cursor? =
            cameraUri?.let { contentResolver.query(it, orientationColumn, null, null, null) }
        var orientation: Float = -1f

        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0])).toFloat()
        }

        val rotationMatrix = Matrix()
        rotationMatrix.setRotate(orientation)

        cur?.close()

        return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
    }

    //The two functions below are used to inflate the menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_profile_menu, menu)

        supportActionBar?.title = "Edit Profile"
        return true
    }

    //The three functions below are used to inflate and manage the context menu
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater

        when (v.id) {
            R.id.profile_image_button -> {
                inflater.inflate(R.menu.profile_picture_context_menu, menu)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {

        //detect when the user clicks on the "confirm" button
        R.id.confirm_button -> {
            this.finish()
            val toast: Toast = Toast.makeText(this, "Now you are in SHOW mode!", Toast.LENGTH_SHORT)
            toast.show()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    //NOTE: the request code is used to identify the request in the callback function but it is completely random
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.camera -> {

                //Check if the permissions are granted
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                ) {
                    val permission = arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    requestPermissions(permission, 112)
                    onRequestPermissionsResult(112, permission, intArrayOf(0, 0))
                } else {
                    openCamera()
                }

                true
            }
            R.id.gallery -> {

                //Check if the permissions are granted
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                ) {
                    val permission = arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    requestPermissions(permission, 113)
                    onRequestPermissionsResult(113, permission, intArrayOf(0, 0))
                } else {
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryActivityResultLauncher.launch(galleryIntent)
                }

                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    //The two functions below are used to save and restore the state of the activity
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("fullName", firstName)
        outState.putString("lastName", lastName)
        outState.putString("nickName", nickname)
        outState.putString("sex", sex)
        outState.putInt("age", age)
        outState.putString("bio", bio)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        firstName = savedInstanceState.getString("fullName").toString()
        lastName = savedInstanceState.getString("lastName").toString()
        nickname = savedInstanceState.getString("nickName").toString()
        sex = savedInstanceState.getString("sex").toString()
        age = savedInstanceState.getInt("age")
        bio = savedInstanceState.getString("bio").toString()
    }

}