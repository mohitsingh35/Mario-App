package com.ncs.marioapp.UI.EventDetailsScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ncs.marioapp.Domain.Models.Answer
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.fadeInAndVisible
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.fadeOutAndGone
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.runDelayed
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.marioapp.Domain.Utility.GlobalUtils
import com.ncs.marioapp.R
import com.ncs.marioapp.UI.EventDetailsScreen.Adapters.QuestionPagerAdapter
import com.ncs.marioapp.databinding.FragmentEventEnrolledBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class EventEnrolledFragment : Fragment() {
    private  var _binding: FragmentEventEnrolledBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: QuestionPagerAdapter
    private val answers = mutableListOf<Answer>()
    private val viewModel: EventDetailsViewModel by activityViewModels()
    private val util: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventEnrolledBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        observeViewModel()
    }

    private fun observeViewModel(){
        viewModel.errorMessage.observe(viewLifecycleOwner){
            if (!it.isNull) {
                util.showActionSnackbar(binding.root, it.toString(), 4000, "Retry") {
                    viewModel.getEventDetails(viewModel.getEvent()!!._id)
                }
                viewModel.resetErrorMessage()
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) {

        }

    }

    private fun setUpViews(){

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.getMyEvents()
            viewModel.getEvents()
        }

        binding.text.fadeInAndVisible(500)

        runDelayed(3000){
            binding.text.fadeOutAndGone()
            binding.ticketView.fadeInAndVisible(500)
        }

        val event=viewModel.getEvent()!!

        binding.qr.setImageBitmap(viewModel.ticketResultBitmap.value)

        binding.title.text = event.title
        if (event.time.isNullOrEmpty()) {
            binding.time.text = "TBA"
            binding.date.text = "TBA"
        } else {
            val (formattedDate, formattedTime) = formatTimestamp(event.time.toLong())
            binding.time.text = formattedTime
            binding.date.text = formattedDate
        }
        binding.venue.text = event.venue

        binding.backkk.setOnClickThrottleBounceListener{
            findNavController().navigate(R.id.action_fragment_event_enrolled_to_fragment_event_details)
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onBackPress()
    }


    private fun onBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_fragment_event_enrolled_to_fragment_event_details)
        }
    }

    fun formatTimestamp(timestamp: Long): Pair<String, String> {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        val formattedTime = timeFormat.format(date)
        return Pair(formattedDate, formattedTime)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}