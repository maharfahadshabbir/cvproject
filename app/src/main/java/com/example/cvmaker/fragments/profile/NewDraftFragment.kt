package com.example.cvmaker.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.cvmaker.R
import com.example.cvmaker.databinding.FragmentNewDraftBinding
import com.example.cvmaker.viewmodels.NewProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewDraftFragment : Fragment() {

    private var _binding: FragmentNewDraftBinding? = null
    private val binding get() = _binding!!

    private val newProfileViewModel by activityViewModels<NewProfileViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewDraftBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            newProfileViewModel.profiles.collect { entities ->
                val drafts = entities.filter { !it.profileOrDraft } // or however you mark drafts
                if (drafts.isEmpty()) {
                    binding.emptyDataConst.visibility = View.VISIBLE
                    binding.profilesRecyclerview.visibility = View.GONE
                } else {
                    binding.emptyDataConst.visibility = View.GONE
                    binding.profilesRecyclerview.visibility = View.VISIBLE
                    // update adapter...
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}