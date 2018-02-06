package com.makaroffandrey.examplecafe.di

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.makaroffandrey.examplecafe.*
import dagger.*
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.multibindings.IntoMap
import javax.inject.Scope
import javax.inject.Singleton
import kotlin.reflect.KClass


@Singleton
@Component(
        modules = [AndroidSupportInjectionModule::class, ActivityModule::class, ViewModelModule::class])
interface AppComponent : AndroidInjector<ExampleCafeApplication> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}

@Suppress("unused") //dagger abstract modules are just declarations and will never be implemented.
@Module
abstract class ActivityModule {
    @PerActivity
    @ContributesAndroidInjector
    abstract fun provideMainActivity(): MainActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun provideDetailsActivity(): DetailsActivity
}


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Retention(AnnotationRetention.RUNTIME)
@Scope
internal annotation class PerActivity

@Suppress("unused") //dagger abstract modules are just declarations and will never be implemented.
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindMainActivityViewModel(viewModel: MainActivityViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(
            viewModelFactory: ExampleCafeViewModelFactory): ViewModelProvider.Factory
}
