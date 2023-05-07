package it.polito.mad.sportapp.playground_details.reviews_recycler_view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R

class ReviewAdapter : RecyclerView.Adapter<ReviewViewHolder>() {

    val reviews = mutableListOf<Any/*TODO: Review class*/>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_item, parent, false)

        return ReviewViewHolder(v)
    }

    override fun getItemCount(): Int = reviews.size

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }
}