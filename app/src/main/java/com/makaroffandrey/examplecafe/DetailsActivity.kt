package com.makaroffandrey.examplecafe

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.view.ViewGroup
import com.makaroffandrey.examplecafe.repo.FinishedResource
import com.makaroffandrey.examplecafe.repo.Repo
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_details.*
import javax.inject.Inject

class DetailsActivity : DaggerAppCompatActivity() {
    companion object {
        private const val SECTION_EXTRA = "extra_section"
        const val VIEW_NAME_IMAGE = "detail:header:image"
        const val VIEW_NAME_TITLE = "detail:header:title"
        const val VIEW_NAME_DETAILS = "detail:header:details"

        fun startIntent(section: Repo.Section, context: Context): Intent {
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra(SECTION_EXTRA, section)
            return intent
        }
    }

    @Inject
    lateinit var repo: Repo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        closeButton.setOnClickListener({ onBackPressed() })
        ViewCompat.setTransitionName(image, VIEW_NAME_IMAGE)
        ViewCompat.setTransitionName(titleText, VIEW_NAME_TITLE)
        ViewCompat.setTransitionName(detailsText, VIEW_NAME_DETAILS)
        val section = intent.getSerializableExtra(SECTION_EXTRA) as? Repo.Section
        if (section == null) {
            finish()
            return
        }
        repo.getData(section).observe(this, Observer { resource ->
            if (resource is FinishedResource) {
                image.setImageBitmap(resource.data.picture)
                titleText.text = resource.data.title
                detailsText.text = resource.data.details
            } else {
                finish()
            }
        })
    }

}