package com.example.cvmaker.data.entity

import androidx.constraintlayout.widget.ConstraintLayout

data class MyCreationRv (
    var cvName: String? = null,
    var isChecked: Boolean = false,
    var constraintLayout: ConstraintLayout? = null,
    var generation_id:Int = 0,
    var thumb_url:String = "",
    var pdf_url:String = ""
)
