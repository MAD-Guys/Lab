package it.polito.mad.sportapp.playgrounds.recycler_view

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.PlaygroundInfo
import java.time.format.DateTimeFormatter
import java.util.Locale

class PlaygroundVH(
    val view: View,
    val navigateToPlayground: (String) -> Unit
) : AbstractPlaygroundVH(view)
{
    private lateinit var playground: PlaygroundInfo

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

    fun bindPlayground(playground: PlaygroundInfo) {
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

    private fun getSportImage(sportId: String): Drawable {
        return when(sportId) {
            "x7f9jrM9BTiMoIFoyVFq" -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._01_tennis, null)!!
            "RQgUy37JaJcE8uRmLanb" -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._02_table_tennis, null)!!
            "fpkrSYDrMUDdqZ4kPfOc" -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._03_padel, null)!!
            "ZoasHiiaJ3CoNWMEr3RF" -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._04_basket, null)!!
            "te2BgJjzIJbC9qTgLrT4" -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._05_football11, null)!!
            "dU8Nvc3SfXfYaQKYzRbr" -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._06_volleyball, null)!!
            "plGE1kMDKhqE17Azvdw8" -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._07_beach_volley, null)!!
            "7AIqD0iwHOW6FIycvlwo" -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._08_football5, null)!!
            "qrwiJsMOa3eCiq6fwOW2" -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._09_football8, null)!!
            "4nFO9rfxo6iIJVTluCcn" -> ResourcesCompat.getDrawable(view.context.resources, R.drawable._10_minigolf, null)!!
            else -> ResourcesCompat.getDrawable(view.context.resources, R.drawable.unknown_playground, null)!!
        }
    }
}