package com.ncs.marioapp.UI.SettingsScreen.EditProfile.SocialDetails

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ncs.marioapp.Domain.HelperClasses.PrefManager
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.gone
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.marioapp.Domain.Utility.ExtensionsUtil.visible
import com.ncs.marioapp.Domain.Utility.GlobalUtils
import com.ncs.marioapp.R
import com.ncs.marioapp.UI.SettingsScreen.EditProfile.EditProfileViewModel
import com.ncs.marioapp.databinding.FragmentSocialLinksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SocialLinksFragmentEditProfile : Fragment() {

    lateinit var binding: FragmentSocialLinksBinding
    private val surveyViewModel: EditProfileViewModel by activityViewModels()
    private val util: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(requireActivity())
    }
    private var backPressedTime: Long = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onBackPress()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSocialLinksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        setUpViews()
    }

    private fun observeData(){
        surveyViewModel.errorMessageSocialDetails.observe(viewLifecycleOwner, Observer { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                surveyViewModel.resetErrorMessageSocialDetails()
            }
        })
        surveyViewModel.socialDetailsPageResult.observe(viewLifecycleOwner, Observer { result ->
            if (result){
                val userSurvey= PrefManager.getUserSurvey()!!
                val links= listOf(surveyViewModel.linkedIn.value!!,surveyViewModel.github.value!!,surveyViewModel.behance.value!!,surveyViewModel.link1.value!!,surveyViewModel.link2.value!!)
                userSurvey.links=links
                PrefManager.setUserSurvey(userSurvey)
                Log.d("usercheck","${PrefManager.getUserSurvey()}")
                requireActivity().finish()
                requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                surveyViewModel.resetSocialDetailsPageResult()
                surveyViewModel.resetErrorMessageSocialDetails()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        surveyViewModel.setCurrentStep(EditProfileViewModel.SurveyStep.SOCIAL_DETAILS)
        restoreFields()
    }

    private fun restoreFields(){
        surveyViewModel.linksCount.observe(viewLifecycleOwner){
            when(it){
                0->{
                    binding.link1Et.gone()
                    binding.link2Et.gone()
                }
                1->{
                    binding.link1Et.visible()
                    binding.link2Et.gone()
                }
                2->{
                    binding.link1Et.visible()
                    binding.link2Et.visible()
                }
            }
        }
        surveyViewModel.linkedIn.observe(viewLifecycleOwner, Observer {
            if (!it.isNull) {
                binding.linkedInEt.setText(it)
            }
        })
        surveyViewModel.github.observe(viewLifecycleOwner, Observer {
            if (!it.isNull) {
                binding.githubEt.setText(it)
            }
        })
        surveyViewModel.behance.observe(viewLifecycleOwner, Observer {
            if (!it.isNull) {
                binding.behanceEt.setText(it)
            }
        })
        surveyViewModel.link1.observe(viewLifecycleOwner, Observer {
            if (!it.isNull) {
                binding.link1Et.setText(it)
            }
        })
        surveyViewModel.link2.observe(viewLifecycleOwner, Observer {
            if (!it.isNull) {
                binding.link2Et.setText(it)
            }
        })
    }


    private fun setUpViews(){
        binding.btnAddMoreLinks.setOnClickThrottleBounceListener {
            if (surveyViewModel.linksCount.value!! < 2) {
                surveyViewModel.setLinksCount(surveyViewModel.linksCount.value?.plus(1) ?: 1)
            }
        }
        binding.btnBack.setOnClickThrottleBounceListener {
            moveToPrevious()
        }
        binding.btnNext.setOnClickThrottleBounceListener {
            surveyViewModel.linkedIn.value = binding.linkedInEt.text.toString()
            surveyViewModel.github.value = binding.githubEt.text.toString()
            surveyViewModel.behance.value = binding.behanceEt.text.toString()
            surveyViewModel.link1.value = binding.link1Et.text.toString()
            surveyViewModel.link2.value = binding.link2Et.text.toString()
            surveyViewModel.validateInputsOnSocialDetailsPage()
        }
    }

    private fun onBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            moveToPrevious()
        }
    }

    fun moveToPrevious(){
        findNavController().navigate(R.id.action_fragment_social_links_ep_to_fragment_technical_ep)
    }

}