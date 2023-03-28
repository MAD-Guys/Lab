package it.polito.mad.lab2

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


class EditProfileActivity : AppCompatActivity() {

    private var fullName = "John Doe"
    private var nickname = "@johndoe"
    private var sex = "Male"
    private var age: Int = 23
    private var bio =
        "I’m a Computer Engineering student from Latina. I love playing basketball and tennis with my friends, especially on the weekend."

    private var galleryUri: Uri? = null
    private var cameraUri: Uri? = null

    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data: Intent? = it.data
                galleryUri = data?.data
                val toast: Toast = Toast.makeText(this, "Image URI: $galleryUri", Toast.LENGTH_LONG)
                toast.show()
            }
        }

    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val toast: Toast = Toast.makeText(this, "Image URI: $cameraUri", Toast.LENGTH_LONG)
                toast.show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        //This code tries to retrieve photos from the gallery
        val galleryButton: Button = findViewById(R.id.button_message)
        galleryButton.setOnClickListener() {
            val galleryIntent: Intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryActivityResultLauncher.launch(galleryIntent)
        }

        //This code tries to retrieve photos from the camera
        val cameraButton: Button = findViewById(R.id.button_add_friend)
        cameraButton.setOnClickListener() {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf<String>(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permission, 112)
                onRequestPermissionsResult(112, permission, intArrayOf(0, 0))
            } else {
                openCamera()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        }

    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")

        cameraUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)

        cameraActivityResultLauncher.launch(cameraIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.confirm_button) {
            this.finish()
            val toast: Toast = Toast.makeText(this, "Now you are in SHOW mode!", Toast.LENGTH_SHORT)
            toast.show()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("fullName", fullName)
        outState.putString("nickName", nickname)
        outState.putString("sex", sex)
        outState.putInt("age", age)
        outState.putString("bio", bio)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        fullName = savedInstanceState.getString("fullName").toString()
        nickname = savedInstanceState.getString("nickName").toString()
        sex = savedInstanceState.getString("sex").toString()
        age = savedInstanceState.getInt("age")
        bio = savedInstanceState.getString("bio").toString()
    }

}