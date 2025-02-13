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

package org.smartregister.fhircore.anc.ui.anccare.encounters

import android.app.Activity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.spyk
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.Robolectric
import org.smartregister.fhircore.anc.robolectric.ActivityRobolectricTest

@HiltAndroidTest
class EncounterListActivityTest : ActivityRobolectricTest() {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  private lateinit var encounterListActivity: EncounterListActivity

  @Before
  fun setUp() {
    hiltRule.inject()
    encounterListActivity =
      spyk(Robolectric.buildActivity(EncounterListActivity::class.java).create().resume().get())
  }

  @Test
  fun testHandleBackClickedShouldCallFinishMethod() {
    encounterListActivity.encounterListViewModel.onAppBackClick()
    Assert.assertTrue(encounterListActivity.isFinishing)
  }

  override fun getActivity(): Activity {
    return encounterListActivity
  }
}
