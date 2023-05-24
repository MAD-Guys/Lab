package it.polito.mad.sportapp.playgrounds.recycler_view

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.room.RoomPlaygroundInfo
import java.time.format.DateTimeFormatter
import java.util.Locale

class PlaygroundVH(
    val view: View,
    val navigateToPlayground: (Int) -> Unit
) : AbstractPlaygroundVH(view)
{
    private lateinit var playground: RoomPlaygroundInfo

    // sub views
    private val playgroundImage = view.findViewById<ImageView>(R.id.playground_image)
    private val playgroundRatingView = view.findViewById<RatingBar>(R.id.playground_rating)
    private val playgroundNameView = view.findViewById<TextView>(R.id.playground_name)
    private val sportCenterNameView = view.findViewById<TextView>(R.id.sport_center_name)
    private val sportNameView = view.findViewById<TextView>(R.id.playground_sport_name)
    private val sportEmojiView = view.findViewById<TextView>(R.id.playground_sport_emoji)
    private val addressView = view.findViewById<TextView>(R.id.playground_address)
    private val openingTimeView = view.findViewById<TextView>(R.id.playground_opening_hour)
    private val closingTimeView = view.findViewById<TextView>(R.id.playground_closing_hour)
    private val costPerHourView = view.findViewById<TextView>(R.id.playground_cost_per_hour)

    init {
        view.setOnClickListener {
            navigateToPlayground(playground.playgroundId)
        }
    }

    fun bindPlayground(playground: RoomPlaygroundInfo) {
        // save playground reference
        this.playground = playground

        // * show playground data on the view *

        // change sport image
        val sportImage = this.getSportImage(playground.sportId)
        playgroundImage.setImageDrawable(sportImage)

        // change playground rating
        playgroundRatingView.rating = playground.overallRating

        // change playground name
        playgroundNameView.text = playground.playgroundName

        // change sport center name
        sportCenterNameView.text = playground.sportCenterName

        // change sport name and emoji
        sportEmojiView.text = playground.sportEmoji
        sportNameView.text = playground.sportName

        // change address
        addressView.text = playground.sportCenterAddress

        // change opening and closing time
        openingTimeView.text = playground.openingHours.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH))
        closingTimeView.text = playground.closingHours.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH))

        // change cost per hour
        costPerHourView.text = String.format("%.2f", playground.pricePerHour)
    }

    private fun getSportImage(sportId: Int): Drawable {
        return when(sportId) {
            1 -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._01_tennis, null)!!
            2 -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._02_table_tennis, null)!!
            3 -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._03_padel, null)!!
            4 -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._04_basket, null)!!
            5 -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._05_football11, null)!!
            6 -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._06_volleyball, null)!!
            7 -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._07_beach_volley, null)!!
            8 -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._08_football5, null)!!
            9 -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._09_football8, null)!!
            10 -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._10_minigolf, null)!!
            else -> ResourcesCompat.getDrawable(view.context.resources, R.drawable.unknown_playground, null)!!
        }
    }
}