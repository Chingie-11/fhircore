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

package org.smartregister.fhircore.quest.ui.patient.register

import android.app.Activity
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.size
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.util.ReflectionHelpers
import org.smartregister.fhircore.engine.auth.AccountAuthenticator
import org.smartregister.fhircore.engine.configuration.ConfigurationRegistry
import org.smartregister.fhircore.engine.databinding.BaseRegisterActivityBinding
import org.smartregister.fhircore.engine.ui.register.model.RegisterItem
import org.smartregister.fhircore.engine.ui.userprofile.UserProfileFragment
import org.smartregister.fhircore.quest.R
import org.smartregister.fhircore.quest.robolectric.ActivityRobolectricTest

@HiltAndroidTest
class PatientRegisterActivityTest : ActivityRobolectricTest() {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @Inject lateinit var configurationRegistry: ConfigurationRegistry

  @Inject lateinit var accountAuthenticator: AccountAuthenticator

  private lateinit var patientRegisterActivity: PatientRegisterActivity

  @Before
  fun setUp() {
    hiltRule.inject()
    configurationRegistry.loadAppConfigurations("quest", accountAuthenticator) {}
    patientRegisterActivity =
      Robolectric.buildActivity(PatientRegisterActivity::class.java).create().resume().get()
  }

  @Test
  fun testSideMenuOptionsShouldReturnZeroOptions() {
    Assert.assertTrue(patientRegisterActivity.sideMenuOptions().isEmpty())
  }

  @Test
  fun testOnSideMenuOptionSelectedShouldReturnTrue() {
    Assert.assertTrue(patientRegisterActivity.onNavigationOptionItemSelected(RoboMenuItem()))
  }

  @Test
  fun testOnClientMenuOptionSelectedShouldLaunchPatientRegisterFragment() {
    patientRegisterActivity.onNavigationOptionItemSelected(
      RoboMenuItem().apply { itemId = R.id.menu_item_clients }
    )
    // switched to patient register fragment
    Assert.assertEquals(
      "Clients",
      patientRegisterActivity.findViewById<TextView>(R.id.register_filter_textview).text
    )
    Assert.assertEquals(
      View.VISIBLE,
      patientRegisterActivity.findViewById<View>(R.id.filter_register_button).visibility
    )
    Assert.assertEquals(
      View.VISIBLE,
      patientRegisterActivity.findViewById<View>(R.id.edit_text_search).visibility
    )
  }

  @Test
  fun testOnSettingMenuOptionSelectedShouldLaunchUserProfileFragment() {
    patientRegisterActivity.onNavigationOptionItemSelected(
      RoboMenuItem().apply { itemId = R.id.menu_item_settings }
    )
    // switched to user profile fragment
    Assert.assertEquals(
      "Settings",
      patientRegisterActivity.findViewById<TextView>(R.id.register_filter_textview).text
    )
    Assert.assertEquals(
      View.GONE,
      patientRegisterActivity.findViewById<View>(R.id.middle_toolbar_section).visibility
    )
    Assert.assertEquals(
      View.GONE,
      patientRegisterActivity.findViewById<ImageButton>(R.id.filter_register_button).visibility
    )
  }

  @Test
  fun testSetupConfigurableViewsShouldUpdateViews() {
    val activityBinding =
      ReflectionHelpers.getField<BaseRegisterActivityBinding>(
        patientRegisterActivity,
        "registerActivityBinding"
      )

    with(activityBinding) {
      Assert.assertEquals("Register new client", this.btnRegisterNewClient.text)
      Assert.assertEquals("Clients", this.toolbarLayout.tvClientsListTitle.text)
      Assert.assertEquals(2, this.bottomNavView.menu.size)
    }
  }

  @Test
  fun testBottomMenuOptionsShouldReturnNonZeroOptions() {
    val menu = patientRegisterActivity.bottomNavigationMenuOptions()

    Assert.assertEquals(2, menu.size)
    Assert.assertEquals(R.id.menu_item_clients, menu[0].id)
    Assert.assertEquals(getString(R.string.menu_clients), menu[0].title)
    Assert.assertEquals(R.id.menu_item_settings, menu[1].id)
    Assert.assertEquals(getString(R.string.menu_settings), menu[1].title)
  }

  @Test
  fun testSupportedFragmentsShouldReturnPatientRegisterFragmentList() {
    val fragments = patientRegisterActivity.supportedFragments()
    Assert.assertEquals(2, fragments.size)
    Assert.assertTrue(fragments.containsKey(PatientRegisterFragment.TAG))
    Assert.assertTrue(fragments.containsKey(UserProfileFragment.TAG))
  }

  @Test
  fun testRegistersListShouldReturnOnlyOneItemList() {
    val list =
      ReflectionHelpers.callInstanceMethod<List<RegisterItem>>(
        patientRegisterActivity,
        "registersList"
      )
    Assert.assertEquals(1, list.size)
    with(list[0]) {
      Assert.assertEquals(PatientRegisterFragment.TAG, uniqueTag)
      Assert.assertEquals(patientRegisterActivity.getString(R.string.clients), title)
      Assert.assertTrue(isSelected)
    }
  }

  override fun getActivity(): Activity {
    return patientRegisterActivity
  }
}
