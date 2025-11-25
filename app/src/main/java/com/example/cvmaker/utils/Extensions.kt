package com.example.cvmaker.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import java.io.FileNotFoundException
import java.io.IOException

fun tryCatch(callback: () -> Unit) {
    try {
        callback()

    } catch (t: Throwable) {
        t.localizedMessage.debug()
    } catch (e: NullPointerException) {
        e.localizedMessage.debug()
    } catch (e: IllegalArgumentException) {
        e.localizedMessage.debug()
    } catch (e: IllegalStateException) {
        e.localizedMessage.debug()
    } catch (e: IndexOutOfBoundsException) {
        e.localizedMessage.debug()
    } catch (e: UnsupportedOperationException) {
        e.localizedMessage.debug()
    } catch (e: ClassCastException) {
        e.localizedMessage.debug()
    } catch (e: IOException) {
        e.localizedMessage.debug()
    } catch (e: Exception) {
        e.localizedMessage.debug()
    } catch (e: java.lang.Exception) {
        e.localizedMessage.debug()
    } catch (e: IOException) {
        e.localizedMessage.debug()
    } catch (e: FileNotFoundException) {
        e.localizedMessage.debug()
    }
}


fun Any?.debug(tag: String = "find") {
    Log.d(tag, "$this")
}


fun Fragment.showToastSafe(text: String) {

    tryCatch {

        context?.let { currentContext ->

            Handler(Looper.getMainLooper()).post {

                tryCatch {
                    Toast.makeText(currentContext, text, Toast.LENGTH_SHORT).show()
                }

            }

        }

    }
}


fun Fragment.getViewLifecycleOwnerOrNull(): LifecycleOwner? =
    view?.let { viewLifecycleOwner }
