package com.example.notebookpc.sparkcar

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.example.notebookpc.sparkcar.adapters.FavoritesAdapter
import com.google.firebase.database.*
import org.jetbrains.anko.support.v4.find

class FavoritesFragment : Fragment() {


    private var mListener: OnFragmentInteractionListener? = null

    private lateinit var listView: ListView
    private lateinit var favoriteCleanersRef: DatabaseReference
    private lateinit var favoritesList: MutableList<String>

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        listView = find(R.id.listFavorites)
        favoritesList = mutableListOf()
        favoriteCleanersRef = FirebaseDatabase.getInstance().getReference("customers/favorite_cleaners")

        favoriteCleanersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot!!.exists()) {
                    favoritesList.clear()
                    for (item in snapshot.children) {
                        //TODO fix reading mutable list from another class
//                        val customerFavorite = Customer.newCustomer(item).favoriteCleaners
//                        favoritesList.add(Customer.newCustomer(item).favoriteCleaners)
                    }
                    val adapter = FavoritesAdapter(this@FavoritesFragment, R.layout.favorite_cleaners_layout, favoritesList)
                    listView.adapter = adapter
                }
            }

        })

        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_favorite, container, false)
    }

    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
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

        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }
}// Required empty public constructor
