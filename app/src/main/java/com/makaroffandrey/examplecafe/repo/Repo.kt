package com.makaroffandrey.examplecafe.repo

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.makaroffandrey.examplecafe.R
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dummy repository that returns data with a delay with some chance of error
 */
@Singleton
class Repo @Inject constructor(private val application: Application) {
    enum class Section {
        ABOUT_US, MENU, LOCATION
    }

    private val random = Random()

    private val data: Array<MutableLiveData<Resource<CardDetails>>> = Array(
            Section.values().size,
            { MutableLiveData<Resource<CardDetails>>() })
    private val timers: Array<Timer> = Array(Section.values().size, { Timer() })

    init {
        Section.values().forEach {
            retry(it)
        }
    }

    fun getData(section: Section): LiveData<Resource<CardDetails>> = data[section.ordinal]

    fun retry(section: Section) {
        val retryData = data[section.ordinal]
        timers[section.ordinal].cancel()
        val timer = Timer()
        timers[section.ordinal] = Timer()
        retryData.postValue(LoadingResource())
        timer.schedule(RefreshTask(section, this), +3000) //simulate network delay

    }

    private fun getTitle(section: Section): String = when (section) {
        Section.ABOUT_US -> application.getString(R.string.about_us_title)
        Section.MENU -> application.getString(R.string.menu_title)
        Section.LOCATION -> application.getString(R.string.location_title)
    }

    private fun getDetails(section: Section): String = when (section) {
        Section.ABOUT_US -> application.getString(R.string.lorem_ipsum)
        Section.MENU -> application.getString(R.string.lorem_ipsum)
        Section.LOCATION -> application.getString(R.string.lorem_ipsum)
    }

    private fun getBitmap(section: Section): Bitmap {
        val resourceId = when (section) {
            Section.ABOUT_US -> R.drawable.about_us
            Section.MENU -> R.drawable.menu
            Section.LOCATION -> R.drawable.location
        }
        return BitmapFactory.decodeResource(application.resources, resourceId)
    }

    private class RefreshTask(val section: Section, val repo: Repo) : TimerTask() {
        override fun run() {
            if (repo.random.nextInt(10) > 0) {
                repo.data[section.ordinal].postValue(
                        FinishedResource(
                                CardDetails(
                                        repo.getBitmap(section), repo.getTitle(section),
                                        repo.getDetails(section))))
            } else {
                //10% chance to simulate network error
                repo.data[section.ordinal].postValue(
                        ErrorResource(
                                IOException("Network error")))
            }
        }
    }

}

data class CardDetails(val picture: Bitmap, val title: CharSequence, val details: CharSequence)

