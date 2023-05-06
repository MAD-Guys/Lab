package it.polito.mad.sportapp.playground_details.reviews_recycler_view

import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val username = view.findViewById<TextView>(R.id.username)
    private val date = view.findViewById<TextView>(R.id.date)
    private val qualityRatingBar = view.findViewById<RatingBar>(R.id.qualityRatingBar)
    private val facilitiesRatingBar = view.findViewById<RatingBar>(R.id.facilitiesRatingBar)
    private val reviewBody = view.findViewById<TextView>(R.id.reviewBody)

    fun bind(review : Any /*TODO: Review class*/){
        username.text = "themadreviewer"//review.username
        date.text = "6/5/23" //review.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        qualityRatingBar.rating = 3f //review.qualityRating
        facilitiesRatingBar.rating = 4f //review.facilitiesRating
        reviewBody.text = "An intresting review"//review.text

    }
}