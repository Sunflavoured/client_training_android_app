package com.example.client_training_app.utils

import android.net.Uri

object YouTubeUtils {
    fun extractVideoId(videoUrl: String): String? {
        // 1. Pokud je to plná URL (youtube.com/watch?v=ID)
        if (videoUrl.contains("v=")) {
            return videoUrl.substringAfter("v=").substringBefore("&")
        }
        // 2. Pokud je to zkrácená URL (youtu.be/ID)
        if (videoUrl.contains("youtu.be/")) {
            return videoUrl.substringAfter("youtu.be/").substringBefore("?")
        }
        return null
    }
}