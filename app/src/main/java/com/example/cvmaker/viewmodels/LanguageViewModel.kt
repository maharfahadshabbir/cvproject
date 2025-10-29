package com.example.cvmaker.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

/*
class LanguageViewModel : ViewModel() {
    val _languageList = MutableLiveData<Pair<List<LanguagesModel>, LanguagesModel>>()
    val languageList: LiveData<Pair<List<LanguagesModel>, LanguagesModel>> get() = _languageList

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->}

    fun loadLanguageList(context: Context) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val loadedList = LanguageList(context)
            val excludedObject = loadedList.find { languageItem ->
                ViewUtils.getStringSharedPreferences(context,ViewUtils.LANG_KEY,"en") == languageItem.code
            }
            val filteredList = loadedList.filter { languageItem ->
                ViewUtils.getStringSharedPreferences(context,ViewUtils.LANG_KEY,"en") != languageItem.code
            }

            excludedObject?.let {
                val result = Pair(filteredList, excludedObject)
                _languageList.postValue(result)
            }

        }
    }
}*/
