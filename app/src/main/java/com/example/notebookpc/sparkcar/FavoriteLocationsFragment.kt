package com.example.notebookpc.sparkcar

import android.arch.lifecycle.Observer
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
import kotlinx.android.synthetic.main.fragment_location.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.startActivity


class FavoriteLocationsFragment : Fragment() {


    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addLocationBtn.onClick {
            startActivity<PickLocationActivity>()
        }

        locationsRcyclerView.layoutManager = LinearLayoutManager(activity)
        locationsRcyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))

        CustomerHolder.customer.observe(this, Observer { currentCustomer ->
            val favoritesList = currentCustomer?.favoriteLocations ?: throw IllegalStateException()
            locationsRcyclerView.adapter = FavoriteLocationsAdapter(favoritesList)
        })
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

        fun newInstance(): FavoriteLocationsFragment {
            return FavoriteLocationsFragment()
        }
    }
}// Required empty public constructor
