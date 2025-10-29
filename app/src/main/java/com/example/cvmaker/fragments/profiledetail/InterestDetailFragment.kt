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
import com.example.cvmaker.databinding.FragmentInterestDetailBinding
import com.example.cvmaker.fragments.profiledetail.adapters.InterestAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.previewCv
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.profilemodels.Interest
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import kotlin.getValue


class InterestDetailFragment : Fragment() {


    private lateinit var binding: FragmentInterestDetailBinding

    private var adapter: InterestAdapter? = null

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInterestDetailBinding.inflate(layoutInflater)
        return binding.root
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
//        val params = binding.scrollInterest.layoutParams as ViewGroup.MarginLayoutParams
//        params.set(
//            params.leftMargin,
//            params.topMargin,
//            params.rightMargin,
//            keyboardHeight-25 // bottom margin = keyboard height
//        )
        binding.scrollInterest.setPadding(
            binding.scrollInterest.paddingLeft,
            binding.scrollInterest.paddingTop,
            binding.scrollInterest.paddingRight,
            keyboardHeight
        )
    }

    private fun onKeyboardHidden() {
//        val params = binding.scrollInterest.layoutParams as ViewGroup.MarginLayoutParams
//        params.setMargins(
//            params.leftMargin,
//            params.topMargin,
//            params.rightMargin,
//            0 // reset bottom margin
//        )
//        binding.scrollInterest.layoutParams = params
//        binding.scrollInterest.setPadding(
//            binding.scrollInterest.paddingLeft,
//            binding.scrollInterest.paddingTop,
//            binding.scrollInterest.paddingRight,
//            0
//        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    private fun backPressed() {
        findNavController().popBackStack()
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
        clickListener()
        populateData()
        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    private fun populateData() {
        tryCatch {
            if (sharedViewModel.cvModel.interests.isNotEmpty()) {
                adapter?.submitList(sharedViewModel.cvModel.interests)
            } else {
                sharedViewModel.cvModel.apply {
                    this.interests.add(Interest())
                }
                adapter?.submitList(sharedViewModel.cvModel.interests)
            }
        }
    }

    private fun clickListener() {
        binding.backButton.setOnClickListener {
            backPressed()
        }
        binding.previewCv.setOnClickListener {
//            previewCv( sharedViewModel, R.id.fragmentPreviewApi)
        }
        binding.expnextbtn.setOnClickListener {


            activity?.let {
                if (sharedViewModel.cvModel.interests.all { interest ->
                        interest.name.isNotEmpty()
                    }) {
                    /*if (it is MainActivity) {
                        InterstitialHelper.showAndLoadInterstitial(
                            it,
                            RemoteConfig.CREATE_AI_CV_INTERSTITIAL_ID
                        ) {
                            navigateToFragment(
                                sharedNavVM = sharedNavigationViewModel,
                                targetDestinationId = R.id.customHomeFragment
                            )
                        }
                    }*/
                } else {
                    Toast.makeText(context, "Please add at least one interest", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        binding.addMoreInterest.setOnClickListener {

            addNewInterestItem()
        }

    }

    private fun adapterListener() {
        tryCatch {
            context?.let { ctx ->
                binding.interestRecyclerView.layoutManager = LinearLayoutManager(ctx)
                adapter = InterestAdapter()
                binding.interestRecyclerView.adapter = adapter

                adapter?.setOnEditTextCompleteListener(object :
                    InterestAdapter.OnEditTextCompleteListener {
                    override fun onInterestTextChange(position: Int, text: String) {
                        tryCatch {

                            if (position in 0 until sharedViewModel.cvModel.interests.size) {
                                sharedViewModel.cvModel.interests[position].name = text

                                sharedViewModel.cvModel.apply {
                                    this.interests[position].name = text

                                }
                            }
                        }
                    }

                    override fun requestFocusForNewItem(editText: EditText) {
                        editText.requestFocus()
                    }

                    override fun onRemoveItem(position: Int) {
                        showRemoveItemBottomSheet(position)
                    }
                })
                adapter?.setSharedViewModel(sharedViewModel)
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
            if (position >= 0 && position < sharedViewModel.cvModel.interests.size) {
                // Create a new copy (so DiffUtil sees a new list instance)
                val updatedList = sharedViewModel.cvModel.interests.toMutableList()
                updatedList.removeAt(position)
                sharedViewModel.cvModel.interests = updatedList
                if (updatedList.isEmpty()) {
                    findNavController().popBackStack()
                    return
                }
                // Update ViewModel and Adapter with a *new* list reference
                binding.scrollInterest.isEnabled = false
                binding.scrollInterest.clearFocus()

                adapter?.submitList(updatedList.toList())

// Re-enable scrolling after a frame
                binding.scrollInterest.post {
                    binding.scrollInterest.isEnabled = true
                }
            }// ensure a new instance each time
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addNewInterestItem() {
        tryCatch {
            // Check if all existing items are not empty
            val areAllItemsNotEmpty = sharedViewModel.cvModel.interests.all { it.name.isNotEmpty() }

            // Only add a new item if all existing items are not empty
            if (areAllItemsNotEmpty) {
                sharedViewModel.cvModel.apply {
                    this.interests.add(Interest())
                }
                adapter?.submitList(sharedViewModel.cvModel.interests)
                binding.interestRecyclerView.scrollToPosition(sharedViewModel.cvModel.interests.size - 1)
            } else {
                context?.let {
                    Toast.makeText(
                        it,
                        "Please fill in existing interest",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


}