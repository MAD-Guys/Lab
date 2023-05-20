package it.polito.mad.sportapp.playgrounds

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.playgrounds.recycler_view.PlaygroundsAdapter
import it.polito.mad.sportapp.playgrounds.PlaygroundsViewModel.PlaygroundOrderKey

@AndroidEntryPoint
class PlaygroundsBySportFragment : Fragment(R.layout.playgrounds_view)
{
    private val viewModel by activityViewModels<PlaygroundsViewModel>()

    private lateinit var progressBar: View

    // playgrounds overlay bar
    private lateinit var playgroundsOverlayBar: View
    private lateinit var overlayBarSportButton: View
    private lateinit var overlayBarCenterButton: View

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)

        // retrieve bottom overlay bar to navigate between the two list views
        playgroundsOverlayBar = requireActivity().findViewById(R.id.overlay_bottom_bar)
        overlayBarSportButton = playgroundsOverlayBar.findViewById(R.id.overlay_bar_sport_button)
        overlayBarCenterButton = playgroundsOverlayBar.findViewById(R.id.overlay_bar_center_button)

        // hide default playgrounds views
        view.findViewById<View>(R.id.default_playgrounds_container).visibility = View.GONE

        // setup playgrounds recycler view
        val playgroundsRecyclerView = view.findViewById<RecyclerView>(R.id.playgrounds_rv)
        playgroundsRecyclerView.visibility = View.VISIBLE

        val playgroundsAdapter = PlaygroundsAdapter(
            PlaygroundOrderKey.SPORT,
            viewModel.getPlaygroundsOrderedBySport()
        ) { playgroundId ->
            // lambda function to navigate to the tapped playground's details view
            val params = bundleOf(
                "id_playground" to playgroundId
            )
            findNavController().navigate(R.id.action_playgroundsBySportFragment_to_PlaygroundDetailsFragment, params)
        }

        playgroundsRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), RecyclerView.VERTICAL, false
            )
            adapter = playgroundsAdapter
        }

        // playgrounds observer
        viewModel.playgrounds.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                // show progress bar and hide playgrounds
                progressBar.visibility = View.VISIBLE
                playgroundsRecyclerView.visibility = View.GONE
            }

            // when playgrounds change, set recycler view new data
            playgroundsAdapter.orderedAndSeparatedPlaygrounds =
                viewModel.separateAndOrderPlaygroundsBy(PlaygroundOrderKey.SPORT, it)

            // update recycler view
            playgroundsAdapter.notifyDataSetChanged()

            if (it.isNotEmpty()) {
                // hide progress bar and show playgrounds
                progressBar.visibility = View.GONE
                playgroundsRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // show playgrounds overlay bar
        playgroundsOverlayBar.visibility = View.VISIBLE

        // set the selected color to *sport* button views
        val sportIcon = overlayBarSportButton.findViewById<ImageView>(R.id.overlay_bar_sport_icon)
        val sportLabel = overlayBarSportButton.findViewById<TextView>(R.id.overlay_bar_sport_label)

        sportIcon.setColorFilter(
            ContextCompat.getColor(requireContext(),
                R.color.overlay_bar_button_selected_color), android.graphics.PorterDuff.Mode.SRC_IN)
        sportLabel.setTextColor(resources.getColor(R.color.overlay_bar_button_selected_color, null))

        // set the unselected color to *center* button views
        val centerIcon = overlayBarCenterButton.findViewById<ImageView>(R.id.overlay_bar_center_icon)
        val centerLabel = overlayBarCenterButton.findViewById<TextView>(R.id.overlay_bar_center_label)

        centerIcon.setColorFilter(
            ContextCompat.getColor(requireContext(),
                R.color.overlay_bar_button_unselected_color), android.graphics.PorterDuff.Mode.SRC_IN)
        centerLabel.setTextColor(resources.getColor(R.color.overlay_bar_button_unselected_color, null))

        // set proper listener
        overlayBarCenterButton.setOnClickListener {
            findNavController().navigate(R.id.action_playgroundsBySportFragment_to_playgroundsByCenterFragment)
        }

        // set bottom bar as visible
        requireActivity().findViewById<View>(R.id.bottom_navigation_bar).visibility = View.VISIBLE
    }


    override fun onPause() {
        super.onPause()
        // hide playgrounds overlay bar
        playgroundsOverlayBar.visibility = View.GONE

        // clear listener
        overlayBarCenterButton.setOnClickListener(null)
    }
}