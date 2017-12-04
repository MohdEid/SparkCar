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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.toast


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
        txtEmail = find(R.id.emailEditText)
        txtName = find(R.id.nameEditText)
        txtPhone = find(R.id.phoneEditText)
        currentPW = find(R.id.editCurrentPassword)
        btnSave = find(R.id.btnSave)

        val currentUser = FirebaseAuth.getInstance().currentUser ?: throw AssertionError()


        if (!currentUser.displayName.isNullOrBlank()) {
            nameEditText.setText(currentUser.displayName)
        }
        if (!currentUser.email.isNullOrBlank()) {
            emailEditText.setText(currentUser.email)
        }
        if (!currentUser.phoneNumber.isNullOrBlank()) {
            phoneEditText.setText(currentUser.phoneNumber)
        }
        //TODO add current password, and/or validate password if changed

        btnSave.onClick {
            val name = nameEditText.text.toString().let {
                if (it.isBlank()) {
                    toast("Please enter your name")
                    return@onClick
                }
                it
            }

            val mobile = phoneEditText.text.toString().let {
                if (it.isBlank()) {
                    toast("Please enter your mobile number")
                    return@onClick
                }
                it
            }

            val email = emailEditText.text.toString().let {
                if (it.isBlank()) {
                    toast("Please enter your email")
                    return@onClick
                }
                it
            }

//            val uid = intent.extras.getString("id")
//            val customer =
//                    Customer(id = uid, name = name, email = email, mobile = mobile, favoriteCleaners = listOf(), favoriteLocations = listOf())
//            val task = FirebaseDatabase.getInstance().getReference("/customers/" + uid).setValue(customer.toMap())
//            task.addOnCompleteListener {
//                if (it.isSuccessful) {
//                    startActivity<TestingActivity>()
//                } else {
//                    toast("Exception occurred: " + it.exception?.message)
//
//                }
        }
    }


//
//    private fun toMap(): Map<String, Any> {
//        val map = mutableMapOf<String, Any>()
//
//        map.put("name", txtName)
//        map.put("email", txtEmail)
//        map.put("mobile", txtPhone)
//
//        return map
//    }
//
//    fun pushUser(user: ProfileFragment) {
//        FirebaseDatabase.getInstance().getReference("/customers").push().setValue(user.toMap())
//    }

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
