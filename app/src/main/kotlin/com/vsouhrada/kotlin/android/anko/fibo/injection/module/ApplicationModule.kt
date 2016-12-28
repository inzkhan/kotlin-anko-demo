package com.vsouhrada.apps.fibo.injection.module

import android.app.Application
import android.content.Context
import android.location.LocationManager
import com.vsouhrada.apps.fibo.core.db.bl.IUserBL
import com.vsouhrada.apps.fibo.injection.qualifier.ForApplication
import com.vsouhrada.kotlin.android.anko.fibo.BuildConfig
import com.vsouhrada.kotlin.android.anko.fibo.core.db.FiboDatabaseSource
import com.vsouhrada.kotlin.android.anko.fibo.core.db.bl.UserBL
import com.vsouhrada.kotlin.android.anko.fibo.core.rx.RxBus
import com.vsouhrada.kotlin.android.anko.fibo.function.signin.login.presenter.LoginPresenter
import com.vsouhrada.kotlin.android.anko.fibo.lib_db.entity.Models
import dagger.Module
import dagger.Provides
import io.requery.Persistable
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.TableCreationMode
import javax.inject.Singleton

/**
 * A module for Android-specific dependencies which require a [android.content.Context] or [ ] to create.
 *
 * @author vsouhrada
 * @since 0.1.0
 */
@Module
class ApplicationModule(private val application: Application) {

  @Provides
  @Singleton
  fun provideApplication(): Application {
    return application
  }

  /**
   * Allow the application context to be injected but require that it be annotated
   * with [ ][ForApplication] to explicitly differentiate it from an activity context.
   */
  @Provides
  @Singleton
  @ForApplication
  fun provideApplicationContext(): Context {
    return application
  }

  @Provides
  @Singleton
  fun provideEventBus(): RxBus {
    return RxBus()
  }

  @Provides
  @Singleton
  fun provideLocationManager(): LocationManager {
    return application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  }

  @Provides
  @Singleton
  fun provideDatabaseSource(@ForApplication context: Context): FiboDatabaseSource {
    return FiboDatabaseSource(context = context, model = Models.DEFAULT, version = 1)
  }

  @Provides
  @Singleton
  fun provideDataStore(databaseSource: FiboDatabaseSource): KotlinEntityDataStore<Persistable> {
    //val dabaseSource = DatabaseSource(context, Models.DEFAULT, 1)
    //val source = FiboDatabaseSource(context = context, model = Models.DEFAULT, version = 1)
    if (BuildConfig.DEBUG) {
      databaseSource.setTableCreationMode(TableCreationMode.DROP_CREATE)
    }
    //val config = KotlinConfiguration(Models.DEFAULT, dabaseSource.configuration)
    //val source = DatabaseSource(context, Models.DEFAULT, 1)
    //val configuration = source.configuration
    //val data = KotlinEntityDataStore<Persistable>(source.configuration)
    //val dataStore = KotlinEntityDataStore<Persistable>(source.configuration)
    var tableCreationMode = TableCreationMode.CREATE_NOT_EXISTS
    if (BuildConfig.DEBUG) {
      tableCreationMode = TableCreationMode.DROP_CREATE
    }
    //SchemaModifier(databaseSource.configuration).createTables(tableCreationMode)

    val dataStore = KotlinEntityDataStore<Persistable>(databaseSource.configuration)

    databaseSource.dataStore = dataStore

    return dataStore
  }

  @Provides
  @Singleton
  fun provideUserBusinessLogic(dataStore: KotlinEntityDataStore<Persistable>): IUserBL {
    return UserBL(dataStore)
  }

  @Provides @Singleton fun provideLoginPresenter(userBL: IUserBL): LoginPresenter {
    return LoginPresenter(userBL)
  }
}