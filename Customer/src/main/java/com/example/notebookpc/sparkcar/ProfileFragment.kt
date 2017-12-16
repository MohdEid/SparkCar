package com.example.notebookpc.sparkcar

import android.arch.lifecycle.Observer
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.support.v4.toast


class ProfileFragment : Fragment() {


    private var mListener: OnFragmentInteractionListener? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = CustomerHolder.customer


        //TODO add a picture for the user to use
        currentUser.observe(this, Observer {
            val value = it ?: throw IllegalStateException()
            nameEditText.setText(value.name)
            emailEditText.setText(value.email)
            phoneEditText.setText(value.mobile)
        })

        resetPassword.onClick {
            val customerValue = currentUser.value ?: throw IllegalStateException()
            val email = customerValue.email
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
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

            val customerValue = currentUser.value ?: throw IllegalStateException()
            val customer = customerValue.copy(name = name, mobile = mobile, email = email)
            CustomerHolder.updateCustomer(customer) {
                if (it.isSuccessful) {
                    toast("Profile updated successfully")
                } else {
                    toast("Update failed: ${it.exception?.message}")
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
