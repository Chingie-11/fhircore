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

package org.smartregister.fhircore.anc.util

import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Enumerations

data class RegisterConfiguration(
  val id: String,
  val primaryFilter: SearchFilter?,
  val secondaryFilter: SearchFilter?
)

/** Only TokenClientParam, and StringClientParam supported as Register Primary Filter. */
data class SearchFilter(
  val key: String,
  val filterType: Enumerations.SearchParamType,
  val valueType: Enumerations.DataType,
  val valueCoding: Coding? = null,
  val valueString: String? = null
)
