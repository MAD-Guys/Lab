package it.polito.mad.sportapp.invitation.users_recycler_view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.room.RoomUser

class UserAdapter(private val buttonListener: (Int) -> Unit) : RecyclerView.Adapter<UserViewHolder>() {

    val users = mutableListOf<RoomUser>()
    var reservationId : Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_row, parent, false)

        return UserViewHolder(v)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position], buttonListener)
    }
}