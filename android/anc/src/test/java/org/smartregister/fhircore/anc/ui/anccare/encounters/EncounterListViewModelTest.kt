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

import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.smartregister.fhircore.anc.data.EncounterRepository
import org.smartregister.fhircore.anc.robolectric.RobolectricTest

class EncounterListViewModelTest : RobolectricTest() {

  private lateinit var encounterRepository: EncounterRepository

  private lateinit var encounterListViewModel: EncounterListViewModel

  @Before
  fun setUp() {
    encounterRepository = EncounterRepository(mockk())
    encounterListViewModel = EncounterListViewModel(encounterRepository)
  }

  @Test
  fun testShouldVerifyBackClickListener() {
    encounterListViewModel.onAppBackClick()
    Assert.assertNotNull(encounterListViewModel.onBackClick)
    Assert.assertEquals(true, encounterListViewModel.onBackClick.value!!)
  }
}
