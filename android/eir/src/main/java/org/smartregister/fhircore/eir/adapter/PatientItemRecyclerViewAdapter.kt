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

package org.smartregister.fhircore.eir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.smartregister.fhircore.eir.R
import org.smartregister.fhircore.eir.fragment.CovaxListFragment
import org.smartregister.fhircore.eir.model.PatientItem
import org.smartregister.fhircore.eir.viewholder.PatientItemViewHolder

/** UI Controller helper class to monitor Patient viewmodel and display list of patients. */
class PatientItemRecyclerViewAdapter(
  private val onItemClicked: (CovaxListFragment.Intention, PatientItem) -> Unit
) : ListAdapter<PatientItem, PatientItemViewHolder>(PatientItemDiffCallback()) {

  class PatientItemDiffCallback : DiffUtil.ItemCallback<PatientItem>() {
    override fun areItemsTheSame(oldItem: PatientItem, newItem: PatientItem): Boolean =
      oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: PatientItem, newItem: PatientItem): Boolean =
      oldItem == newItem
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientItemViewHolder =
    PatientItemViewHolder(
      LayoutInflater.from(parent.context).inflate(R.layout.patient_list_item, parent, false)
    )

  override fun onBindViewHolder(holder: PatientItemViewHolder, position: Int) {
    val item = currentList[position]

    holder.bindTo(item, onItemClicked)
  }
}