package com.example.cvmaker.fragments.profiledetail

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cvmaker.R
import com.example.cvmaker.activities.MainActivity
import com.example.cvmaker.databinding.FragmentReferencesDetailBinding
import com.example.cvmaker.fragments.profiledetail.adapters.ReferencesAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.previewCv
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.profilemodels.Reference
import com.example.cvmaker.utils.showToastSafe
import com.example.cvmaker.viewmodels.SharedViewModel
import kotlin.getValue


class ReferencesDetailFragment : Fragment() {


    private lateinit var binding: FragmentReferencesDetailBinding


    companion object{
        val OnItemRemoved:(() -> Unit)? = null
    }

    private lateinit var adapter: ReferencesAdapter
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReferencesDetailBinding.inflate(layoutInflater)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
        return binding.root

    }

    private fun backPressed() {

        findNavController().popBackStack()
//        sharedViewModel.cvModel.references.clear()
//        adapter.submitList(sharedViewModel.cvModel.references)
//        adapter.notifyDataSetChanged()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListner()

        binding.apply {


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
    }

    private fun onKeyboardShown(keyboardHeight: Int) {
//        val params = binding.scrollReferences.layoutParams as ViewGroup.MarginLayoutParams
//        params.set(
//            params.leftMargin,
//            params.topMargin,
//            params.rightMargin,
//            keyboardHeight-25 // bottom margin = keyboard height
//        )
        binding.scrollReferences.setPadding(
            binding.scrollReferences.paddingLeft,
            binding.scrollReferences.paddingTop,
            binding.scrollReferences.paddingRight,
            keyboardHeight
        )
    }

    private fun onKeyboardHidden() {
//        val params = binding.scrollReferences.layoutParams as ViewGroup.MarginLayoutParams
//        params.setMargins(
//            params.leftMargin,
//            params.topMargin,
//            params.rightMargin,
//            0 // reset bottom margin
//        )
//        binding.scrollReferences.layoutParams = params
//        binding.scrollReferences.setPadding(
//            binding.scrollReferences.paddingLeft,
//            binding.scrollReferences.paddingTop,
//            binding.scrollReferences.paddingRight,
//            0
//        )
    }

    private fun initListner() {
        clickListener()
        adapterListener()
        populateData()
        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    private fun populateData() {
        if (sharedViewModel.cvModel.references.isNotEmpty()) {
            adapter.submitList(sharedViewModel.cvModel.references)
        } else {
            sharedViewModel.cvModel.apply {
                this?.references?.add(Reference())
            }
            adapter.submitList(sharedViewModel.cvModel.references)

        }
    }

    private fun adapterListener() {
        context?.let { ctx ->
            binding.referencesRecyclerView.layoutManager = LinearLayoutManager(ctx)
            binding.referencesRecyclerView.itemAnimator = null
            binding.referencesRecyclerView.setHasFixedSize(true)
            adapter = ReferencesAdapter()
            binding.referencesRecyclerView.adapter = adapter
            adapter.setOnEditTextCompleteListener(object :
                ReferencesAdapter.OnEditTextCompleteListener {
                override fun onReferenceNameTextChange(position: Int, text: String) {
                    // Update the sharedViewModel data directly


                    if (position in 0 until sharedViewModel.cvModel.references.size) {

                        sharedViewModel.cvModel.apply {
                            this?.references?.get(position)?.name = text

                        }

                    }
                }

                override fun onJobTitleTextChange(position: Int, text: String) {
                    // Update the sharedViewModel data directly


                    if (position in 0 until sharedViewModel.cvModel.references.size) {

                        sharedViewModel.cvModel.apply {
                            this?.references?.get(position)?.designation = text

                        }

                    }
                }

                override fun onCompanyNameTextChange(position: Int, text: String) {
                    // Update the sharedViewModel data directly

                    if (position in 0 until sharedViewModel.cvModel.references.size) {

                        sharedViewModel.cvModel.apply {
                            this?.references?.get(position)?.company_name = text

                        }

                    }
                }

//            override fun onEmailTextChange(position: Int, text: String) {
//                // Update the sharedViewModel data directly
//                if (position in 0 until sharedViewModel.cvModel.references.size) {
//
//
//
//                    sharedViewModel.cvModel.apply {
//                        this?.references?.get(position)?.email = text
//
//                    }
//
//                }
//            }

                override fun onEmailTextChange(position: Int, text: String) {
                    // Check if the email already exists in other references

                    val isDuplicateEmail = sharedViewModel.cvModel.references
                        .filterIndexed { index, _ -> index != position }
                        .any { it.email == text }

                    if (isDuplicateEmail) {
                        ViewUtils.error = getString(R.string.dublicate_email_founded)
                        // Show a toast message if the email is a duplicate
                        showToastSafe(getString(R.string.dublicate_email_founded))
                    } else {
                        ViewUtils.error = ""
                        // Update the sharedViewModel data if no duplicate email is found
                        if (position in 0 until sharedViewModel.cvModel.references.size) {
                            sharedViewModel.cvModel.apply {
                                this.references[position].email = text
                            }
                        }
                    }


                }


                override fun onPhoneTextChange(position: Int, text: String) {
                    // Update the sharedViewModel data directly



                    if (position in 0 until sharedViewModel.cvModel.references.size) {

                        sharedViewModel.cvModel.apply {
                            references?.get(position)?.phone = text
                        }
                    }
                }
                override fun requestFocus(editText: EditText) {
                    editText.requestFocus()
                }

                override fun onRemoveItem(position: Int, itemRemoved: () -> Unit) {
                    showRemoveItemBottomSheet(position){
                        itemRemoved.invoke()
                    }
                }

            })
        }

    }
    private fun showRemoveItemBottomSheet(position: Int, itemRemoved: (Boolean) -> Unit) {
        val removeItemBottomSheet = RemoveItemBottomSheet {
            itemRemoved.invoke(true)
            removeItemFromList(position)
        }
        removeItemBottomSheet.show(parentFragmentManager, "RemoveItemBottomSheet")
    }

    private fun removeItemFromList(position: Int) {
        try {
            if (position >= 0 && position < sharedViewModel.cvModel.references.size) {
                // Create a new copy (so DiffUtil sees a new list instance)
                val updatedList = sharedViewModel.cvModel.references.toMutableList()
                updatedList.removeAt(position)
                sharedViewModel.cvModel.references = updatedList
                if (updatedList.isEmpty()) {
                    findNavController().popBackStack()
                    return
                }
                Log.i("currentList", "removeItemFromList:${updatedList.size} ")
                // Update ViewModel and Adapter with a *new* list reference
                binding.scrollReferences.isEnabled = false
                binding.scrollReferences.clearFocus()

                adapter?.submitList(updatedList.toList())

// Re-enable scrolling after a frame
                binding.scrollReferences.post {
                    binding.scrollReferences.isEnabled = true
                }

                // ensure a new instance each time
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun clickListener() {

        binding.backButton.setOnClickListener {
            backPressed()
        }
        binding.previewCv.setOnClickListener {
//            previewCv( sharedViewModel, R.id.fragmentPreviewApi)
        }
        binding.addMoreReferences.setOnClickListener {

            addNewEducationItem()
        }
        binding.expnextbtn.setOnClickListener {

            val arePhoneEmailItemsNotEmpty =
                sharedViewModel.cvModel.references.all {
                    it.email.isNotEmpty() &&
                            it.phone.isNotEmpty()
                }
            if (arePhoneEmailItemsNotEmpty) {
                activity?.let {
                    if (it is MainActivity) {
//                        InterstitialHelper.showAndLoadInterstitial(
//                            it,
//                            RemoteConfig.CREATE_AI_CV_INTERSTITIAL_ID
//                        ) {
//
//                            findNavController().navigate(R.id.customHomeFragment)
//
//                        }
                    }
                }

            } else {
                showToastSafe("please enter at least phone and email")
            }

        }

    }

    private fun validateAndAddNewReference() {

        val hasDuplicateEmails = hasDuplicateEmailsInReferences()
        if (!hasDuplicateEmails) {
            addNewReferenceItemToList()
        } else {
            showToastSafe("Duplicate email found in references")
        }

    }

    private fun hasDuplicateEmailsInReferences(): Boolean {
        val emailSet = sharedViewModel.cvModel.references.map { it.email }.toSet()
        return emailSet.size != sharedViewModel.cvModel.references.size
    }

    private fun addNewReferenceItemToList() {
        sharedViewModel.cvModel.apply {
            this.references.add(Reference())
        }
        adapter.submitList(sharedViewModel.cvModel.references)
        binding.referencesRecyclerView.scrollToPosition(sharedViewModel.cvModel.references.size - 1)
    }

    private fun addNewEducationItem() {
        validateAndAddNewReference()
    }

}