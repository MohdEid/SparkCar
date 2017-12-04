package com.example.notebookpc.sparkcar

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.notebookpc.sparkcar.adapters.FavoritesAdapter
import kotlinx.android.synthetic.main.fragment_favorite.*

// TODO add listener for clicking on items

class FavoriteCleanersFragment : Fragment() {


    private var mListener: OnFragmentInteractionListener? = null

    private lateinit var favoritesList: List<Id>

    //TODO fix problem with fragment doesn't load with empty favorites
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesList = CustomerHolder.customer.favoriteCleaners

        val adapter = FavoritesAdapter(favoritesList)
        favoriteCleanersRecyclerView.layoutManager = LinearLayoutManager(activity)
        favoriteCleanersRecyclerView.adapter = adapter
        favoriteCleanersRecyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


    interface OnFragmentInteractionListener {

        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        fun newInstance(): FavoriteCleanersFragment {
            return FavoriteCleanersFragment()
        }
    }
}// Required empty public constructor
