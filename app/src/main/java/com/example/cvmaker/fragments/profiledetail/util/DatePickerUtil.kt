package com.example.cvmaker.fragments.profiledetail.util

import android.app.DatePickerDialog
import android.content.Context
import com.example.cvmaker.R
import com.example.cvmaker.utils.tryCatch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DatePickerUtil {

    interface DateSelectedListener {
        fun onDateSelected(formattedDate: String)
    }

    fun showDatePickerDialog(calendar: Calendar, value:Int, context: Context, listener: DateSelectedListener) {
        tryCatch {
            //        val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context,
                R.style.MyDatePickerDialogTheme,  // Set the custom style here
                { _, selectedYear, selectedMonth, selectedDay ->

                    val selectedCalendar = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                    }
                    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    if (value == 1){
                        val formattedDate = "${selectedMonth + 1}/$selectedYear"
                        listener.onDateSelected(formattedDate)

                    }else{

//                    val formattedDate = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                        val formattedDate = dateFormat.format(selectedCalendar.time)
                        listener.onDateSelected(formattedDate)
                    }


                },
                year, month, day
            )


            // Set the minimum date to January 1, 1900
            val minCalendar = Calendar.getInstance()
            minCalendar.set(1900, Calendar.JANUARY, 1)
            datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

            // Set the maximum date to the current date
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

            datePickerDialog.show()
        }
    }
}
