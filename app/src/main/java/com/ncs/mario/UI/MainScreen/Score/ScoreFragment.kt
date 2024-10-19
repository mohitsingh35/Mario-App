package com.ncs.mario.UI.MainScreen.Score

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ncs.mario.Domain.HelperClasses.PrefManager
import com.ncs.mario.R
import com.ncs.mario.databinding.FragmentScoreBinding

class ScoreFragment : Fragment() {

    companion object {
        fun newInstance() = ScoreFragment()
    }
    private var _binding: FragmentScoreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScoreViewModel by viewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScoreBinding.inflate(inflater, container, false)
        val profile = PrefManager.getUserProfile()
        binding.score.text = profile?.points.toString()
        binding.nameScore.text=profile?.name.toString()
        if(profile?.points!!>100){
            binding.level.text="Level: Intermediate"
        }
        else if(profile.points>400){
            binding.level.text="Level: Pro"
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }
}