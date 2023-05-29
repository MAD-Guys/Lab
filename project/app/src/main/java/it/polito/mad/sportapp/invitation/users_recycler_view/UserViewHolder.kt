package it.polito.mad.sportapp.invitation.users_recycler_view

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.application_utilities.CircleTransform
import it.polito.mad.sportapp.entities.User
import it.polito.mad.sportapp.entities.room.RoomUser

class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val profilePicture = view.findViewById<ImageView>(R.id.profile_picture)
    private val fullName = view.findViewById<TextView>(R.id.full_name)
    private val username = view.findViewById<TextView>(R.id.username)
    private val button = view.findViewById<Button>(R.id.button_invite)

    @SuppressLint("SetTextI18n")
    fun bind(user: User, buttonListener: (String, String) -> Unit){

        if (user.imageURL != null) {
            // set profile picture with url
            Picasso.get()
                .load(user.imageURL)
                .into(profilePicture)
        }
        profilePicture.clipToOutline = true
        fullName.text = "${user.firstName} ${user.lastName}"
        username.text = "@${user.username}"
        button.setOnClickListener {
            buttonListener(user.id!!, user.username)
        }
    }
}