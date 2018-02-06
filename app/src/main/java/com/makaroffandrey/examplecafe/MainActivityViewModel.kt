package com.makaroffandrey.examplecafe

import android.arch.lifecycle.ViewModel
import com.makaroffandrey.examplecafe.repo.Repo
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val repo: Repo) : ViewModel() {
    fun getData(section: Repo.Section) = repo.getData(section)
    fun retry(section: Repo.Section) = repo.retry(section)
}

