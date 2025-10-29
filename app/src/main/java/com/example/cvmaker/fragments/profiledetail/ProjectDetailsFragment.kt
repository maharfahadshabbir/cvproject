package com.example.cvmaker.fragments.profiledetail

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cvmaker.R
import com.example.cvmaker.activities.MainActivity
import com.example.cvmaker.databinding.FragmentProjectDetailsBinding
import com.example.cvmaker.fragments.profiledetail.adapters.ProjectAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.previewCv
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.profilemodels.Project
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.getValue


class ProjectDetailsFragment : Fragment() {


    private lateinit var binding: FragmentProjectDetailsBinding

    private var adapter: ProjectAdapter? = null

    private val sharedViewModel by activityViewModels<SharedViewModel>()


    private var onBackPressedCallback: OnBackPressedCallback? = null

    private var coroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProjectDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun backPressed() {
       findNavController().popBackStack()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureBackPress()

        initListener()

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * 0.15) {
                onKeyboardShown(keypadHeight)
            } else {
                onKeyboardHidden()
            }
        }

    }


    private fun onKeyboardShown(keyboardHeight: Int) {
//        val params = binding.scrollView.layoutParams as ViewGroup.MarginLayoutParams
//        params.set(
//            params.leftMargin,
//            params.topMargin,
//            params.rightMargin,
//            keyboardHeight-25 // bottom margin = keyboard height
//        )
        binding.scrollView.setPadding(
            binding.scrollView.paddingLeft,
            binding.scrollView.paddingTop,
            binding.scrollView.paddingRight,
            keyboardHeight
        )
    }

    private fun onKeyboardHidden() {
//        val params = binding.scrollView.layoutParams as ViewGroup.MarginLayoutParams
//        params.setMargins(
//            params.leftMargin,
//            params.topMargin,
//            params.rightMargin,
//            0 // reset bottom margin
//        )
//        binding.scrollView.layoutParams = params
//        binding.scrollView.setPadding(
//            binding.scrollView.paddingLeft,
//            binding.scrollView.paddingTop,
//            binding.scrollView.paddingRight,
//            0
//        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    private fun configureBackPress() {
        tryCatch {
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            }
            onBackPressedCallback?.let {
                getViewLifecycleOwnerOrNull()?.let { it1 ->
                    activity?.onBackPressedDispatcher?.addCallback(
                        it1,
                        it
                    )
                }
            }
        }
    }

    private fun initListener() {
        adapterListener()
        populateData()
        clickListener()
        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    private fun populateData() {
        tryCatch {
            if (sharedViewModel.cvModel.projects.isNotEmpty()) {
                adapter?.submitList(sharedViewModel.cvModel.projects)
            } else {
                sharedViewModel.cvModel.apply {
                    this.projects.add(Project())
                }
                adapter?.submitList(sharedViewModel.cvModel.projects)
            }
        }
    }

    private fun clickListener() {

        binding.backButton.setOnClickListener {
            backPressed()
        }
        binding.addMoreLayout.setOnClickListener {

            addNewEducationItem()
        }


        binding.previewCv.setOnClickListener {
//            previewCv( sharedViewModel, R.id.fragmentPreviewApi)
        }
        binding.expnextbtn.setOnClickListener {
            if (validateFields()) {
                saveAndProceed()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill all fields before proceeding.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun adapterListener() {
        tryCatch {
            context?.let { ctx ->
                binding.projectRecyclerView.layoutManager = LinearLayoutManager(ctx)
                adapter = ProjectAdapter()
                binding.projectRecyclerView.adapter = adapter

                adapter?.setOnEditTextCompleteListener(object :
                    ProjectAdapter.OnEditTextCompleteListener {
                    override fun onProjectTitleTextChange(position: Int, text: String) {

                        if (position in 0 until sharedViewModel.cvModel.projects.size) {
                            sharedViewModel.cvModel.apply {
                                this.projects[position].title = text

                            }
                        }
                    }

                    override fun onProjectDescriptionChange(position: Int, text: String) {

                        if (position in 0 until sharedViewModel.cvModel.projects.size) {
                            sharedViewModel.cvModel.apply {
                                this.projects[position].description = text
                            }
                        }
                    }

                    override fun requestNewFocus(editText: EditText) {
                        editText.requestFocus()
                    }

                    override fun OnRemoveItem(position: Int) {
                        showRemoveItemBottomSheet(position)
                    }
                })
            }
        }
    }

    private fun showRemoveItemBottomSheet(position: Int) {
        val removeItemBottomSheet = RemoveItemBottomSheet() {
            removeItemFromList(position)
        }
        removeItemBottomSheet.show(parentFragmentManager, "RemoveItemBottomSheet")
    }

    private fun removeItemFromList(position: Int) {
        try {
            if (position >= 0 && position < sharedViewModel.cvModel.projects.size) {
                // Create a new copy (so DiffUtil sees a new list instance)
                val updatedList = sharedViewModel.cvModel.projects.toMutableList()
                updatedList.removeAt(position)
                sharedViewModel.cvModel.projects = updatedList
                if (updatedList.isEmpty()) {
                    findNavController().popBackStack()
                    return
                }
                binding.scrollView.isEnabled = false
                binding.scrollView.clearFocus()

                adapter?.submitList(updatedList.toList())

// Re-enable scrolling after a frame
                binding.scrollView.post {
                    binding.scrollView.isEnabled = true
                }
            }
        }catch (ex: Exception){
            ex.printStackTrace()
        }
    }

    private fun validateFields(): Boolean {
        // Check if all project items have non-empty title and description
        return sharedViewModel.cvModel.projects.all { it.title.isNotEmpty() && it.description.isNotEmpty() }
    }

    private fun saveAndProceed() {
        findNavController().navigate(R.id.createProfileFragment)
    }

    private fun addNewEducationItem() {
        tryCatch {
            // Only add a new item if all existing items are not empty
            if (validateFields()) {
                sharedViewModel.cvModel.apply {
                    this.projects.add(Project())
                }
                adapter?.submitList(sharedViewModel.cvModel.projects)

                // Scroll to the newly added item at the bottom
                try {
                    binding.projectRecyclerView.scrollToPosition(sharedViewModel.cvModel.projects.size - 1)
                }catch (ex:Exception){}
            } else {
                Toast.makeText(requireContext(), "Input Fields are empty", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}