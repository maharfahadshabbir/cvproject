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
import com.example.cvmaker.databinding.FragmentInterestDetailBinding
import com.example.cvmaker.fragments.profiledetail.adapters.InterestAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.workingmodels.InterestModel
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.showToastSafe
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel

class InterestDetailFragment : Fragment() {

    private lateinit var binding: FragmentInterestDetailBinding
    private var adapter: InterestAdapter? = null
    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentInterestDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()
        initListener()
        handleKeyboard(view)
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
                    findNavController().popBackStack()
                }

            }
            onBackPressedCallback?.let {
                getViewLifecycleOwnerOrNull()?.let { owner ->
                    activity?.onBackPressedDispatcher?.addCallback(owner, it)
                }
            }
        }
    }

    private fun initListener() {
        setupRecycler()
        clickListener()
        populateData()
        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    private fun setupRecycler() {
        context?.let { ctx ->
            adapter = InterestAdapter()
            adapter?.setSharedViewModel(sharedViewModel)
            binding.interestRecyclerView.layoutManager = LinearLayoutManager(ctx)
            binding.interestRecyclerView.adapter = adapter

            adapter?.setOnEditTextCompleteListener(object : InterestAdapter.OnEditTextCompleteListener {
                override fun onInterestTextChange(position: Int, text: String) {
                    if (position in sharedViewModel.cvModelRequestDb.interestList.indices) {
                        sharedViewModel.cvModelRequestDb.interestList[position].interestName = text
                    }
                }

                override fun requestFocusForNewItem(editText: EditText) {
                    editText.requestFocus()
                }

                override fun onRemoveItem(position: Int) {
                    showRemoveItemBottomSheet(position)
                }
            })
        }
    }

    private fun populateData() {
        tryCatch {
            val list = sharedViewModel.cvModelRequestDb.interestList
            if (list.isEmpty()) {
                list.add(InterestModel())
            }
            adapter?.submitList(list.toList()) // force DiffUtil update
        }
    }

    private fun clickListener() {
        binding.backButton.setOnClickListener { findNavController().popBackStack() }

        binding.addMoreInterest.setOnClickListener { addNewInterestItem() }

        binding.btnSave.setOnClickListener {
            val valid = sharedViewModel.cvModelRequestDb.interestList
                .any { it.interestName?.isNotBlank() == true }

            if (!valid) {
                Toast.makeText(context, "Please add at least one valid interest", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Save to in-memory DB (CvModelRequestDb)
            val cleanList = sharedViewModel.cvModelRequestDb.interestList
                .filter { !it.interestName.isNullOrBlank() }
                .map { InterestModel(it.interestName!!.trim()) }
                .toMutableList()

            sharedViewModel.cvModelRequestDb.interestList = cleanList

            showToastSafe("Interests saved successfully!")
            findNavController().popBackStack()
        }
    }

    private fun addNewInterestItem() {
        tryCatch {
            val list = sharedViewModel.cvModelRequestDb.interestList
            val allFilled = list.all { it.interestName?.isNotBlank() == true }

            if (allFilled) {
                list.add(InterestModel())
                adapter?.submitList(list.toList())
                binding.interestRecyclerView.scrollToPosition(list.size - 1)
            } else {
                Toast.makeText(context, "Please fill existing interest first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRemoveItemBottomSheet(position: Int) {
        val removeSheet = RemoveItemBottomSheet {
            removeItemFromList(position)
        }
        removeSheet.show(parentFragmentManager, "RemoveItemBottomSheet")
    }

    private fun removeItemFromList(position: Int) {
        try {
            val list = sharedViewModel.cvModelRequestDb.interestList
            if (position in list.indices) {
                val updated = list.toMutableList()
                updated.removeAt(position)
                sharedViewModel.cvModelRequestDb.interestList = updated
                adapter?.submitList(updated.toList())
                if (updated.isEmpty()) findNavController().popBackStack()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleKeyboard(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            binding.scrollInterest.setPadding(
                binding.scrollInterest.paddingLeft,
                binding.scrollInterest.paddingTop,
                binding.scrollInterest.paddingRight,
                if (keypadHeight > screenHeight * 0.15) keypadHeight else 0
            )
        }
    }
}
