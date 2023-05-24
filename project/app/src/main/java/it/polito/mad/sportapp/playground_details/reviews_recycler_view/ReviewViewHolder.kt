package it.polito.mad.sportapp.playground_details.reviews_recycler_view

import android.view.View
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.room.RoomReview
import java.time.format.DateTimeFormatter

class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val title = view.findViewById<TextView>(R.id.reviewTitle)
    private val username = view.findViewById<TextView>(R.id.username)
    private val date = view.findViewById<TextView>(R.id.date)
    private val qualityRatingBar = view.findViewById<RatingBar>(R.id.qualityRatingBar)
    private val facilitiesRatingBar = view.findViewById<RatingBar>(R.id.facilitiesRatingBar)
    private val reviewBody = view.findViewById<TextView>(R.id.reviewBody)
    private val lastUpdate = view.findViewById<TextView>(R.id.lastUpdate)
    private val lastUpdateContainer = view.findViewById<LinearLayout>(R.id.lastUpdate_container)

    fun bind(review : RoomReview){
        title.text = review.title
        username.text = review.username
        date.text = review.publicationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        qualityRatingBar.rating = review.qualityRating
        facilitiesRatingBar.rating = review.facilitiesRating
        reviewBody.text = review.review
        lastUpdate.text = review.lastUpdateDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        if(review.lastUpdateDate.isAfter(review.publicationDate)){
            lastUpdateContainer.visibility = LinearLayout.VISIBLE
        }
    }
}