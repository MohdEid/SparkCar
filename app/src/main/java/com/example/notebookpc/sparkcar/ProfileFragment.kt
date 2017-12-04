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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.longToast
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
        btnSave = find(R.id.btnSave)

        val currentUser = CustomerHolder.customer ?: throw AssertionError()


        nameEditText.setText(currentUser.name)
        emailEditText.setText(currentUser.email)
        phoneEditText.setText(currentUser.mobile)

        //TODO add current password, and/or validate password if changed
        resetPassword.onClick {
            FirebaseAuth.getInstance().sendPasswordResetEmail(currentUser.email).addOnCompleteListener {
                if (it.isSuccessful) {
                    longToast("Please check your email for instructions to reset your password")
                } else {
                    longToast("Error: ${it.exception?.message}")
                }
            }
        }

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

            val uid = currentUser.id
            val customer = currentUser.copy(name = name, mobile = mobile, email = email)
            val task = FirebaseDatabase.getInstance().getReference("/customers/" + uid).setValue(customer.toMap())
            task.addOnCompleteListener {
                if (it.isSuccessful) {
                    toast("Your profile was updated successfully")
                    CustomerHolder.customer = customer
                } else {
                    toast("Error: " + it.exception?.message)
                }
            }
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
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}
