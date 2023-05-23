package it.polito.mad.sportapp.invitation.users_recycler_view

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.User

class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val profilePicture = view.findViewById<ImageView>(R.id.profile_picture)
    private val username = view.findViewById<TextView>(R.id.username)
    private val button = view.findViewById<Button>(R.id.button_invite)

    fun bind(user: User){

        //TODO profilePicture.setImageResource( ? )
        profilePicture.clipToOutline = true
        username.text = "@${user.username}"
        button.setOnClickListener {
            //TODO sendInvitation(user.id)
        }
    }
}