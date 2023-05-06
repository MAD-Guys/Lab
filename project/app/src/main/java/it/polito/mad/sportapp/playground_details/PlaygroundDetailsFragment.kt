package it.polito.mad.sportapp.playground_details

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.polito.mad.sportapp.R

class PlaygroundDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = PlaygroundDetailsFragment()
    }

    private lateinit var viewModel: PlaygroundDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playground_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaygroundDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}