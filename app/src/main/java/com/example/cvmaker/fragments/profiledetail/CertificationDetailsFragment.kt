package com.example.cvmaker.fragments.profiledetail

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cvmaker.databinding.FragmentCertificationDetailsBinding
import com.example.cvmaker.fragments.profiledetail.adapters.CertificationAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.workingmodels.CertificationModel
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.showToastSafe
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel

class CertificationDetailsFragment : Fragment() {

    private var _binding: FragmentCertificationDetailsBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var adapter: CertificationAdapter? = null
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCertificationDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()
        setupRecycler()
        populateData()
        setupClicks()
        handleKeyboard(view)

        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
        adapter = null
        _binding = null
    }

    private fun configureBackPress() {
        tryCatch {
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }

            }
            onBackPressedCallback?.let { cb ->
                getViewLifecycleOwnerOrNull()?.let { owner ->
                    activity?.onBackPressedDispatcher?.addCallback(owner, cb)
                }
            }
        }
    }

    private fun setupRecycler() {
        adapter = CertificationAdapter()
        binding.certificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.certificationRecyclerView.adapter = adapter

        adapter?.setListener(object : CertificationAdapter.Listener {
            override fun onCourseChanged(position: Int, value: String) {
                val list = sharedViewModel.cvModelRequestDb.certificationList
                if (position in list.indices) list[position].course = value
            }

            override fun onInstituteChanged(position: Int, value: String) {
                val list = sharedViewModel.cvModelRequestDb.certificationList
                if (position in list.indices) list[position].institute = value
            }

            override fun onGradeChanged(position: Int, value: String) {
                val list = sharedViewModel.cvModelRequestDb.certificationList
                if (position in list.indices) list[position].grade = value
            }

            override fun onStartDateChanged(position: Int, value: String) {
                val list = sharedViewModel.cvModelRequestDb.certificationList
                if (position in list.indices) list[position].startDate = value
            }

            override fun onEndDateChanged(position: Int, value: String) {
                val list = sharedViewModel.cvModelRequestDb.certificationList
                if (position in list.indices) list[position].endDate = value
            }

            override fun onCurrentStudentToggled(position: Int, isChecked: Boolean) {
                val list = sharedViewModel.cvModelRequestDb.certificationList
                if (position in list.indices) {
                    list[position].isCurrentStudent = isChecked
                    if (isChecked) list[position].endDate = "" // clear end date when current
                }
            }

            override fun onRemove(position: Int) {
                showRemoveItemBottomSheet(position)
            }

            override fun requestFocusForNewItem(editText: EditText) {
                editText.requestFocus()
            }

            override fun collapseAllExcept(position: Int) {
                adapter?.expandOnly(position)
            }
        })
    }

    private fun populateData() {
        val list = sharedViewModel.cvModelRequestDb.certificationList
        if (list.isEmpty()) {
            list.add(
                CertificationModel(
                    course = "",
                    institute = "",
                    grade = "",
                    startDate = "",
                    endDate = "",
                    isCurrentStudent = false,
                    expanded = true
                )
            )
        }
        adapter?.submitList(list.toList())
        adapter?.expandOnly(list.lastIndex)
    }

    private fun setupClicks() {
        binding.backButton.setOnClickListener { findNavController().popBackStack() }

        binding.addMoreCert.setOnClickListener {
            val list = sharedViewModel.cvModelRequestDb.certificationList

            // Ensure current rows are minimally filled before adding new
            val allFilled = list.all { !(it.course.isNullOrBlank()) && !(it.institute.isNullOrBlank()) }
            if (!allFilled) {
                Toast.makeText(requireContext(), "Please fill the current certification first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add and expand the new row
            list.add(
                CertificationModel(
                    course = "",
                    institute = "",
                    grade = "",
                    startDate = "",
                    endDate = "",
                    isCurrentStudent = false,
                    expanded = true
                )
            )
            adapter?.submitList(list.toList())
            val newIndex = list.lastIndex
            binding.certificationRecyclerView.scrollToPosition(newIndex)
            adapter?.expandOnly(newIndex)
        }

        binding.btnSave.setOnClickListener {
            val list = sharedViewModel.cvModelRequestDb.certificationList

            // Clean up and drop blank rows
            val cleaned = list
                .map {
                    it.copy(
                        course = it.course?.trim(),
                        institute = it.institute?.trim(),
                        grade = it.grade?.trim(),
                        startDate = it.startDate?.trim(),
                        endDate = (if (it.isCurrentStudent) "" else it.endDate?.trim()),
                        expanded = false
                    )
                }
                .filter { !(it.course.isNullOrBlank() && it.institute.isNullOrBlank() && it.grade.isNullOrBlank()
                        && it.startDate.isNullOrBlank() && it.endDate.isNullOrBlank()) }
                .toMutableList()

            if (cleaned.isEmpty()) {
                Toast.makeText(requireContext(), "Please add at least one certification", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedViewModel.cvModelRequestDb.certificationList = cleaned
            showToastSafe("Certifications saved successfully!")
            findNavController().popBackStack()
        }
    }

    private fun showRemoveItemBottomSheet(position: Int) {
        val bottomSheet = com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet {
            removeItem(position)
        }
        bottomSheet.show(parentFragmentManager, "RemoveCertificationBottomSheet")
    }

    private fun removeItem(position: Int) {
        try {
            val list = sharedViewModel.cvModelRequestDb.certificationList
            if (position in list.indices) {
                val updated = list.toMutableList()
                updated.removeAt(position)

                if (updated.isEmpty()) {
                    // Keep one empty row so user isn’t stuck
                    updated.add(
                        CertificationModel(
                            course = "",
                            institute = "",
                            grade = "",
                            startDate = "",
                            endDate = "",
                            isCurrentStudent = false,
                            expanded = true
                        )
                    )
                }

                sharedViewModel.cvModelRequestDb.certificationList = updated
                adapter?.submitList(updated.toList())
                adapter?.expandOnly(updated.lastIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleKeyboard(root: View) {
        root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            root.getWindowVisibleDisplayFrame(r)
            val screenHeight = root.rootView.height
            val keypadHeight = screenHeight - r.bottom

            binding.scrollCertification.setPadding(
                binding.scrollCertification.paddingLeft,
                binding.scrollCertification.paddingTop,
                binding.scrollCertification.paddingRight,
                if (keypadHeight > screenHeight * 0.15) keypadHeight else 0
            )
        }
    }
}
