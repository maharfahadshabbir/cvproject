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
import com.example.cvmaker.databinding.FragmentSkillsDetailsBinding
import com.example.cvmaker.fragments.profiledetail.adapters.SkillAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.previewCv
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.profilemodels.OtherSkill
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import kotlin.getValue


class SkillsDetailsFragment : Fragment() {


    private lateinit var binding: FragmentSkillsDetailsBinding

    private var adapter: SkillAdapter? = null

    private val sharedViewModel by activityViewModels<SharedViewModel>()


    private var onBackPressedCallback: OnBackPressedCallback? = null

    private var isSkillNameAnalyticSent = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSkillsDetailsBinding.inflate(layoutInflater)
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
//        val params = binding.scrollvieww.layoutParams as ViewGroup.MarginLayoutParams
//        params.set(
//            params.leftMargin,
//            params.topMargin,
//            params.rightMargin,
//            keyboardHeight-25 // bottom margin = keyboard height
//        )
        binding.scrollSkills.setPadding(
            binding.scrollSkills.paddingLeft,
            binding.scrollSkills.paddingTop,
            binding.scrollSkills.paddingRight,
            keyboardHeight
        )
    }

    private fun onKeyboardHidden() {
//        val params = binding.scrollvieww.layoutParams as ViewGroup.MarginLayoutParams
//        params.setMargins(
//            params.leftMargin,
//            params.topMargin,
//            params.rightMargin,
//            0 // reset bottom margin
//        )
//        binding.scrollvieww.layoutParams = params
//        binding.scrollvieww.setPadding(
//            binding.scrollvieww.paddingLeft,
//            binding.scrollvieww.paddingTop,
//            binding.scrollvieww.paddingRight,
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
        tryCatch {
            adapterListener()
            populateData()
            clickListener()
            binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
        }
    }


    private fun clickListener() {
        binding.backButton.setOnClickListener {
            backPressed()
        }

        binding.addMoreSkill.setOnClickListener {

            addNewEducationItem()
        }

        binding.previewCv.setOnClickListener {
            // Call the extension function with the chosen destination
//            previewCv( sharedViewModel, R.id.fragmentPreviewApi)
        }


    }

    private fun populateData() {
        tryCatch {
            if (sharedViewModel.cvModel.other_skills.isNotEmpty()) {
                adapter?.submitList(sharedViewModel.cvModel.other_skills)
            } else {
                sharedViewModel.cvModel.apply {
                    this.other_skills.add(OtherSkill())
                }
                adapter?.submitList(sharedViewModel.cvModel.other_skills)
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
            if (position >= 0 && position < sharedViewModel.cvModel.other_skills.size) {
                // Create a new copy (so DiffUtil sees a new list instance)
                val updatedList = sharedViewModel.cvModel.other_skills.toMutableList()
                updatedList.removeAt(position)
                sharedViewModel.cvModel.other_skills = updatedList
                if (updatedList.isEmpty()) {

                    findNavController().popBackStack()
                    return
                }
                binding.scrollSkills.isEnabled = false
                binding.scrollSkills.clearFocus()

                adapter?.submitList(updatedList.toList())

// Re-enable scrolling after a frame
                binding.scrollSkills.post {
                    binding.scrollSkills.isEnabled = true
                }
            }// ensure a new instance each time
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun adapterListener() {
        tryCatch {
            context?.let { ctx ->
                binding.skillRecyclerView.layoutManager = LinearLayoutManager(ctx)
                adapter = SkillAdapter()
                binding.skillRecyclerView.adapter = adapter
                adapter?.setOnEditTextCompleteListener(object :
                    SkillAdapter.OnEditTextCompleteListener {
                    override fun requestFocusForNewItem(editText: EditText) {
                        editText.requestFocus()
                    }

                    override fun onSkillNameTextChange(position: Int, text: String) {
                        tryCatch {
                            if (text.isNotEmpty() && !isSkillNameAnalyticSent) {
                                activity?.let {
                                    if (it is MainActivity) {
                                        isSkillNameAnalyticSent = true
                                    }
                                }
                            }

                            if (position in 0 until sharedViewModel.cvModel.other_skills.size) {
                                sharedViewModel.cvModel.apply {
                                    this.other_skills[position].name = text
                                }
                            }
                        }
                    }

                    override fun onRatingChanged(position: Int, rating: Float) {
                        tryCatch {

                            if (position in 0 until sharedViewModel.cvModel.other_skills.size) {
                                sharedViewModel.cvModel.apply {
                                    this.other_skills[position].rating = rating.toInt()
                                }
                            }
                        }
                    }

                    override fun onItemRemoved(position: Int) {
                        showRemoveItemBottomSheet(position)
                    }
                })
            }
        }
    }

    private fun addNewEducationItem() {
        tryCatch {
            val areAllItemsNotEmpty =
                sharedViewModel.cvModel.other_skills.all { it.name.isNotEmpty() && it.rating != 0 }

            // Only add a new item if all existing items are not empty
            if (areAllItemsNotEmpty) {
                // Create a new empty Education item and add it to the list
                sharedViewModel.cvModel.apply {
                    this.other_skills.add(OtherSkill())
                }
                // Notify the adapter that the data has changed
                adapter?.submitList(sharedViewModel.cvModel.other_skills)
                // Scroll to the newly added item (optional)
                //  adapter.notifyDataSetChanged()
                binding.skillRecyclerView.scrollToPosition(sharedViewModel.cvModel.other_skills.size - 1)
            } else {
                Toast.makeText(requireContext(), "Input Fields are empty", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}