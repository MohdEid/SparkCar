package com.example.notebookpc.sparkcar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.notebookpc.sparkcarcommon.data.Cleaner
import com.example.notebookpc.sparkcarcommon.data.Messages
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_new_message.*
import org.jetbrains.anko.toast

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        val cleanerId = intent.getStringExtra("cleanerId")
        FirebaseDatabase.getInstance().getReference("/cleaners/$cleanerId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(p0: DataSnapshot?) {
                val cleaner = Cleaner.newCleaner(p0)
                recipientTextView.text = cleaner.name
            }
        })

        sendButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val text = messageEditText.text.toString()
            if (title.isBlank()) {
                toast("Cannot send message with no title")
                return@setOnClickListener
            }
            if (text.isBlank()) {
                toast("Cannot send empty message")
                return@setOnClickListener
            }

            val customerId = CustomerHolder.customer.value?.id ?: throw IllegalStateException()
            val message = Messages(id = null, title = title, content = text, customerId = customerId, cleanerId = cleanerId)
            FirebaseDatabase.getInstance().getReference("/messages").push().setValue(message.toMap()).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("Message has been sent")
                    finish()
                } else {
                    toast("Error: ${task.exception?.message}")
                }
            }
        }
    }
}
