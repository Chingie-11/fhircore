/*
 * Copyright 2021 Ona Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smartregister.fhircore.engine.ui.userprofile

import android.os.Looper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import javax.inject.Inject
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.Shadows
import org.smartregister.fhircore.engine.auth.AccountAuthenticator
import org.smartregister.fhircore.engine.configuration.ConfigurationRegistry
import org.smartregister.fhircore.engine.robolectric.RobolectricTest
import org.smartregister.fhircore.engine.sync.SyncBroadcaster
import org.smartregister.fhircore.engine.sync.SyncInitiator
import org.smartregister.fhircore.engine.ui.register.model.Language
import org.smartregister.fhircore.engine.util.SecureSharedPreference
import org.smartregister.fhircore.engine.util.SharedPreferencesHelper

@HiltAndroidTest
class UserProfileViewModelTest : RobolectricTest() {

  @get:Rule var hiltRule = HiltAndroidRule(this)

  lateinit var userProfileViewModel: UserProfileViewModel
  lateinit var accountAuthenticator: AccountAuthenticator
  lateinit var secureSharedPreference: SecureSharedPreference
  lateinit var sharedPreferencesHelper: SharedPreferencesHelper
  lateinit var configurationRegistry: ConfigurationRegistry

  @Inject lateinit var realConfigurationRegistry: ConfigurationRegistry

  val syncBroadcaster = SyncBroadcaster

  @Before
  fun setUp() {
    hiltRule.inject()
    accountAuthenticator = mockk()
    secureSharedPreference = mockk()
    sharedPreferencesHelper = mockk()
    configurationRegistry = mockk()
    userProfileViewModel =
      UserProfileViewModel(
        syncBroadcaster,
        accountAuthenticator,
        secureSharedPreference,
        sharedPreferencesHelper,
        configurationRegistry
      )
  }

  @Test
  fun testRunSync() {
    val mockSyncInitiator = mockk<SyncInitiator> { every { runSync() } returns Unit }
    syncBroadcaster.unRegisterSyncInitiator()
    syncBroadcaster.registerSyncInitiator(mockSyncInitiator)
    userProfileViewModel.runSync()
    verify { mockSyncInitiator.runSync() }
  }

  @Test
  fun testRetrieveUsernameShouldReturnDemo() {
    every { secureSharedPreference.retrieveSessionUsername() } returns "demo"

    Assert.assertEquals("demo", userProfileViewModel.retrieveUsername())
    verify { secureSharedPreference.retrieveSessionUsername() }
  }

  @Test
  fun testLogoutUserShouldCallAuthLogoutService() {
    every { accountAuthenticator.logout() } returns Unit

    userProfileViewModel.logoutUser()

    verify(exactly = 1) { accountAuthenticator.logout() }
    Shadows.shadowOf(Looper.getMainLooper()).idle()
    Assert.assertTrue(userProfileViewModel.onLogout.value!!)
  }

  @Test
  fun allowSwitchingLanguagesShouldReturnTrueWhenMultipleLanguagesAreConfigured() {
    val languages = listOf(Language("es", "Spanish"), Language("en", "English"))
    userProfileViewModel = spyk(userProfileViewModel)

    every { userProfileViewModel.languages } returns languages

    Assert.assertTrue(userProfileViewModel.allowSwitchingLanguages())
  }

  @Test
  fun allowSwitchingLanguagesShouldReturnFalseWhenConfigurationIsFalse() {
    val languages = listOf(Language("es", "Spanish"))
    userProfileViewModel = spyk(userProfileViewModel)

    every { userProfileViewModel.languages } returns languages

    Assert.assertFalse(userProfileViewModel.allowSwitchingLanguages())
  }

  @Test
  fun loadSelectedLanguage() {
    every { sharedPreferencesHelper.read(SharedPreferencesHelper.LANG, any()) } returns "fr"

    Assert.assertEquals("French", userProfileViewModel.loadSelectedLanguage())
    verify { sharedPreferencesHelper.read(SharedPreferencesHelper.LANG, "en") }
  }

  @Test
  fun setLanguageShouldCallSharedPreferencesHelperWriteWithSelectedLanguageTagAndPostValue() {
    val language = Language("es", "Spanish")
    var postedValue: Language? = null

    every { sharedPreferencesHelper.write(any(), any<String>()) } just runs

    userProfileViewModel.language.observeForever { postedValue = it }

    userProfileViewModel.setLanguage(language)

    Shadows.shadowOf(Looper.getMainLooper()).idle()

    verify { sharedPreferencesHelper.write(SharedPreferencesHelper.LANG, "es") }
    Assert.assertEquals(language, postedValue!!)
  }

  @Test
  fun fetchLanguagesShouldReturnEnglishAndSwahiliAsModels() {
    every { accountAuthenticator.launchLoginScreen() } just runs
    realConfigurationRegistry.loadAppConfigurations("appId", accountAuthenticator) {}
    userProfileViewModel =
      UserProfileViewModel(
        syncBroadcaster,
        accountAuthenticator,
        secureSharedPreference,
        sharedPreferencesHelper,
        realConfigurationRegistry
      )

    val languages = userProfileViewModel.fetchLanguages()
    Assert.assertEquals("English", languages[0].displayName)
    Assert.assertEquals("en", languages[0].tag)
    Assert.assertEquals("Swahili", languages[1].displayName)
    Assert.assertEquals("sw", languages[1].tag)
  }

  @Test
  fun languagesLazyPropertyShouldRunFetchLanguagesAndReturnConfiguredLanguages() {
    realConfigurationRegistry.appId = "appId"
    every { accountAuthenticator.launchLoginScreen() } just runs
    userProfileViewModel =
      spyk(
        UserProfileViewModel(
          syncBroadcaster,
          accountAuthenticator,
          secureSharedPreference,
          sharedPreferencesHelper,
          realConfigurationRegistry
        )
      )
    every { userProfileViewModel.fetchLanguages() } returns mockk()

    val languages = userProfileViewModel.languages

    Assert.assertEquals("English", languages[0].displayName)
    Assert.assertEquals("en", languages[0].tag)
    Assert.assertEquals("Swahili", languages[1].displayName)
    Assert.assertEquals("sw", languages[1].tag)
  }
}
