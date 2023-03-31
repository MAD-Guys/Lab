package it.polito.mad.lab2

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {

    //General info variables
    val metrics = DisplayMetrics()
    val displayHeight = metrics.heightPixels
    val displayWidth = metrics.widthPixels
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var nickname: EditText
    private lateinit var age: EditText
    private lateinit var location: EditText
    private lateinit var bio: EditText

    //Radio group variables
    private lateinit var radioGroup: RadioGroup

    //Profile picture variable
    private lateinit var profilePicture: ImageView

    private var galleryUri: Uri? = null
    private var cameraUri: Uri? = null

    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                val data: Intent? = it.data
                galleryUri = data?.data
                val inputImage: Bitmap? = galleryUri?.let { it1 -> uriToBitmap(it1) }
                Glide.with(this).load(inputImage).override(displayWidth, 300).centerCrop().into(profilePicture)

                //Setting picture into the imageView
                //profilePicture.setImageBitmap(inputImage)

                //Saving picture into shared preferences
                if (inputImage != null) {
                    savePictureOnSharedPreferences(inputImage)
                }
            }
        }

    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val inputImage: Bitmap? = cameraUri?.let { it1 -> uriToBitmap(it1) }
                val rotated: Bitmap? = inputImage?.let { it1 -> rotateBitmap(it1) }

                //Setting picture into the imageView
                profilePicture.setImageBitmap(rotated)

                //Saving picture into shared preferences
                if (rotated != null) {
                    savePictureOnSharedPreferences(rotated)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        firstName = findViewById(R.id.edit_first_name)
        lastName = findViewById(R.id.edit_last_name)
        nickname = findViewById(R.id.edit_nickname)
        radioGroup = findViewById(R.id.radioSexGroup)
        age = findViewById(R.id.edit_age)
        location = findViewById(R.id.edit_location)
        bio = findViewById(R.id.edit_bio)
        profilePicture = findViewById(R.id.profile_picture)

        firstName.addTextChangedListener(textListenersInit("firstName", firstName))
        lastName.addTextChangedListener(textListenersInit("lastName", lastName))
        nickname.addTextChangedListener(textListenersInit("nickname", nickname))
        age.addTextChangedListener(textListenersInit("age", age))
        location.addTextChangedListener(textListenersInit("location", location))
        bio.addTextChangedListener(textListenersInit("bio", bio))

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val sh = getSharedPreferences("it.polito.mad.lab2", Context.MODE_PRIVATE)
            val editor = sh.edit()
            editor.putInt("radioChecked", checkedId)

            //Managing the Sex field in order to display it correctly into the ShowActivityProfile activity
            when (checkedId) {
                R.id.radioFemale -> editor.putString("sex", "Female")
                R.id.radioOther -> editor.putString("sex", "Other")
                else -> editor.putString("sex", "Male")
            }

            editor.apply()
        }

        val profileImageButton: ImageButton = findViewById(R.id.profile_image_button)
        registerForContextMenu(profileImageButton)

        profileImageButton.setOnClickListener() {
            //open the related context menu
            openContextMenu(profileImageButton)
        }

    }

    override fun onResume() {
        super.onResume()

        // retrieve data from SharedPreferences
        val sh = getSharedPreferences("it.polito.mad.lab2", Context.MODE_PRIVATE)

        val firstNameResume = sh.getString("firstName", getString(R.string.first_name))
        val lastNameResume = sh.getString("lastName", getString(R.string.last_name))
        val nicknameResume = sh.getString("nickname", getString(R.string.nickname))
        val radioCheckedResume = sh.getInt("radioChecked", R.id.radioMale)
        val ageResume = sh.getString("age", getString(R.string.age))
        val locationResume = sh.getString("location", getString(R.string.location))
        val bioResume = sh.getString("bio", getString(R.string.bio))

        val profilePictureResume = sh.getString("profilePicture", null)

        firstName.setText(firstNameResume)
        lastName.setText(lastNameResume)
        nickname.setText(nicknameResume)
        radioGroup.check(radioCheckedResume)
        age.setText(ageResume)
        location.setText(locationResume)
        bio.setText(bioResume)

        if (profilePictureResume != null && !profilePictureResume.equals("", ignoreCase = true)) {
            val b: ByteArray = Base64.decode(profilePictureResume, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
            profilePicture.setImageBitmap(bitmap)
        }

    }

    private fun textListenersInit(id: String, et: EditText): TextWatcher {

        return object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val sh = getSharedPreferences("it.polito.mad.lab2", Context.MODE_PRIVATE)
                val editor = sh.edit()
                editor.putString(id, et.text.toString())
                editor.apply()
            }

            override fun afterTextChanged(s: Editable?) {
                val sh = getSharedPreferences("it.polito.mad.lab2", Context.MODE_PRIVATE)
                val editor = sh.edit()
                editor.putString(id, et.text.toString())
                editor.apply()
            }
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

        //Uncomment these lines if you want to save images inside the phone gallery
        /*val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        cameraUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)*/

        //Creating a file object for the temporal image
        val imageFile = File.createTempFile("temp_image", ".jpeg", cacheDir)

        //Creating through a FileProvider the URI
        cameraUri = FileProvider.getUriForFile(
            this,
            "it.polito.mad.lab2.fileprovider", imageFile
        )

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

    private fun savePictureOnSharedPreferences(picture: Bitmap) {
        val sh = getSharedPreferences("it.polito.mad.lab2", Context.MODE_PRIVATE)
        val editor = sh.edit()

        //Encoding bitmap into Base64 string
        val baos = ByteArrayOutputStream()
        picture.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        val encodedImage: String = Base64.encodeToString(b, Base64.DEFAULT)

        //Saving Base64 string into shared preferences
        editor.putString("profilePicture", encodedImage)
        editor.apply()
    }

    //rotate image if image captured on samsung devices
    //Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
    private fun rotateBitmap(bitmap: Bitmap): Bitmap? {
        val input = MediaStore.Images.Media.getBitmap(contentResolver, cameraUri)
        val exif = ExifInterface(cameraUri?.let { contentResolver.openInputStream(it) }!!)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        val rotationMatrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotationMatrix.setRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotationMatrix.setRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotationMatrix.setRotate(270f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
    }

    //The function below is used to inflate the menu
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
            val toast: Toast = Toast.makeText(this, "Save button clicked!!!", Toast.LENGTH_SHORT)
            toast.show()
            true
        }
        //detect when the user clicks on the "back" button
        R.id.back_button -> {
            this.finish()
            val toast: Toast = Toast.makeText(this, "Back button clicked!!!", Toast.LENGTH_SHORT)
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

}