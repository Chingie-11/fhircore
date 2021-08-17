package org.smartregister.fhircore.engine.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.search.count
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.Patient
import org.smartregister.fhircore.engine.configuration.app.ConfigurableApplication
import org.smartregister.fhircore.engine.configuration.view.RegisterViewConfiguration
import org.smartregister.fhircore.engine.ui.model.Language
import org.smartregister.fhircore.engine.ui.model.SideMenuOption
import org.smartregister.fhircore.engine.ui.model.SyncStatus
import org.smartregister.fhircore.engine.util.DefaultDispatcherProvider
import org.smartregister.fhircore.engine.util.DispatcherProvider
import org.smartregister.fhircore.engine.util.SharedPreferencesHelper
import org.smartregister.fhircore.engine.util.extension.syncData
import timber.log.Timber

/**
 * Subclass of [ViewModel]. This view model is responsible for updating configuration views by
 * providing [registerViewConfiguration] variable which is a [MutableLiveData] that can be observed
 * on when UI configuration changes.
 */
class RegisterViewModel(
  application: Application,
  registerViewConfiguration: RegisterViewConfiguration,
  val dispatcher: DispatcherProvider = DefaultDispatcherProvider
) : AndroidViewModel(application) {

  private val applicationConfiguration =
    (getApplication<Application>() as ConfigurableApplication).applicationConfiguration
  private val fhirEngine = (application as ConfigurableApplication).fhirEngine

  lateinit var languages: List<Language>

  val syncStatus = MutableLiveData(SyncStatus.NOT_SYNCING)

  var selectedLanguage =
    MutableLiveData(
      SharedPreferencesHelper.read(SharedPreferencesHelper.LANG, Locale.ENGLISH.toLanguageTag())
        ?: Locale.ENGLISH.toLanguageTag()
    )
  val registerViewConfiguration = MutableLiveData(registerViewConfiguration)
  fun updateViewConfigurations(registerViewConfiguration: RegisterViewConfiguration) {
    this.registerViewConfiguration.value = registerViewConfiguration
  }

  fun loadLanguages() {
    languages =
      applicationConfiguration.languages.map { Language(it, Locale.forLanguageTag(it).displayName) }
  }

  fun syncData() =
    viewModelScope.launch(dispatcher.io()) {
      try {
        getApplication<Application>().syncData(applicationConfiguration)
        syncStatus.postValue(SyncStatus.COMPLETE)
      } catch (exception: Exception) {
        Timber.e("Error syncing data", exception)
        syncStatus.postValue(SyncStatus.FAILED)
      }
    }

  suspend fun performCount(sideMenuOption: SideMenuOption): Long {
    if (sideMenuOption.countForResource &&
        sideMenuOption.entityTypePatient &&
        sideMenuOption.showCount
    ) {
      return withContext(dispatcher.io()) {
          val count = fhirEngine.count<Patient> { sideMenuOption.searchFilterLambda }.toInt()
          Timber.d("Loaded %s clients from db", count)
          count
        }
        .toLong()
    }
    return -1
  }
}