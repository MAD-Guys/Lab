package it.polito.mad.sportapp.playgrounds.recycler_view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.playgrounds.PlaygroundsViewModel

class PlaygroundsAdapter(
    private val orderKey: PlaygroundsViewModel.PlaygroundOrderKey,
    var orderedAndSeparatedPlaygrounds: List<PlaygroundInfo?>,   // contains 'null' before each key group
    private val navigateToPlayground: (Int) -> Unit
) : RecyclerView.Adapter<AbstractPlaygroundVH>()
{
    override fun getItemCount() = orderedAndSeparatedPlaygrounds.size

    override fun getItemViewType(position: Int): Int {
        val listItemData = orderedAndSeparatedPlaygrounds[position]

        return if(listItemData != null) {
            R.layout.playground_card_small
        }
        else when(orderKey) {
            PlaygroundsViewModel.PlaygroundOrderKey.SPORT -> R.layout.playground_card_sport_separator
            PlaygroundsViewModel.PlaygroundOrderKey.CENTER -> R.layout.playground_card_center_separator
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractPlaygroundVH {
        val listItemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return when(viewType) {
            R.layout.playground_card_small -> PlaygroundVH(listItemView, navigateToPlayground)
            R.layout.playground_card_sport_separator -> PlaygroundSportSeparatorVH(listItemView)
            R.layout.playground_card_center_separator -> PlaygroundCenterSeparatorVH(listItemView)
            else -> throw Exception("Unexpected item layout id in onCreateViewHolder(), inside playgrounds Recycler View")
        }
    }

    override fun onBindViewHolder(holder: AbstractPlaygroundVH, position: Int) {
        when(holder) {
            is PlaygroundVH -> {
                // retrieve the corresponding playground info
                val playgroundInfo = orderedAndSeparatedPlaygrounds[position]!!

                // update list item view with that playground's info
                holder.bindPlayground(playgroundInfo)
            }
            is PlaygroundSportSeparatorVH -> {
                // retrieve info regarding next playgrounds
                val nextPlayground = orderedAndSeparatedPlaygrounds[position+1]!!

                // set proper sport name and emoji
                holder.bindSport(nextPlayground.sportEmoji, nextPlayground.sportName)
            }
            is PlaygroundCenterSeparatorVH -> {
                // retrieve info regarding next playgrounds
                val nextPlayground = orderedAndSeparatedPlaygrounds[position+1]!!

                // set proper sport center name
                holder.bindSportCenter(nextPlayground.sportCenterName)
            }
            else -> throw Exception("Unexpected View Holder in onBindViewHolder(), inside Playgrounds Recycler View")
        }
    }
}
