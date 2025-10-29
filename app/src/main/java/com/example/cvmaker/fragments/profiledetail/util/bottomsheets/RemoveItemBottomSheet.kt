package com.example.cvmaker.fragments.profiledetail.util.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cvmaker.R
import com.example.cvmaker.databinding.RemoveItemBottomsheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RemoveItemBottomSheet(val removeItemCallback: () -> Unit) : BottomSheetDialogFragment() {
    var _binding: RemoveItemBottomsheetBinding? = null
    val binding get() = _binding!!

    override fun getTheme(): Int = R.style.RoundedBottomSheetShape
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = RemoveItemBottomsheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            btnCancel.setOnClickListener {
                dismiss()
            }
            btnRemove.setOnClickListener {
                removeItemCallback.invoke()
                dismiss()
            }
        }

    }

}