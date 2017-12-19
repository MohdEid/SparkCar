package com.example.notebookpc.sparkcarcleaner

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.notebookpc.sparkcarcleaner.adapter.PendingOrdersAdapter
import kotlinx.android.synthetic.main.activity_pending_order.*

class PendingOrderFragment : Fragment() {
    private var mListener: OnFragmentInteractionListener? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_pending_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pendingOrdersRecyclerView.layoutManager = LinearLayoutManager(activity)
        pendingOrdersRecyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))

        CleanerHolder.orders.observe(this, Observer { ordersList ->
            if (ordersList == null) {
                pendingOrdersRecyclerView.adapter = null
                return@Observer
            }
            val adapter = PendingOrdersAdapter(ordersList)
            pendingOrdersRecyclerView.adapter = adapter
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


    interface OnFragmentInteractionListener

    companion object {

        fun newInstance(): PendingOrderFragment {
            val fragment = PendingOrderFragment()
            return fragment
        }
    }
}// Required empty public constructor

