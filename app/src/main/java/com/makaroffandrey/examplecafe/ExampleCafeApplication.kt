package com.makaroffandrey.examplecafe

import com.makaroffandrey.examplecafe.di.AppComponent
import com.makaroffandrey.examplecafe.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class ExampleCafeApplication : DaggerApplication() {

    private lateinit var component: AppComponent

    override fun onCreate() {
        component = DaggerAppComponent.builder().application(this).build()
        super.onCreate()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return component
    }
}