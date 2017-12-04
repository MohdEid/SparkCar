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
import com.example.notebookpc.sparkcar.adapters.FavoriteLocationsAdapter
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_location.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.startActivity


class LocationFragment : Fragment() {


    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addLocationBtn.onClick {
            FirebaseDatabase.getInstance().getReference("/locations")
            startActivity<PickLocationActivity>()
        }
        val favoritesList = CustomerHolder.customer?.favoriteLocations ?: throw AssertionError()
        locationsRcyclerView.adapter = FavoriteLocationsAdapter(favoritesList)
        locationsRcyclerView.layoutManager = LinearLayoutManager(activity)
        locationsRcyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))

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

        fun newInstance(): LocationFragment {
            return LocationFragment()
        }
    }
}// Required empty public constructor
