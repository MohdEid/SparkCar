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
import com.example.notebookpc.sparkcar.adapters.OrdersListAdapter
import kotlinx.android.synthetic.main.orders_list_activity.*

class OrdersListFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.orders_list_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerOrdersList.layoutManager = LinearLayoutManager(activity)
        customerOrdersList.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))

        CustomerHolder.orders.observe(this, Observer { ordersList ->
            if (ordersList == null) {
                customerOrdersList.adapter = null
                return@Observer
            }
            val adapter = OrdersListAdapter(ordersList)
            customerOrdersList.adapter = adapter
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

        fun newInstance(): OrdersListFragment {
            return OrdersListFragment()
        }
    }
}