package it.polito.mad.sportapp.invitation.users_recycler_view

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.User

class UserViewHolder(private val view: View, val sportId: String) : RecyclerView.ViewHolder(view) {
    private val profilePicture = view.findViewById<ImageView>(R.id.profile_picture)
    private val fullName = view.findViewById<TextView>(R.id.full_name)
    private val username = view.findViewById<TextView>(R.id.username)
    private val button = view.findViewById<Button>(R.id.button_invite)
    private val sportLevelIcon = view.findViewById<ImageView>(R.id.user_to_invite_sport_level_icon)

    @SuppressLint("SetTextI18n")
    fun bind(user: User, buttonListener: (String, String) -> Unit){

        if (user.imageURL != null) {
            // set profile picture with url
            Picasso.get()
                .load(user.imageURL)
                .into(profilePicture)
        }
        else {
            // set notification icon with default url
            Picasso.get()
                .load(R.drawable.user_profile_picture_noalpha)
                .into(profilePicture)
        }

        profilePicture.clipToOutline = true
        fullName.text = "${user.firstName} ${user.lastName}"
        username.text = "@${user.username}"

        button.setOnClickListener {
            buttonListener(user.id!!, user.username)
        }

        // set user sport level icon
        val userSportLevel = user.sportLevels.find { sportLevel -> sportLevel.sportId == sportId }

        if(userSportLevel == null) {
            sportLevelIcon.visibility = ImageView.INVISIBLE
        }
        else {
            val level = userSportLevel.level!!

            sportLevelIcon.setImageDrawable(AppCompatResources.getDrawable(view.context, when(level) {
                "BEGINNER" -> R.drawable.beginner_level_badge
                "INTERMEDIATE" -> R.drawable.intermediate_level_badge
                "EXPERT" -> R.drawable.expert_level_badge
                "PRO" -> R.drawable.pro_level_badge
                else -> throw Exception("Unrecognized sport level icon in UserViewHolder.bind()")
            }))

            sportLevelIcon.visibility = ImageView.VISIBLE
        }
    }
}