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

package org.smartregister.fhircore.engine.data.local

import com.google.android.fhir.FhirEngine
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.Resource
import org.smartregister.fhircore.engine.util.DefaultDispatcherProvider
import org.smartregister.fhircore.engine.util.DispatcherProvider
import org.smartregister.fhircore.engine.util.extension.loadResource

class DefaultRepository(
  val fhirEngine: FhirEngine,
  val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider
) {

  suspend inline fun <reified T : Resource> loadResource(structureMapId: String): T? {
    return withContext(dispatcherProvider.io()) { fhirEngine.loadResource(structureMapId) }
  }

  suspend fun save(resource: Resource) {
    return withContext(dispatcherProvider.io()) { fhirEngine.save(resource) }
  }
}