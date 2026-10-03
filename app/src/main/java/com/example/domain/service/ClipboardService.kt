package com.example.domain.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClipboardService(private val context: Context) {

    private val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val scope = CoroutineScope(Dispatchers.Main)
    private var autoClearJob: Job? = null
    private var lastCopiedText: String? = null

    fun copySecure(label: String, text: String, autoClearDelayMs: Long = 30_000L) {
        val clip = ClipData.newPlainText(label, text)
        // Set sensitive content flag on Android 13+
        clip.description.extras = android.os.PersistableBundle().apply {
            putBoolean("android.content.extra.IS_SENSITIVE", true)
        }
        clipboardManager.setPrimaryClip(clip)
        lastCopiedText = text

        autoClearJob?.cancel()
        autoClearJob = scope.launch {
            delay(autoClearDelayMs)
            clearIfMatching(text)
        }
    }

    fun clearOnAppBackground() {
        autoClearJob?.cancel()
        lastCopiedText?.let { text ->
            clearIfMatching(text)
        }
    }

    private fun clearIfMatching(text: String) {
        try {
            val currentClip = clipboardManager.primaryClip
            if (currentClip != null && currentClip.itemCount > 0) {
                val currentText = currentClip.getItemAt(0).text?.toString()
                if (currentText == text) {
                    val emptyClip = ClipData.newPlainText("", "")
                    clipboardManager.setPrimaryClip(emptyClip)
                }
            }
        } catch (_: Exception) {
        }
        lastCopiedText = null
    }
}
