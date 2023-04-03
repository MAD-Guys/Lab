package it.polito.mad.lab2
import android.content.ContentResolver
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.WindowMetrics
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException

/** Returns display width and display height */
internal fun AppCompatActivity.getDisplayMeasures(): Pair<Int,Int> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics: WindowMetrics = windowManager.currentWindowMetrics
        val displayHeight = metrics.bounds.height()
        val displayWidth = metrics.bounds.width()
        Pair(displayWidth, displayHeight)
    } else {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val displayHeight = metrics.heightPixels
        val displayWidth = metrics.widthPixels
        Pair(displayWidth, displayHeight)
    }
}

/**
 * Change profile picture size:
 * - set the height to 1/3 of the view (*excluding* the menu) in portrait view
 * - set the width to 1/3 of the view in landscape view
 * */
 internal fun AppCompatActivity.setProfilePictureSize(
     menuHeight: Int, profilePictureContainer: ConstraintLayout,
     backgroundProfilePicture: ImageView, profilePicture: ImageView) {
    // retrieve display sizes
    val (displayWidth, displayHeight) = this.getDisplayMeasures()

    // if orientation is vertical, set the picture box height to 1/3 of the display (excluding the menu)
    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        profilePictureContainer.layoutParams.height = (displayHeight-menuHeight)/3
    }
    // if orientation is horizontal, set the picture box width to 1/3 the display
    else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        profilePictureContainer.layoutParams.width = displayWidth/3
    }

    // render new dimensions on the screen
    profilePictureContainer.requestLayout()
    backgroundProfilePicture.requestLayout()
    profilePicture.requestLayout()
}

/* manipulate image bitmaps */

/** Transform image into a bitmap */
internal fun uriToBitmap(selectedFileUri: Uri, contentResolver: ContentResolver): Bitmap? {
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

/** Rotate image if image captured from samsung devices
 * (Most phone cameras are landscape, meaning if you take the photo in portrait,
 * the resulting photos will be rotated 90 degrees)
 * */
internal fun rotateBitmap(imageUri: Uri?, bitmap: Bitmap, contentResolver: ContentResolver): Bitmap? {
    val input = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    val exif = ExifInterface(imageUri?.let { contentResolver.openInputStream(it) }!!)
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

/* saving picture on internal storage */

internal fun savePictureOnInternalStorage(picture: Bitmap, directory: File, filename: String) {
    val file = File(directory, filename)
    val outputStream = FileOutputStream(file)
    picture.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.flush()
    outputStream.close()
}

/* getting picture from internal storage */

internal fun getPictureFromInternalStorage(directory: File, filename: String): Bitmap? {
    val file = File(directory, filename)
    return if (file.exists()) {
        BitmapFactory.decodeFile(file.absolutePath)
    } else null
}
