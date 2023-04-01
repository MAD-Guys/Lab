package it.polito.mad.lab2
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowMetrics
import androidx.appcompat.app.AppCompatActivity


/*** Returns display width and display height */
fun AppCompatActivity.getDisplayMeasures(): Pair<Int,Int> {
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
