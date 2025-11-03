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
import com.example.cvmaker.R
import com.example.cvmaker.databinding.FragmentProjectDetailsBinding
import com.example.cvmaker.fragments.profiledetail.adapters.ProjectAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.workingmodels.ProjectModel
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel

class ProjectDetailsFragment : Fragment() {

    private lateinit var binding: FragmentProjectDetailsBinding
    private var adapter: ProjectAdapter? = null
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProjectDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureBackPress()
        initUi()
        initAdapter()
        populateFromDb()
        initClicks()

        // keyboard padding
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * 0.15) {
                binding.scrollView.setPadding(
                    binding.scrollView.paddingLeft,
                    binding.scrollView.paddingTop,
                    binding.scrollView.paddingRight,
                    keypadHeight
                )
            } else {
                // keep at 0 – your existing code already handles reset if needed
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    /* -------------------- UI + Adapter -------------------- */

    private fun initUi() = tryCatch {
        binding.projectRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    private fun initAdapter() = tryCatch {
        adapter = ProjectAdapter().also { binding.projectRecyclerView.adapter = it }

        adapter?.setOnEditTextCompleteListener(object : ProjectAdapter.OnEditTextCompleteListener {
            override fun onProjectTitleTextChange(position: Int, text: String) {
                updateModel(position) { it.projectTitle = text.ifBlank { null } }
            }
            override fun onProjectDescriptionChange(position: Int, text: String) {
                updateModel(position) { it.description = text.ifBlank { null } }
            }
            override fun onProjectLinkChange(position: Int, text: String) {
                updateModel(position) { it.link = text.ifBlank { null } }
            }
            override fun requestNewFocus(editText: EditText) {
                editText.requestFocus()
            }
            override fun onRemoveItem(position: Int) {
                showRemoveItemBottomSheet(position)
            }
        })
    }

    /* -------------------- Data flow -------------------- */

    private fun populateFromDb() = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.projectList
        if (db.isEmpty()) db.add(ProjectModel())
        adapter?.submitList(db.toList())
        adapter?.expandOnly(db.lastIndex.coerceAtLeast(0))
    }

    private inline fun updateModel(index: Int, update: (ProjectModel) -> Unit) {
        val db = sharedViewModel.cvModelRequestDb.projectList
        if (index in db.indices) update(db[index])
    }

    /* -------------------- Clicks -------------------- */

    private fun initClicks() = with(binding) {
        backButton.setOnClickListener { backPressed() }

        addMoreLayout.setOnClickListener { addNewProjectItem() }

        previewCv.setOnClickListener {
            // hook your preview if needed
        }

        expnextbtn.setOnClickListener {
            if (!validateFields()) {
                Toast.makeText(
                    requireContext(),
                    "Please fill all fields before proceeding.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.createProfileFragment)
        }
    }

    /* -------------------- Add / Remove -------------------- */

    private fun addNewProjectItem() = tryCatch {
        if (validateFields()) {
            val db = sharedViewModel.cvModelRequestDb.projectList
            db.add(ProjectModel())
            adapter?.submitList(db.toList())
            val newIndex = db.lastIndex
            binding.projectRecyclerView.scrollToPosition(newIndex)
            adapter?.expandOnly(newIndex) // close previous, open new
        } else {
            Toast.makeText(requireContext(), "Input fields are empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRemoveItemBottomSheet(position: Int) {
        RemoveItemBottomSheet { removeItemFromList(position) }
            .show(parentFragmentManager, "RemoveProjectItem")
    }

    private fun removeItemFromList(position: Int) = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.projectList
        if (position !in db.indices) return@tryCatch

        db.removeAt(position)

        if (db.isEmpty()) {
            findNavController().popBackStack()
            return@tryCatch
        }

        binding.scrollView.isEnabled = false
        binding.scrollView.clearFocus()

        adapter?.submitList(db.toList())
        val expandIndex = position.coerceAtMost(db.lastIndex)
        adapter?.expandOnly(expandIndex)

        binding.scrollView.post { binding.scrollView.isEnabled = true }
    }

    /* -------------------- Validation -------------------- */

    private fun validateFields(): Boolean {
        val list = sharedViewModel.cvModelRequestDb.projectList
        val ok = list.all { m ->
            val title = m.projectTitle?.trim().orEmpty()
            val desc = m.description?.trim().orEmpty()
            title.isNotEmpty() && desc.isNotEmpty()
            // link is optional; if you want to require a valid URL, add a regex check here
        }
        return ok
    }

    /* -------------------- Navigation / Back -------------------- */

    private fun backPressed() {
        removeIncompleteItems()
        findNavController().popBackStack()
    }

    private fun configureBackPress() = tryCatch {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = backPressed()
        }
        onBackPressedCallback?.let { cb ->
            getViewLifecycleOwnerOrNull()?.let { owner ->
                activity?.onBackPressedDispatcher?.addCallback(owner, cb)
            }
        }
    }

    private fun removeIncompleteItems() = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.projectList
        if (db.isEmpty()) return@tryCatch

        val complete = db.filter { isComplete(it) }.toMutableList()
        val partialCount = db.count { !isEmpty(it) && !isComplete(it) }

        sharedViewModel.cvModelRequestDb.projectList = complete

        adapter?.submitList(complete.toList())
        if (partialCount > 0) {
            Toast.makeText(
                requireContext(),
                "Removed $partialCount incomplete project item(s).",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (complete.isNotEmpty()) adapter?.expandOnly(complete.lastIndex)
    }

    private fun isComplete(m: ProjectModel): Boolean {
        val title = m.projectTitle?.trim().orEmpty()
        val desc = m.description?.trim().orEmpty()
        return title.isNotEmpty() && desc.isNotEmpty()
    }

    private fun isEmpty(m: ProjectModel): Boolean {
        return (m.projectTitle.isNullOrBlank()
                && m.description.isNullOrBlank()
                && m.link.isNullOrBlank())
    }
}
