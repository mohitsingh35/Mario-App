package com.ncs.mario.UI.SettingsScreen.Notifications

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ncs.mario.Domain.HelperClasses.PrefManager
import com.ncs.mario.R
import com.ncs.mario.Domain.Utility.ExtensionsUtil.gone
import com.ncs.mario.Domain.Utility.GlobalUtils
import com.ncs.mario.databinding.ActivityNotificationsPrefBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsPrefActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsPrefBinding
    private lateinit var viewModel: NotificationsPrefViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationsPrefBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(NotificationsPrefViewModel::class.java)
        setUpViews()
        observeViewModel()
    }

    private fun setUpViews() {
        binding.actionBar.btnHam.setImageResource(R.drawable.ic_back_arrow)
        binding.actionBar.btnHam.setOnClickListener {
            onBackPressed()
        }
        binding.actionBar.titleTv.text = "App Notifications"
        binding.actionBar.score.gone()

        binding.allNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAllNotifications(isChecked)
        }

        binding.postNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPostNotification(isChecked)
        }

        binding.pollNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPollNotification(isChecked)
        }

        binding.eventNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEventNotification(isChecked)
        }

        binding.merchNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setMerchNotification(isChecked)
        }
    }

    private fun observeViewModel() {
        viewModel.postNotification.observe(this) { isChecked ->
            binding.postNotificationsSwitch.isChecked = isChecked
        }

        viewModel.pollNotification.observe(this) { isChecked ->
            binding.pollNotificationsSwitch.isChecked = isChecked
        }

        viewModel.eventNotification.observe(this) { isChecked ->
            binding.eventNotificationsSwitch.isChecked = isChecked
        }

        viewModel.merchNotification.observe(this) { isChecked ->
            binding.merchNotificationsSwitch.isChecked = isChecked
        }

        viewModel.allNotifications.observe(this) { isChecked ->
            binding.allNotificationsSwitch.setOnCheckedChangeListener(null)
            binding.allNotificationsSwitch.isChecked = isChecked
            binding.allNotificationsSwitch.setOnCheckedChangeListener { _, newIsChecked ->
                viewModel.setAllNotifications(newIsChecked)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }
}
