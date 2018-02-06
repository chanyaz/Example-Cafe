package com.makaroffandrey.examplecafe

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.makaroffandrey.examplecafe.repo.ErrorResource
import com.makaroffandrey.examplecafe.repo.FinishedResource
import com.makaroffandrey.examplecafe.repo.LoadingResource
import com.makaroffandrey.examplecafe.repo.Repo
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.inside_card_layout.view.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    private lateinit var drawerToggle: ActionBarDrawerToggle

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[MainActivityViewModel::class.java]
        setContentView(R.layout.activity_main)
        supportActionBar?.title = null
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, 0, 0)
        drawerLayout.addDrawerListener(drawerToggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        navigation.setNavigationItemSelectedListener({
            Toast.makeText(this, R.string.not_implemented, Toast.LENGTH_SHORT).show()
            true
        })
        bindCardView(aboutUsCard, Repo.Section.ABOUT_US)
        bindCardView(menuCard, Repo.Section.MENU)
        bindCardView(locationCard, Repo.Section.LOCATION)
        fab.setOnClickListener(
                { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+79852539198"))) })
    }

    private fun bindCardView(view: View, section: Repo.Section) {
        view.retryButton.setOnClickListener({ viewModel.retry(section) })
        view.setOnClickListener({
            if (view.cardContent.visibility == View.VISIBLE) {
                val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        Pair(view.image, DetailsActivity.VIEW_NAME_IMAGE),
                        Pair(view.title, DetailsActivity.VIEW_NAME_TITLE),
                        Pair(view.details, DetailsActivity.VIEW_NAME_DETAILS))
                startActivity(DetailsActivity.startIntent(section, this),
                        activityOptions.toBundle())
            }
        })
        viewModel.getData(section).observe(this, Observer { resource ->
            when (resource) {
                is LoadingResource -> {
                    view.cardError.visibility = View.GONE
                    view.cardProgress.visibility = View.VISIBLE
                    view.cardContent.visibility = View.GONE
                }
                is ErrorResource -> {
                    view.cardError.visibility = View.VISIBLE
                    view.cardProgress.visibility = View.GONE
                    view.cardContent.visibility = View.GONE
                }
                is FinishedResource -> {
                    view.cardError.visibility = View.GONE
                    view.cardProgress.visibility = View.GONE
                    view.cardContent.visibility = View.VISIBLE
                    view.image.setImageBitmap(resource.data.picture)
                    view.title.text = resource.data.title
                    view.details.text = resource.data.details
                }
            }
        })
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return if (drawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
}
