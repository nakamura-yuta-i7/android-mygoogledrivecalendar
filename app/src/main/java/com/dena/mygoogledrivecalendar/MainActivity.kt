package com.dena.mygoogledrivecalendar

import kotlinx.android.synthetic.main.activity_main.*

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.*
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event


class MainActivity : AppCompatActivity(),
    GoogleApiClient.OnConnectionFailedListener,
    View.OnClickListener {

    private val TAG = "MainActivity"
    private val RC_SIGN_IN = 9001

    var mGoogleApiClient: GoogleApiClient? = null

    private lateinit var calendar: MyGoogleCalendar
    private fun setCalendarApi(email: String) {
        calendar = MyGoogleCalendar(applicationContext, email = email)
    }

    fun tappedGetEventsButton(v: View) {
        calendar.getEvents {
            events -> Log.d("events", events.toString() )
            events.forEach { event: Event ->
                Log.d("event.summary", event.summary)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.DRIVE_FILE))
            .requestScopes(Scope(CalendarScopes.CALENDAR_READONLY))
            .requestEmail()
            .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        sign_in_button.setOnClickListener(this)
        sign_out_button.setOnClickListener(this)
        disconnect_button.setOnClickListener(this)

        sign_out_button.visibility = View.GONE
        disconnect_button.visibility = View.GONE

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in_button -> signIn()
            R.id.sign_out_button -> signOut()
            R.id.disconnect_button -> revokeAccess()
        }
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback {
            sign_in_button.visibility = View.VISIBLE
            sign_out_button.visibility = View.GONE
            disconnect_button.visibility = View.GONE
        }
    }

    private fun signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback {
            sign_in_button.visibility = View.VISIBLE
            sign_out_button.visibility = View.GONE
            disconnect_button.visibility = View.GONE
        }
    }

    public override fun onStart() {
        super.onStart()

        val opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient)
        if (opr.isDone) {
            Log.d(TAG, "Got cached sign-in")
            val result = opr.get()
            handleSignInResult(result)
        } else {
            opr.setResultCallback { googleSignInResult ->
                handleSignInResult(googleSignInResult)
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            val email = acct!!.email.toString()
            textView.text = email
            setCalendarApi(email)

            sign_in_button.visibility = View.GONE
            sign_out_button.visibility = View.VISIBLE
            disconnect_button.visibility = View.VISIBLE
        } else {
            textView.text = ""
            sign_out_button.visibility = View.GONE
            disconnect_button.visibility = View.GONE
        }
    }
}
