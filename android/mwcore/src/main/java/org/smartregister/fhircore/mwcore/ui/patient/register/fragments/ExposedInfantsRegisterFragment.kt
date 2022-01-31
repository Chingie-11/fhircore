package org.smartregister.fhircore.mwcore.ui.patient.register.fragments

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.paging.compose.LazyPagingItems
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.hl7.fhir.r4.model.Patient
import org.smartregister.fhircore.engine.ui.questionnaire.QuestionnaireActivity.Companion.QUESTIONNAIRE_ARG_PATIENT_KEY
import org.smartregister.fhircore.engine.ui.register.ComposeRegisterFragment
import org.smartregister.fhircore.engine.ui.register.RegisterDataViewModel
import org.smartregister.fhircore.engine.ui.register.model.RegisterFilterType
import org.smartregister.fhircore.engine.util.ListenerIntent
import org.smartregister.fhircore.engine.util.extension.createFactory
import org.smartregister.fhircore.mwcore.data.patient.PatientRepository
import org.smartregister.fhircore.mwcore.data.patient.model.PatientItem
import org.smartregister.fhircore.mwcore.ui.patient.details.QuestPatientDetailActivity
import org.smartregister.fhircore.mwcore.ui.patient.register.OpenPatientProfile
import org.smartregister.fhircore.mwcore.ui.patient.register.components.PatientRegisterList

@AndroidEntryPoint
class ExposedInfantsRegisterFragment : ComposeRegisterFragment<Patient, PatientItem>() {

    @Inject lateinit var patientRepository: PatientRepository

    override fun navigateToDetails(uniqueIdentifier: String) {
        startActivity(
            Intent(requireActivity(), QuestPatientDetailActivity::class.java)
                .putExtra(QUESTIONNAIRE_ARG_PATIENT_KEY, uniqueIdentifier)
        )
    }

    @Composable
    override fun ConstructRegisterList(pagingItems: LazyPagingItems<PatientItem>, modifier: Modifier) {
        PatientRegisterList(
            pagingItems = pagingItems,
            modifier = modifier,
            clickListener = { listenerIntent, data -> onItemClicked(listenerIntent, data) }
        )
    }

    override fun onItemClicked(listenerIntent: ListenerIntent, data: PatientItem) {
        when (listenerIntent) {
            OpenPatientProfile -> navigateToDetails(data.id)
            else -> throw UnsupportedOperationException("Given ListenerIntent is not supported")
        }
    }

    override fun performFilter(
        registerFilterType: RegisterFilterType,
        data: PatientItem,
        value: Any
    ): Boolean {
        return when (registerFilterType) {
            RegisterFilterType.SEARCH_FILTER -> {
                if (value is String && value.isEmpty()) return true
                else
                    data.name.contains(value.toString(), ignoreCase = true) ||
                            data.identifier.contentEquals(value.toString()) ||
                            data.id == value.toString()
            }
            else -> false
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun initializeRegisterDataViewModel(): RegisterDataViewModel<Patient, PatientItem> {
        return ViewModelProvider(
            viewModelStore,
            RegisterDataViewModel(
                application = requireActivity().application,
                registerRepository = patientRepository
            )
                .createFactory()
        )[RegisterDataViewModel::class.java] as
                RegisterDataViewModel<Patient, PatientItem>
    }

    companion object {
        const val TAG = "ExposedInfantsRegisterFragment"
    }
}