package com.ncs.marioapp.UI.StartScreen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.messaging.FirebaseMessaging
import com.ncs.marioapp.BuildConfig
import com.ncs.marioapp.Domain.HelperClasses.PrefManager
import com.ncs.marioapp.Domain.HelperClasses.RemoteConfigHelper
import com.ncs.marioapp.Domain.Models.ProfileData.UserImpDetails
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.gone
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.toast
import com.ncs.marioapp.Domain.Utility.GlobalUtils
import com.ncs.marioapp.R
import com.ncs.marioapp.UI.AdminScreen.AdminMainActivity
import com.ncs.marioapp.UI.AuthScreen.AuthActivity
import com.ncs.marioapp.UI.MainScreen.MainActivity
import com.ncs.marioapp.UI.SurveyScreen.SurveyActivity
import com.ncs.marioapp.UI.WaitScreen.WaitActivity
import com.ncs.marioapp.databinding.ActivityStartScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class StartScreen : AppCompatActivity() {
    val binding: ActivityStartScreenBinding by lazy {
        ActivityStartScreenBinding.inflate(layoutInflater)
    }
    private val util: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }
    private val viewModel: StartScreenViewModel by viewModels()

    private val updateType = AppUpdateType.IMMEDIATE
    private val APP_UPDATE_REQUEST_CODE = 101
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var remoteConfigHelper: RemoteConfigHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        remoteConfigHelper = RemoteConfigHelper(this)
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        handleDynamicLink(intent)
        startAnim()

        FirebaseMessaging.getInstance().subscribeToTopic("marioapp-general")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to topic successfully")
                    setState("All ringers working fine..")
                } else {
                    Log.d("FCM", "Failed to subscribe to topic")
                    setState("Bip bop.. some errors , but carrying on..")

                }
            }

        if (updateType == AppUpdateType.IMMEDIATE) {
            appUpdateManager.registerListener(installStateUpdatedListener)
            checkforAppUpdates()
            initializeProcesses()
        }


    }

    private fun setState(text: String) {
        binding.status.text = text
        binding.status.animFadein(this@StartScreen)
    }

    override fun onStart() {
        super.onStart()
        setState("Starting up the engines...")
    }

    private fun checkforAppUpdates() {

        setState("Checking for updates...")

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when (updateType) {
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }
            if (isUpdateAvailable && isUpdateAllowed) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType,
                    this,
                    APP_UPDATE_REQUEST_CODE
                )
            }
        }
    }

    private fun initializeProcesses() {
        remoteConfigHelper.fetchRemoteConfig {
            if (BuildConfig.VERSION_CODE >= it.toInt()) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) ==
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        runNormally(true)
                    } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                        util.twoBtnDialog(
                            title = "Notification Permission required",
                            msg = "Notification permission is required for better functioning of the app",
                            positiveBtnText = "OK",
                            positive = {
                                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            },
                            negativeBtnText = "Cancel",
                            negative = {
                                runNormally(false)
                            })
                    } else {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    runNormally(true)
                }
            } else {
                util.twoBtnDialogNonCancellable("Update Available",
                    "Hooray! A new version on NCS Mario has been released on playstore, please update your app to continue using forward",
                    positiveBtnText = "Update", positive = {
                        //TODO: Open playstore
                    }, negativeBtnText = "Cancel", negative = {
                        finishAffinity()
                    })
            }
        }
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            toast("Download Successful")
            lifecycleScope.launch {
                delay(5.seconds)
                appUpdateManager.completeUpdate()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                finish()
                toast("Something went wrong while updating the app")
                setState("Bip bop.. something went wrong..")
            }
        }
    }

    val slideOutAnim by lazy { AnimationUtils.loadAnimation(this, R.anim.slide_out_right) }
    val fadeInAnim by lazy { AnimationUtils.loadAnimation(this, R.anim.fadein) }

    private fun startAnim() {

        slideOutAnim.duration = 2000
        fadeInAnim.duration = 2500
        binding.blackCircle.startAnimation(slideOutAnim)
        binding.imageView2.startAnimation(fadeInAnim)

        slideOutAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                binding.blackCircle.gone()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })


        fadeInAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                binding.imageView2.alpha = 1f
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })


    }

    private fun runNormally(isPermissionGranted: Boolean) {
        if (PrefManager.getToken() == "") {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        } else {
            bindObservers()
            observeViewModel()
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            runNormally(true)
        } else {
            util.twoBtnDialog(
                title = "Notification Permission required",
                msg = "You can always allow permissions from the App's settings.",
                positiveBtnText = "Take me there",
                positive = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", this@StartScreen.packageName, null)
                    }
                    startActivity(intent)
                },
                negativeBtnText = "Cancel",
                negative = {
                    runNormally(false)
                }
            )
        }
    }

    private fun handleDynamicLink(intent: Intent?) {
        val dynamicLinkTask = FirebaseDynamicLinks.getInstance().getDynamicLink(intent!!)
        dynamicLinkTask.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val dynamicLink = task.result
                if (dynamicLink != null) {
                    handleDeepLink(dynamicLink.link!!)
                }
            } else {
                Log.e("DynamicLink", "Error getting dynamic link", task.exception)
            }
        }
    }

    private fun handleDeepLink(uri: Uri) {
        Log.d("shareLinkTest", uri.toString())
        val pathSegments = uri.pathSegments
        Log.d("shareLinkTest", pathSegments.toString())
    }

    private fun bindObservers() {
        viewModel.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                if (it == "User not found!") {
                    PrefManager.setShowProfileCompletionAlert(true)
                    startActivity(Intent(this, SurveyActivity::class.java))
                    finish()
                } else {
                    util.showActionSnackbar(binding.root, it, Snackbar.LENGTH_INDEFINITE, "Retry") {
                        observeViewModel()
                    }
                    binding.progress.gone()
                    setState("Yikes! Couldn’t load – let’s retry.")
                }
            }
        }
    }


    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }


    private fun observeViewModel() {

        lifecycleScope.launch {
            try {
                binding.progress.show()
                setState("Compiling your user saga...")
                val userDetailsDeferred = async { viewModel.getUserImpDetails() }
                val kycTokenDeferred = async { viewModel.fetchUserKYCHeaderToken() }

                val userDetails = userDetailsDeferred.await()
                val kycStatus = kycTokenDeferred.await()

                handleUserDetails(userDetails!!)
                handleKYCStatus(kycStatus, userDetails.role)

            } catch (e: Exception) {
                binding.progress.gone()
            }
        }
    }

    private fun handleUserDetails(user: UserImpDetails) {
//        PrefManager.setUserProfile(user.profile)
//        if (user.profile.name.isNotEmpty()) {
////            PrefManager.setUserProfileForCache(user.profile)
//        }
        if (user.photo.isNotEmpty()) {
            PrefManager.setShowProfileCompletionAlert(false)
        } else {
            PrefManager.setShowProfileCompletionAlert(true)
            startActivity(Intent(this, SurveyActivity::class.java))
            finish()
        }
    }

    private fun handleKYCStatus(kycStatus: String?, role: Int) {
        if (!kycStatus.isNullOrEmpty()) {
            when (kycStatus) {
                "ACCEPT" -> {
                    if (role == 1) {
                        startActivity(Intent(this, AdminMainActivity::class.java))
                        finish()
                    } else {
                        slideOutAnim.cancel()
                        fadeInAnim.cancel()
                        binding.progress.gone()
                        setState("Starting Mario...")
                        Handler(Looper.getMainLooper()).postDelayed({
                            startActivity(Intent(this@StartScreen, MainActivity::class.java))
                            finish()
                        }, 1000)

                    }
                }

                "PENDING", "REJECT" -> {
                    startActivity(Intent(this@StartScreen, WaitActivity::class.java))
                    finish()
                }

            }
        }
    }

}