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

package org.smartregister.fhircore.engine.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import org.smartregister.fhircore.engine.R
import org.smartregister.fhircore.engine.app.fakes.FakeModel.authCredentials
import org.smartregister.fhircore.engine.auth.AccountAuthenticator
import org.smartregister.fhircore.engine.data.remote.model.response.OAuthResponse
import org.smartregister.fhircore.engine.robolectric.AccountManagerShadow
import org.smartregister.fhircore.engine.robolectric.RobolectricTest
import org.smartregister.fhircore.engine.rule.CoroutineTestRule
import org.smartregister.fhircore.engine.util.DispatcherProvider
import org.smartregister.fhircore.engine.util.SecureSharedPreference
import org.smartregister.fhircore.engine.util.SharedPreferencesHelper
import retrofit2.Call
import retrofit2.Response

@ExperimentalCoroutinesApi
@HiltAndroidTest
@Config(shadows = [AccountManagerShadow::class])
internal class LoginViewModelTest : RobolectricTest() {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val instantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule(order = 2) val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

  @Inject lateinit var accountAuthenticator: AccountAuthenticator

  @Inject lateinit var dispatcherProvider: DispatcherProvider

  @Inject lateinit var sharedPreferencesHelper: SharedPreferencesHelper

  @Inject lateinit var secureSharedPreference: SecureSharedPreference

  private lateinit var loginViewModel: LoginViewModel

  private lateinit var accountAuthenticatorSpy: AccountAuthenticator

  @Before
  fun setUp() {
    hiltRule.inject()
    // Spy needed to control interaction with the real injected dependency
    accountAuthenticatorSpy = spyk(accountAuthenticator)

    loginViewModel =
      LoginViewModel(
        accountAuthenticator = accountAuthenticatorSpy,
        dispatcher = dispatcherProvider,
        sharedPreferences = sharedPreferencesHelper,
        app = ApplicationProvider.getApplicationContext()
      )
  }

  @After
  fun tearDown() {
    accountAuthenticatorSpy.secureSharedPreference.deleteCredentials()
  }

  @Test
  fun testAttemptLocalLoginWithCorrectCredentials() {
    // Simulate saving of credentials prior to login
    accountAuthenticatorSpy.secureSharedPreference.saveCredentials(authCredentials)

    // Provide username and password (The saved password is hashed, actual one is needed)
    loginViewModel.run {
      onUsernameUpdated(authCredentials.username)
      onPasswordUpdated("51r1K4l1")
    }

    val successfulLocalLogin = loginViewModel.attemptLocalLogin()
    Assert.assertTrue(successfulLocalLogin)
  }

  @Test
  fun testAttemptLocalLoginWithWrongCredentials() {
    // Simulate saving of credentials prior to login
    accountAuthenticatorSpy.secureSharedPreference.saveCredentials(authCredentials)

    // Provide username and password (The saved password is hashed, actual one is needed)
    loginViewModel.run {
      onUsernameUpdated("hello")
      onPasswordUpdated("51r1K4l1")
    }

    val successfulLocalLogin = loginViewModel.attemptLocalLogin()
    Assert.assertFalse(successfulLocalLogin)
  }

  @Test
  fun testAttemptLocalLoginWithNewUser() {

    // Provide username and password (The saved password is hashed, actual one is needed)
    loginViewModel.run {
      onUsernameUpdated("demo")
      onPasswordUpdated("51r1K4l1")
    }

    val callMock = spyk<Call<OAuthResponse>>()

    every { callMock.enqueue(any()) } just runs

    every { accountAuthenticatorSpy.fetchToken(any(), any()) } returns callMock

    loginViewModel.attemptRemoteLogin()

    // Login error is reset
    Assert.assertNotNull(loginViewModel.loginError.value)
    Assert.assertTrue(loginViewModel.loginError.value!!.isEmpty())

    // Show progress bar active
    Assert.assertNotNull(loginViewModel.showProgressBar.value)
    Assert.assertTrue(loginViewModel.showProgressBar.value!!)

    verify { accountAuthenticatorSpy.fetchToken(any(), any()) }
  }

  @Test
  fun testOauthResponseHandlerHandleSuccessfulResponse() {

    // Provide username and password (The saved password is hashed, actual one is needed)
    loginViewModel.run {
      onUsernameUpdated("demo")
      onPasswordUpdated("51r1K4l1")
    }

    val callMock = spyk<Call<OAuthResponse>>()

    val mockResponse: Response<OAuthResponse> =
      Response.success(
        OAuthResponse(
          accessToken = authCredentials.sessionToken,
          tokenType = "openid email profile",
          refreshToken = authCredentials.refreshToken,
          scope = "openid"
        )
      )

    loginViewModel.oauthResponseHandler.handleResponse(call = callMock, response = mockResponse)

    // Show progress bar inactive
    Assert.assertNotNull(loginViewModel.showProgressBar.value)
    Assert.assertFalse(loginViewModel.showProgressBar.value!!)

    // New user credentials added
    val retrieveCredentials = secureSharedPreference.retrieveCredentials()
    Assert.assertNotNull(retrieveCredentials)
    Assert.assertEquals(authCredentials.username, retrieveCredentials!!.username)
    Assert.assertEquals(authCredentials.sessionToken, retrieveCredentials.sessionToken)
  }

  @Test
  fun testForgotPasswordLoadsContact() {
    loginViewModel.forgotPassword()
    Assert.assertEquals("tel:0123456789", loginViewModel.launchDialPad.value)
  }

  @Test
  fun testAttemptRemoteLoginWithCredentialsCallsAccountAuthenticator() {

    // Provide username and password
    loginViewModel.run {
      onUsernameUpdated("testUser")
      onPasswordUpdated("51r1K4l1")
    }

    loginViewModel.attemptRemoteLogin()

    Assert.assertEquals("", loginViewModel.loginError.value)
    loginViewModel.showProgressBar.value?.let { Assert.assertTrue(it) }
    verify { accountAuthenticatorSpy.fetchToken("testUser", "51r1K4l1".toCharArray()) }
  }

  @Test
  fun testHandleErrorMessageShouldVerifyExpectedMessage() {

    ReflectionHelpers.callInstanceMethod<Any>(
      loginViewModel,
      "handleErrorMessage",
      ReflectionHelpers.ClassParameter(Throwable::class.java, UnknownHostException())
    )
    Assert.assertEquals(
      loginViewModel.app.getString(R.string.login_call_fail_error_message),
      loginViewModel.loginError.value
    )

    ReflectionHelpers.callInstanceMethod<Any>(
      loginViewModel,
      "handleErrorMessage",
      ReflectionHelpers.ClassParameter(Throwable::class.java, IOException())
    )
    Assert.assertEquals(
      loginViewModel.app.getString(R.string.invalid_login_credentials),
      loginViewModel.loginError.value
    )
  }
}
