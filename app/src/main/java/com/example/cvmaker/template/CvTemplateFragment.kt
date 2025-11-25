import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cvmaker.R
import com.example.cvmaker.adaptor.CvTemplateAdapter
import com.example.cvmaker.cv.CvTemplateOption
import com.example.cvmaker.cvgenerator.CvGenerator
import com.example.cvmaker.databinding.FragmentCvTemplateBinding
import com.example.cvmaker.model.workingmodels.CvModelRequestDb
import com.example.cvmaker.viewmodels.SharedViewModel
import com.rajat.pdfviewer.PdfRendererView

class CvTemplateFragment : Fragment() {

    private var _binding: FragmentCvTemplateBinding? = null
    private val binding get() = _binding!!

    private var selectedTemplate: CvTemplateOption? = null
    private lateinit var templateAdapter: CvTemplateAdapter
    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private var cvData: CvModelRequestDb? = null
    private var currentCvUrl: String? = null

    private val TAG = "CvTemplateFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCvTemplateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.cvData.observe(viewLifecycleOwner) { data ->
            cvData = data
            Log.d(TAG, "CV data received from ViewModel: $cvData")
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                @SuppressLint("SetTextI18n")
                override fun handleOnBackPressed() {
                    if (binding.pdfViewCV.isVisible) {
                        // Hide PDF and show templates again
                        binding.pdfViewCV.visibility = View.GONE
                        binding.btnDownloadCV.visibility = View.GONE
                        binding.rvTemplates.visibility = View.VISIBLE
                        binding.btnGenerateCV.visibility = View.VISIBLE
                        binding.tvSelectTemplate.text = "Select Template"
                    } else {
                        parentFragmentManager.popBackStack()
                    }
                }
            })

        setupTemplates()
    }

    private fun setupTemplates() {
        val templates = listOf(
            CvTemplateOption("ats1", R.drawable.temp1),
            CvTemplateOption("style1", R.drawable.temp2),
            CvTemplateOption("style2", R.drawable.temp3),
            CvTemplateOption("style3", R.drawable.temp4)
        )

        templateAdapter = CvTemplateAdapter(templates) { template ->
            selectedTemplate = template
            binding.btnGenerateCV.isEnabled = true
        }

        binding.rvTemplates.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2 items per row
            adapter = templateAdapter
        }

        binding.btnGenerateCV.setOnClickListener {
            selectedTemplate?.let { template ->
                generateCV(template.templateName)
            }
        }

        binding.btnDownloadCV.setOnClickListener {
            currentCvUrl?.let { url -> downloadPdf(url) }
        }
    }

    private fun generateCV(templateName: String) {
        cvData?.let { cv ->
            CvGenerator.generate(cv, templateName) { success, urls ->
                if (success && urls.isNotEmpty()) {
                    val url = urls[0]
                    currentCvUrl = url
                    requireActivity().runOnUiThread {
                        // Hide templates
                        binding.rvTemplates.visibility = View.GONE
                        binding.btnGenerateCV.visibility = View.GONE
                        binding.tvSelectTemplate.text = "Your CV"

                        // Show PDF
                        binding.pdfViewCV.visibility = View.VISIBLE
                        loadPdfUrl( url)
                        binding.btnDownloadCV.visibility = View.VISIBLE
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "CV generation failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadPdfUrl(url: String) {
        // Show overlay
        binding.progressOverlay.visibility = View.VISIBLE
        binding.pdfViewCV.visibility = View.VISIBLE

        binding.pdfViewCV.initWithUrl(
            url = url,
            lifecycleCoroutineScope = viewLifecycleOwner.lifecycleScope,
            lifecycle = viewLifecycleOwner.lifecycle
        )

        binding.pdfViewCV.statusListener = object : PdfRendererView.StatusCallBack {
            @SuppressLint("SetTextI18n")
            override fun onPdfLoadStart() {
                Log.d(TAG, "PDF load started")
                binding.progressText.text = "Loading CV... 0%"
            }

            @SuppressLint("SetTextI18n")
            override fun onPdfLoadProgress(progress: Int, downloadedBytes: Long, totalBytes: Long?) {
                Log.d(TAG, "PDF loading: $progress%")
                binding.progressBar.progress = progress
                binding.progressText.text = "Loading CV... $progress%"
            }

            override fun onPdfLoadSuccess(absolutePath: String) {
                Log.d(TAG, "PDF loaded: $absolutePath")
            }

            override fun onError(error: Throwable) {
                Log.e(TAG, "PDF load error", error)
                binding.progressOverlay.visibility = View.GONE
            }

            override fun onPageChanged(currentPage: Int, totalPage: Int) { }

            override fun onPdfRenderStart() {
                Log.d(TAG, "PDF render started")
            }

            override fun onPdfRenderSuccess() {
                Log.d(TAG, "PDF render success")
                // Hide overlay once rendered
                binding.progressOverlay.visibility = View.GONE
            }
        }
    }

    private fun downloadPdf(url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Generated_CV.pdf")
                .setDescription("Downloading your CV")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Generated_CV.pdf")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(requireContext(), "Downloading CV...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Download failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
