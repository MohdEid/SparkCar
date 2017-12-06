package com.example.notebookpc.sparkcar

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.example.notebookpc.sparkcar", appContext.packageName)
    }

    @Test(expected = NotImplementedError::class)
    fun cancelFirebaseListener() {
        val listener: ChildEventListener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d("TEST", "onCancelled")
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
            }
        }
        val reference = FirebaseDatabase.getInstance().getReference("/customers")
        reference.addChildEventListener(listener)
        reference.removeEventListener(listener)
        Thread.sleep(10000)

    }
}
