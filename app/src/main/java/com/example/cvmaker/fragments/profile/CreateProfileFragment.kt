package com.example.cvmaker.fragments.profile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cvmaker.R
import com.example.cvmaker.databinding.FragmentCreateProfileBinding
import com.example.cvmaker.model.workingmodels.CvModelRequestDb
import com.example.cvmaker.model.workingmodels.Education
import com.example.cvmaker.typeConvertor.CvModelRequestEntity
import com.example.cvmaker.viewmodels.MyViewModel
import com.example.cvmaker.viewmodels.SharedViewModel
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.getValue

class CreateProfileFragment : Fragment() {

    // Nullable binding
    private var _binding: FragmentCreateProfileBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private val myViewModel by activityViewModels<MyViewModel>()

    companion object {
        private const val TAG = "CreateProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClicks()
    }

    private fun logClick(name: String) {
        Log.d(TAG, "Clicked: $name")
    }

    private fun setClicks() = with(binding) {
        // Toolbar actions
        backButton.setOnClickListener {
            logClick("backButton")
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        toolbarTitle.setOnClickListener { logClick("toolbarTitle") }

        // Preview chip (container + inner views)
        preview.setOnClickListener { logClick("preview container") }

        // Section tiles
        personalDetail.setOnClickListener {
            findNavController().navigate(R.id.personalDetailFragment)
            logClick("personalDetail")
        }
        educationDetail.setOnClickListener {
            findNavController().navigate(R.id.educationDetailFragment)
            logClick("educationDetail")
        }
        experience.setOnClickListener {
            findNavController().navigate(R.id.experienceDetailFragment)
            logClick("experience")
        }
        skills.setOnClickListener {
            findNavController().navigate(R.id.skillsDetailsFragment)
            logClick("skills")
        }
        objective.setOnClickListener {
            findNavController().navigate(R.id.objectiveFragment)
            logClick("objective")
        }
        references.setOnClickListener {
            findNavController().navigate(R.id.referencesDetailFragment)
            logClick("references")
        }
        projects.setOnClickListener {
            findNavController().navigate(R.id.projectDetailsFragment)
            logClick("projects")
        }
        interest.setOnClickListener {
            findNavController().navigate(R.id.interestDetailFragment)
            logClick("interest")
        }
        portfolio.setOnClickListener {
            findNavController().navigate(R.id.portfolioDetailsFragment)
            logClick("portfolio")
        }
        certification.setOnClickListener {
            findNavController().navigate(R.id.certificationDetailsFragment)
            logClick("certification")
        }
        achievements.setOnClickListener {
            findNavController().navigate(R.id.achievementsDetailsFragment)
            logClick("achievements")
        }
        languages.setOnClickListener {
            findNavController().navigate(R.id.languageDetailsFragment)
            logClick("languages")
        }


        // Bottom primary button
        profile.setOnClickListener {
            lifecycleScope.launch {
                try {

                  var educationList = listOf(
                        Education(
                            institute = "Harvard University",
                            course = "Computer Science",
                            grade = "A+",
                            startDate = "09/2018",
                            endDate = "05/2022",
                            isCurrentStudent = false
                        ),
                        Education(
                            institute = "Stanford University",
                            course = "Software Engineering",
                            grade = "A",
                            startDate = "09/2022",
                            endDate = "Present",
                            isCurrentStudent = true
                        ),
                        Education(
                            institute = "MIT",
                            course = "Artificial Intelligence",
                            grade = "A+",
                            startDate = "01/2021",
                            endDate = "12/2023",
                            isCurrentStudent = false
                        )
                    )



                    sharedViewModel.cvModelRequestDb.educationList = educationList.toMutableList()
                    Log.d("SaveCV", "🟢 Starting CV save process...")

                    // ✅ Check if cvModel is not null
                    val cvModel = sharedViewModel.cvModelRequestDb
                    if (cvModel == null) {
                        Log.e("SaveCV", "❌ cvModel is NULL! Cannot save.")
                        Toast.makeText(requireContext(), "No CV data to save", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // ✅ Log key model info for debugging
                    Log.d("SaveCV", "Personal details: ${cvModel.personalDetails}")

                    val userId = UUID.randomUUID().toString()



                    val gson = Gson()
                    val json = gson.toJson(cvModel)

                    // ✅ Log JSON size and preview
                    Log.d("SaveCV", "Generated JSON length: ${json.length}")
                    Log.v("SaveCV", "JSON Preview (first 500 chars): ${json.take(500)}")

                    val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

                    val entity = CvModelRequestEntity(
                        json = json,
                        draftName = "CV_${System.currentTimeMillis()}",
                        profileOrDraft = true, // it is profile
                        updateDate = currentTime,
                        creationDate = currentTime,
                        userId = userId
                    )

                    Log.d("SaveCV", "Inserting entity into DB: $entity")

                    myViewModel.insertCvModelRequest(entity)

                    //fetch
                    lifecycleScope.launch {
                        delay(5000) // 5 seconds delay
                        var data = myViewModel.getCvModelRequest()
                        Log.i("SaveCV", "✅ profile fetched successfully from DB at $currentTime and data is $data")



                        val gson = Gson()
                        val deserializedList = data.mapNotNull { entity ->
                            try {
                                gson.fromJson(entity?.json, CvModelRequestDb::class.java)
                            } catch (e: Exception) {
                                Log.e("SaveCV", "❌ Failed to deserialize CV JSON (id=${entity?.id}): ${e.localizedMessage}")
                                null
                            }
                        }

// ✅ Log the deserialized results
                        deserializedList.forEachIndexed { index, cv ->
                            Log.d("SaveCV", "🧩 CV #$index -> Personal: ${cv.personalDetails?.name}, Education count: ${cv.educationList?.size}")
                        }

                    }

                    Log.i("SaveCV", "✅ CV saved successfully to DB at $currentTime")

                    Toast.makeText(requireContext(), "✅ CV saved successfully!", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    Log.e("SaveCV", "❌ Exception while saving CV: ${e.localizedMessage}", e)
                    e.printStackTrace()

                    // If Gson serialization failed, log details
                    try {
                        val jsonFallback = Gson().toJson(sharedViewModel.cvModel)
                        Log.e("SaveCV", "🔍 Partial JSON that caused error:\n$jsonFallback")
                    } catch (inner: Exception) {
                        Log.e("SaveCV", "⚠️ Could not even serialize cvModel for debug: ${inner.localizedMessage}")
                    }

                    Toast.makeText(requireContext(), "❌ Failed to save CV. Check logs.", Toast.LENGTH_LONG).show()
                }
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
