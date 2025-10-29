package com.example.cvmaker.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.cvmaker.activities.MainActivity
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


fun Fragment.ifMainActivity(): MainActivity? {
    return activity?.let {
        if (it is MainActivity) {
            it
        } else {
            null
        }
    }
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

fun TextView.setTextColorSafe(colorRes: Int) {
    tryCatch {
        context?.let { currentContext ->
            this.setTextColor(ContextCompat.getColor(currentContext, colorRes))
        }
    }
}

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beInVisible() {
    visibility = View.INVISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}

fun Fragment.getViewLifecycleOwnerOrNull(): LifecycleOwner? =
    view?.let { viewLifecycleOwner }

fun NavController.canGoBack(isPressed: (Boolean) -> Unit) {
    isPressed.invoke(this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connectivityManager.activeNetwork ?: return false
    } else null
    val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}