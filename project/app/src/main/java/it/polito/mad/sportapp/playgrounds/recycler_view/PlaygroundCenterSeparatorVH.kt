package it.polito.mad.sportapp.playgrounds.recycler_view

import android.view.View
import android.widget.TextView
import it.polito.mad.sportapp.R

class PlaygroundCenterSeparatorVH(view: View) : AbstractPlaygroundVH(view) {
    private val sportCenterNameView = view.findViewById<TextView>(R.id.playground_separator_sport_center_name)

    fun bindSportCenter(sportCenterName: String) {
        sportCenterNameView.text = sportCenterName
    }
}