package com.example.cvmaker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cvmaker.R
import com.example.cvmaker.adaptor.LanguageAdapter
import com.example.cvmaker.databinding.FragmentLanguageBinding
import com.example.cvmaker.model.Language
import com.example.cvmaker.utils.AppPreferences
import com.google.android.material.card.MaterialCardView

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LanguageAdapter
    private lateinit var prefs: AppPreferences
    private var selectedCode: String? = null
    private var fromSplash = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = AppPreferences(requireContext())

        // detect if this fragment came from SplashFragment
        val previousDestination = findNavController().previousBackStackEntry?.destination
        fromSplash = previousDestination?.id == R.id.splashFragment

        // hide back button if from splash
        binding.btnBack.isVisible = !fromSplash

        // disable back press if from splash
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!fromSplash) {
                        // allow normal back press if not from splash
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        // do nothing (disable going back)
                    }
                }
            }
        )

        // back arrow click
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // seed list and restore saved selection
        val data = provideLanguages().toMutableList()
        val saved = prefs.getLanguageCode()
        if (!saved.isNullOrEmpty()) {
            data.find { it.code == saved }?.isSelected = true
            selectedCode = saved
        }

        // setup RecyclerView
        adapter = LanguageAdapter(data) { tapped ->
            selectedCode = tapped.code
            setDoneEnabled(true)
        }

        binding.recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recycler.adapter = adapter

        // Done pill state
        setDoneEnabled(selectedCode != null)

        // Done click
        binding.btnDoneCard.setOnClickListener {
            val chosen = selectedCode
            if (chosen == null) {
                Toast.makeText(requireContext(), R.string.select_language, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            prefs.saveLanguageCode(chosen)
            applyLocale(chosen)

            findNavController().navigate(R.id.onboardingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------- helpers ----------
    private fun setDoneEnabled(enabled: Boolean) {
        val card: MaterialCardView = binding.btnDoneCard
        card.isEnabled = enabled
        if (enabled) {
            card.setCardBackgroundColor(0xFF2563EB.toInt()) // blue
            binding.txtDone.alpha = 1f
            binding.imgTick.alpha = 1f
        } else {
            card.setCardBackgroundColor(0x332563EB) // faint blue
            binding.txtDone.alpha = .6f
            binding.imgTick.alpha = .6f
        }
    }

    private fun provideLanguages(): List<Language> = listOf(
        Language("en", "English", "Select English", R.drawable.english),
        Language("ar", "Arabic", "العربية", R.drawable.ar),
        Language("hi", "Hindi (हिंदी)", "हिंदी भाषा चुनें", R.drawable.hi),
        Language("pt", "Portuguese", "Selecione Português", R.drawable.portugues),
        Language("ru", "Russian", "Выберите русский", R.drawable.russian),
        Language("zh", "Chinese", "选择中文", R.drawable.chines),
        Language("es", "Spanish", "Seleccionar español", R.drawable.spanish),
        Language("de", "German", "Wählen Sie Deutsch", R.drawable.german),
        Language("fr", "French", "Sélectionnez le français", R.drawable.french)
    )

    private fun applyLocale(code: String) {
        val locales = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
