package com.example.cvmaker.fragments.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cvmaker.R
import com.example.cvmaker.adaptor.NewProfileAdapter
import com.example.cvmaker.databinding.FragmentNewDraftBinding
import com.example.cvmaker.databinding.ProfileDetailBottomSheetBinding
import com.example.cvmaker.fragments.profile.NewProfileFragment.Companion.draftItems
import com.example.cvmaker.model.workingmodels.CvModelRequestDb
import com.example.cvmaker.model.workingmodels.CvProfileItem
import com.example.cvmaker.viewmodels.NewProfileViewModel
import com.example.cvmaker.viewmodels.SharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewDraftFragment : Fragment() {

    private var _binding: FragmentNewDraftBinding? = null
    private val binding get() = _binding!!

    private val newProfileViewModel by activityViewModels<NewProfileViewModel>()
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private val draftAdapter by lazy { NewProfileAdapter() }  // use your adapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewDraftBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profilesRecyclerview.adapter = draftAdapter
        binding.profilesRecyclerview.setHasFixedSize(true)

        observeDrafts()

        draftAdapter.setOnClickListener(object : NewProfileAdapter.OnClickListener {
            override fun onPopMenu(position: Int, item: CvProfileItem) {
                showDraftBottomSheet(position, item)
            }

            override fun onItemClick(position: Int, item: CvProfileItem) {
                editDraftProfile(item)
            }

            override fun onClick(v: View?) {
                // Not used
            }
        })



    }

    private fun showDraftBottomSheet(position: Int, item: CvProfileItem) {
        context?.let { ctx ->
            val sheetBinding = ProfileDetailBottomSheetBinding.inflate(layoutInflater)
            val dialog = BottomSheetDialog(ctx, R.style.MyBottomSheetDialog)
            dialog.setContentView(sheetBinding.root)

            val name = item.data.personalDetails?.name ?: "Untitled Draft"
            sheetBinding.text.text = name

            sheetBinding.editTxt.setOnClickListener {
                dialog.dismiss()
                editDraftProfile(item)
            }

            sheetBinding.deleteTxt.setOnClickListener {
                dialog.dismiss()
                deleteDraftProfile(item)
            }

            dialog.show()
        }
    }


    private fun deleteDraftProfile(item: CvProfileItem) {
        lifecycleScope.launch {
            try {
                newProfileViewModel.deleteProfileById(item.id)
                Toast.makeText(requireContext(), "Draft deleted", Toast.LENGTH_SHORT).show()
                // Remove from local list and update adapter
                draftItems.remove(item)
                draftAdapter.setData(draftItems)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    private fun editDraftProfile(item: CvProfileItem) {
        sharedViewModel.cvModelRequestDb = item.data
        sharedViewModel.editingProfileId = item.id
        sharedViewModel.profileCase = "updateProfile"  // make sure profileOrDraft = true on save
        findNavController().navigate(R.id.createProfileFragment)
    }

    private fun observeDrafts() {
        Log.d("DraftFragment", "observeDrafts(): Started")

        if (draftItems.isEmpty()) {
            Log.d("DraftFragment", "No draft items → show empty state")

            binding.emptyDataConst.visibility = View.VISIBLE
            binding.profilesRecyclerview.visibility = View.GONE
        } else {
            Log.d("DraftFragment", "Showing ${draftItems.size} draft items")

            binding.emptyDataConst.visibility = View.GONE
            binding.profilesRecyclerview.visibility = View.VISIBLE

            draftAdapter.setData(draftItems)
        }

        Log.v("DraftFragment", "UI Updated with drafts=${draftItems.size}")


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
