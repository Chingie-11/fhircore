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

package org.smartregister.fhircore.eir.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import org.smartregister.fhircore.eir.BR
import org.smartregister.fhircore.eir.BuildConfig
import org.smartregister.fhircore.eir.R
import org.smartregister.fhircore.eir.databinding.LoginBinding
import org.smartregister.fhircore.eir.viewmodel.LoginViewModel
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

  private lateinit var binding: LoginBinding
  internal lateinit var viewModel: LoginViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

    binding = DataBindingUtil.setContentView(this, R.layout.login)
    binding.setVariable(BR.usr, viewModel)
    binding.lifecycleOwner = this

    startHomeObserver()
    getAppVersion()
  }

  private fun startHomeObserver() {
    viewModel.goHome.observe(
      this,
      {
        Timber.i("GoHome value changed, now shall start home %b", it)

        if (it) {
          val intent = Intent(baseContext, CovaxListActivity::class.java)
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

          startActivity(intent)
          finish()
        }
      }
    )

    viewModel.loginFailed.observe(
      this,
      { binding.spacer.visibility = if (it == true) View.GONE else View.VISIBLE }
    )

    viewModel.showProgressIcon.observe(
      this,
      { binding.progressIcon.visibility = if (it == true) View.VISIBLE else View.INVISIBLE }
    )
  }

  private fun getAppVersion() {
    val versionName = BuildConfig.VERSION_NAME
    binding.tvAppVersion.text = getString(R.string.version, versionName)
  }
}