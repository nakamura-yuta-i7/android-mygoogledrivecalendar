package com.dena.mygoogledrivecalendar

import android.app.Activity
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import java.util.*

class MyGoogleCalendar(
    context: Context,
    val email: String) {

    var mCredential: GoogleAccountCredential
    var mService: Calendar

    init {
        mCredential = GoogleAccountCredential.usingOAuth2(
            context, Arrays.asList(CalendarScopes.CALENDAR_READONLY))
            .setBackOff(ExponentialBackOff())

        mCredential.selectedAccountName = email

        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()

        mService = com.google.api.services.calendar.Calendar.Builder(
            transport, jsonFactory, mCredential)
            .setApplicationName("Google Calendar API Android Quickstart")
            .build()
    }

    fun getEvents(onSuccess: (events: List<Event>) -> Unit) {

        object : MyAsyncTask() {
            override fun doInBackground(vararg params: Void): String? {

                val now = DateTime(System.currentTimeMillis())
                val eventStrings = ArrayList<String>()

                val events = mService!!.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()

                onSuccess(events.items)

//                val items = events.items
//
//                for (event in items) {
//                    var start: DateTime? = event.start.dateTime
//                    if (start == null) {
//                        start = event.start.date
//                    }
//                    eventStrings.add(
//                        String.format("%s (%s)", event.summary, start))
//                }
//                Log.d("eventString", eventStrings.toString())

                return null
            }
        }.execute()
    }
}