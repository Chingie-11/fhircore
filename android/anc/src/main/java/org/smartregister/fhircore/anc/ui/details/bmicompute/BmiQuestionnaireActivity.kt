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

package org.smartregister.fhircore.anc.ui.details.bmicompute

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import java.util.UUID
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.smartregister.fhircore.anc.R
import org.smartregister.fhircore.engine.ui.questionnaire.QuestionnaireActivity

class BmiQuestionnaireActivity : QuestionnaireActivity() {

  val bmiQuestionnaireViewModel by viewModels<BmiQuestionnaireViewModel>()
  private var encounterId = UUID.randomUUID().toString()
  private lateinit var saveBtn: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    saveBtn = findViewById(org.smartregister.fhircore.engine.R.id.btn_save_client_info)
    saveBtn.text = getString(R.string.compute_bmi)
  }

  override fun onClick(view: View) {
    if (view.id == org.smartregister.fhircore.engine.R.id.btn_save_client_info) {
      handleQuestionnaireSubmit()
    }
  }

  override fun handleQuestionnaireResponse(questionnaireResponse: QuestionnaireResponse) {
    lifecycleScope.launch {
      val isUnitModeMetric = bmiQuestionnaireViewModel.isUnitModeMetric(questionnaireResponse)
      val inputWeight =
        bmiQuestionnaireViewModel.getInputWeight(questionnaireResponse, isUnitModeMetric)
      val inputHeight =
        bmiQuestionnaireViewModel.getInputHeight(questionnaireResponse, isUnitModeMetric)
      val computedBmi =
        bmiQuestionnaireViewModel.calculateBmi(inputHeight, inputWeight, isUnitModeMetric)
      if (computedBmi < 0)
        showErrorAlert(getString(R.string.try_again), getString(R.string.error_saving_form))
      else {
        val patientId = intent.getStringExtra(QUESTIONNAIRE_ARG_PATIENT_KEY)!!
        showBmiDataAlert(
          questionnaireResponse = questionnaireResponse,
          patientId = patientId,
          weight = inputWeight,
          height = inputHeight,
          computedBmi = computedBmi,
          isUnitModeMetric = isUnitModeMetric
        )
      }
    }
  }

  private fun showErrorAlert(title: String, message: String) {
    AlertDialog.Builder(this)
      .setTitle(title)
      .setMessage(message)
      .setCancelable(true)
      .setPositiveButton("OK") { dialogInterface, _ ->
        dialogInterface.dismiss()
        resumeForm()
      }
      .show()
  }

  private fun resumeForm() {
    dismissSaveProcessing()
  }

  private fun exitForm() {
    this.finish()
  }

  private fun showBmiDataAlert(
    questionnaireResponse: QuestionnaireResponse,
    patientId: String,
    weight: Double,
    height: Double,
    computedBmi: Double,
    isUnitModeMetric: Boolean
  ) {
    val message = bmiQuestionnaireViewModel.getBmiResult(computedBmi, this)
    AlertDialog.Builder(this)
      .setTitle(getString(R.string.your_bmi) + " $computedBmi")
      .setMessage(message)
      .setCancelable(false)
      .setNegativeButton(R.string.re_compute) { dialogInterface, _ ->
        dialogInterface.dismiss()
        resumeForm()
      }
      .setPositiveButton(R.string.str_save) { dialogInterface, _ ->
        dialogInterface.dismiss()
        proceedRecordBmi(
          patientId = patientId,
          weight = weight,
          height = height,
          computedBmi = computedBmi,
          isUnitModeMetric = isUnitModeMetric
        )
      }
      .show()
  }

  private fun proceedRecordBmi(
    patientId: String,
    weight: Double,
    height: Double,
    computedBmi: Double,
    isUnitModeMetric: Boolean
  ) {
    lifecycleScope.launch {
      val success =
        bmiQuestionnaireViewModel.saveComputedBmi(
          patientId = patientId,
          encounterId = encounterId,
          weight = weight,
          height = height,
          computedBmi = computedBmi,
          isUnitModeMetric = isUnitModeMetric
        )

      if (success) {
        exitForm()
      } else {
        showErrorAlert(getString(R.string.try_again), getString(R.string.error_saving_form))
      }
    }
  }
}
