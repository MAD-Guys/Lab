package it.polito.mad.lab2

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.*

class EditProfileActivity : AppCompatActivity() {

    private var firstNameTemp: String? = null
    private var lastNameTemp: String? = null
    private var usernameTemp: String? = null
    private var ageTemp: String? = null
    private var radioGenderTemp: Int = R.id.radioMale
    private var locationTemp: String? = null
    private var bioTemp: String? = null

    private var inputImage: Bitmap? = null

    //User info EditTexts
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var username: EditText
    private lateinit var age: EditText
    private lateinit var location: EditText
    private lateinit var bio: EditText

    //Radio group variables
    private lateinit var radioGroup: RadioGroup

    //Profile picture view and launchers
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
                inputImage = galleryUri?.let { it1 -> uriToBitmap(it1) }
                //Glide.with(this).load(inputImage).override(displayWidth, 300).centerCrop().into(profilePicture)

                //Setting picture into the imageView
                profilePicture.setImageBitmap(inputImage)
            }
        }

    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                inputImage = cameraUri?.let { it1 -> uriToBitmap(it1) }
                inputImage = inputImage?.let { it1 -> rotateBitmap(it1) }

                //Setting picture into the imageView
                profilePicture.setImageBitmap(inputImage)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        //Initializing the EditText views
        firstName = findViewById(R.id.edit_first_name)
        lastName = findViewById(R.id.edit_last_name)
        username = findViewById(R.id.edit_nickname)
        radioGroup = findViewById(R.id.radioSexGroup)
        age = findViewById(R.id.edit_age)
        location = findViewById(R.id.edit_location)
        bio = findViewById(R.id.edit_bio)
        profilePicture = findViewById(R.id.profile_picture)

        val profileImageButton: ImageButton = findViewById(R.id.profile_image_button)
        registerForContextMenu(profileImageButton)

        profileImageButton.setOnClickListener() {
            //open the related context menu
            openContextMenu(profileImageButton)
        }

        //Loading data from sharedPreferences file
        loadDataFromSharedPreferences()

        //Adding listeners to the temporary variables
        firstName.addTextChangedListener(textListenerInit("firstName"))
        lastName.addTextChangedListener(textListenerInit("lastName"))
        username.addTextChangedListener(textListenerInit("username"))
        age.addTextChangedListener(textListenerInit("age"))
        location.addTextChangedListener(textListenerInit("location"))
        bio.addTextChangedListener(textListenerInit("bio"))

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            radioGenderTemp = checkedId
        }

    }

    private fun loadDataFromSharedPreferences() {

        // retrieving data from SharedPreferences
        val sh = getSharedPreferences("it.polito.mad.lab2", Context.MODE_PRIVATE)

        val firstNameResume = sh.getString("firstName", getString(R.string.first_name))
        val lastNameResume = sh.getString("lastName", getString(R.string.last_name))
        val usernameResume = sh.getString("username", getString(R.string.username))
        val radioCheckedResume = sh.getInt("radioChecked", R.id.radioMale)
        val ageResume = sh.getString("age", getString(R.string.user_age))
        val locationResume = sh.getString("location", getString(R.string.user_location))
        val bioResume = sh.getString("bio", getString(R.string.user_bio))

        val profilePictureResume = sh.getString("profilePicture", null)

        //Setting EditText views
        firstName.setText(firstNameResume)
        lastName.setText(lastNameResume)
        username.setText(usernameResume)
        radioGroup.check(radioCheckedResume)
        age.setText(ageResume)
        location.setText(locationResume)
        bio.setText(bioResume)

        //Setting temporary variables
        firstNameTemp = firstNameResume
        lastNameTemp = lastNameResume
        usernameTemp = usernameResume
        radioGenderTemp = radioCheckedResume
        ageTemp = ageResume
        locationTemp = locationResume
        bioTemp = bioResume

        if (profilePictureResume != null && !profilePictureResume.equals("", ignoreCase = true)) {
            val b: ByteArray = Base64.decode(profilePictureResume, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
            profilePicture.setImageBitmap(bitmap)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //Saving temporary variables into the bundle in order to have the right values after the activity's restore
        outState.putString("firstNameTemp", firstNameTemp)
        outState.putString("lastNameTemp", lastNameTemp)
        outState.putString("usernameTemp", usernameTemp)
        outState.putString("ageTemp", ageTemp)
        outState.putInt("radioGenderChecked", radioGenderTemp)
        outState.putString("locationTemp", locationTemp)
        outState.putString("bioTemp", bioTemp)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        //Restoring temporary variables from the bundle
        firstNameTemp = savedInstanceState.getString("firstNameTemp").toString()
        firstName.setText(firstNameTemp)

        lastNameTemp = savedInstanceState.getString("lastNameTemp").toString()
        lastName.setText(lastNameTemp)

        usernameTemp = savedInstanceState.getString("usernameTemp").toString()
        username.setText(usernameTemp)

        ageTemp = savedInstanceState.getString("ageTemp").toString()
        age.setText(ageTemp)

        radioGenderTemp = savedInstanceState.getInt("radioGenderChecked")
        radioGroup.check(radioGenderTemp)

        locationTemp = savedInstanceState.getString("locationTemp").toString()
        location.setText(locationTemp)

        bioTemp = savedInstanceState.getString("bioTemp").toString()
        bio.setText(bioTemp)
    }

    private fun textListenerInit(fieldName: String): TextWatcher {

        //Implementing and returning the TextWatcher interface
        return object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                when (fieldName) {
                    "firstName" -> firstNameTemp = firstName.text.toString()
                    "lastName" -> lastNameTemp = lastName.text.toString()
                    "username" -> usernameTemp = username.text.toString()
                    "age" -> ageTemp = age.text.toString()
                    "location" -> locationTemp = location.text.toString()
                    "bio" -> bioTemp = bio.text.toString()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
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
        val imageFile = File.createTempFile("temp_profile_picture", ".jpeg", cacheDir)

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

    private fun savePictureOnInternalStorage(picture: Bitmap) {
        val cw = ContextWrapper(applicationContext)

        val directory: File = cw.getDir("imageDir", Context.MODE_PRIVATE)

        val file = File(directory, "profile_picture" + ".jpg")

        if (!file.exists()) {
            val fos: FileOutputStream?

            try {
                fos = FileOutputStream(file)
                picture.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
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

    //This function rotates the image if the image captured on samsung devices
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
        //if the user clicks on the confirm button, the information changed is saved
        R.id.confirm_button -> {

            //Saving values into the sharedPreferences file
            val sh = getSharedPreferences("it.polito.mad.lab2", Context.MODE_PRIVATE)
            val editor = sh.edit()

            editor.putString("firstName", firstNameTemp)
            editor.putString("lastName", lastNameTemp)
            editor.putString("username", usernameTemp)
            editor.putString("age", ageTemp)
            editor.putInt("radioChecked", radioGenderTemp)
            editor.putString("location", locationTemp)
            editor.putString("bio", bioTemp)

            //Managing the Gender field in order to display it correctly into the ShowProfileActivity
            when (radioGenderTemp) {
                R.id.radioFemale -> editor.putString("gender", "Female")
                R.id.radioOther -> editor.putString("gender", "Other")
                else -> editor.putString("gender", "Male")
            }

            //Saving the pi
            if (inputImage != null) {
                savePictureOnSharedPreferences(inputImage!!)
            }

            editor.apply()

            this.finish()
            true
        }

        //detect when the user clicks on the "back" button
        //if the user clicks the back button the information changed is not saved
        R.id.back_button -> {
            this.finish()
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