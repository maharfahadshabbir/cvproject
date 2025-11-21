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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProjectDetailsBinding.inflate(inflater, container, false)
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
                binding.scrollView.setPadding(
                    binding.scrollView.paddingLeft,
                    binding.scrollView.paddingTop,
                    binding.scrollView.paddingRight,
                    0
                )
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

    /* -------------------- Data flow (Edit-ready) -------------------- */

    private fun populateFromDb() = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.projectList
        // Edit case: if list already has items, show them.
        // New case: ensure at least one empty row.
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

        // ✅ SAVE button: clean list, persist into cvModelRequestDb, then navigate back
        btnSave.setOnClickListener {
            saveProjects()
        }

        // This "next" button you already had – can keep or remove depending on flow
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

    /* -------------------- Save logic -------------------- */

    private fun saveProjects() = tryCatch {
        val currentList = sharedViewModel.cvModelRequestDb.projectList

        // 1) If nothing at all, just keep one empty row and warn
        if (currentList.isEmpty() || currentList.all { isEmpty(it) }) {
            sharedViewModel.cvModelRequestDb.projectList = mutableListOf(ProjectModel())
            adapter?.submitList(sharedViewModel.cvModelRequestDb.projectList.toList())
            Toast.makeText(requireContext(), "No project added yet.", Toast.LENGTH_SHORT).show()
            return@tryCatch
        }

        // 2) Keep only fully valid rows (title + description), trim values
        val cleaned = currentList
            .filter { isComplete(it) }
            .map {
                ProjectModel(
                    projectTitle = it.projectTitle?.trim(),
                    description = it.description?.trim(),
                    link = it.link?.trim()?.takeIf { link -> link.isNotEmpty() }
                )
            }
            .toMutableList()

        if (cleaned.isEmpty()) {
            // User typed something but nothing complete
            sharedViewModel.cvModelRequestDb.projectList = mutableListOf(ProjectModel())
            adapter?.submitList(sharedViewModel.cvModelRequestDb.projectList.toList())
            Toast.makeText(
                requireContext(),
                "Please complete at least one project (title & description).",
                Toast.LENGTH_SHORT
            ).show()
            return@tryCatch
        }

        // 3) Persist cleaned list into CvModelRequestDb
        sharedViewModel.cvModelRequestDb.projectList = cleaned

        // 4) Update adapter UI
        adapter?.submitList(cleaned.toList())
        adapter?.expandOnly(cleaned.lastIndex)

        // 5) Here is where you would call your Room/DB save for the whole CvModelRequestDb:
        //    e.g. sharedViewModel.saveCurrentProfileToDb()
        //    (implement that in SharedViewModel / another ViewModel)

        Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
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
            Toast.makeText(
                requireContext(),
                "Please complete current project first",
                Toast.LENGTH_SHORT
            ).show()
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
            // If the user removed everything, keep one empty row so they can add again
            db.add(ProjectModel())
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
            // require filled only for non-empty projects
            if (isEmpty(m)) {
                true
            } else {
                title.isNotEmpty() && desc.isNotEmpty()
            }
        }
        if (!ok) {
            Toast.makeText(
                requireContext(),
                "Please complete project title and description.",
                Toast.LENGTH_SHORT
            ).show()
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

        // If nothing is fully complete but user typed something,
        // keep one empty row instead of leaving the list empty.
        sharedViewModel.cvModelRequestDb.projectList =
            if (complete.isEmpty()) mutableListOf(ProjectModel()) else complete

        adapter?.submitList(sharedViewModel.cvModelRequestDb.projectList.toList())

        if (partialCount > 0) {
            Toast.makeText(
                requireContext(),
                "Removed $partialCount incomplete project item(s).",
                Toast.LENGTH_SHORT
            ).show()
        }

        val lastIndex = sharedViewModel.cvModelRequestDb.projectList.lastIndex
        if (lastIndex >= 0) adapter?.expandOnly(lastIndex)
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
