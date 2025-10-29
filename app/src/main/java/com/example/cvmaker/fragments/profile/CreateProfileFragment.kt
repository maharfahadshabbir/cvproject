package com.example.cvmaker.fragments.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cvmaker.R
import com.example.cvmaker.databinding.FragmentCreateProfileBinding

class CreateProfileFragment : Fragment() {

    // Nullable binding
    private var _binding: FragmentCreateProfileBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "CreateProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClicks()
    }

    private fun logClick(name: String) {
        Log.d(TAG, "Clicked: $name")
    }

    private fun setClicks() = with(binding) {
        // Toolbar actions
        backButton.setOnClickListener {
            logClick("backButton")
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        toolbarTitle.setOnClickListener { logClick("toolbarTitle") }

        // Preview chip (container + inner views)
        preview.setOnClickListener { logClick("preview container") }

        // Section tiles
        personalDetail.setOnClickListener {
            findNavController().navigate(R.id.personalDetailFragment)
            logClick("personalDetail")
        }
        educationDetail.setOnClickListener {
            findNavController().navigate(R.id.educationDetailFragment)
            logClick("educationDetail")
        }
        experience.setOnClickListener {
            findNavController().navigate(R.id.experienceDetailFragment)
            logClick("experience")
        }
        skills.setOnClickListener {
            findNavController().navigate(R.id.skillsDetailsFragment)
            logClick("skills")
        }
        objective.setOnClickListener {
            findNavController().navigate(R.id.objectiveFragment)
            logClick("objective")
        }
        references.setOnClickListener {
            findNavController().navigate(R.id.referencesDetailFragment)
            logClick("references")
        }
        projects.setOnClickListener {
            findNavController().navigate(R.id.projectDetailsFragment)
            logClick("projects")
        }
        interest.setOnClickListener {
            findNavController().navigate(R.id.interestDetailFragment)
            logClick("interest")
        }
        portfolio.setOnClickListener {
            findNavController().navigate(R.id.portfolioDetailsFragment)
            logClick("portfolio")
        }
        certification.setOnClickListener {
            findNavController().navigate(R.id.certificationDetailsFragment)
            logClick("certification")
        }
        achievements.setOnClickListener {
            findNavController().navigate(R.id.achievementsDetailsFragment)
            logClick("achievements")
        }
        languages.setOnClickListener {
            findNavController().navigate(R.id.languageDetailsFragment)
            logClick("languages")
        }


        // Bottom primary button
        profile.setOnClickListener { logClick("create_profile button") }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
