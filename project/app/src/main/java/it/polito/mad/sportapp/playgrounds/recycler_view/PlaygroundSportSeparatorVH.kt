package it.polito.mad.sportapp.playgrounds.recycler_view

import android.view.View
import android.widget.TextView
import it.polito.mad.sportapp.R

class PlaygroundSportSeparatorVH(view: View) : AbstractPlaygroundVH(view) {
    private val sportEmojiView = view.findViewById<TextView>(R.id.playground_separator_sport_emoji)
    private val sportNameView = view.findViewById<TextView>(R.id.playground_separator_sport_name)

    fun bindSport(sportEmoji: String, sportName: String) {
        sportEmojiView.text = sportEmoji
        sportNameView.text = sportName
    }
}