package com.example.notebookpc.sparkcar

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.support.v4.find


class ProfileFragment : Fragment() {


    private var mListener: OnFragmentInteractionListener? = null

    //declaration of layout variables
    private lateinit var txtName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtPhone: TextView
    private lateinit var currentPW: TextView
    private lateinit var btnSave: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtEmail = find(R.id.editEmailAdress)
        txtName = find(R.id.editFirstName)
        txtPhone = find(R.id.editPhoneNumber)
        currentPW = find(R.id.editCurPw)
        btnSave = find(R.id.btnSave)
    }

    private fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        map.put("name", txtName)
        map.put("email", txtEmail)
        map.put("mobile", txtPhone)

        return map
    }

    fun pushUser(user: ProfileFragment) {
        FirebaseDatabase.getInstance().getReference("/customers").push().setValue(user.toMap())
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
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}
