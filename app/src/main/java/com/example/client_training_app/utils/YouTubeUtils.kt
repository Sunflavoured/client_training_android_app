package com.example.client_training_app.utils

object YouTubeUtils {
    fun extractVideoId(videoUrl: String): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
        val compiledPattern = java.util.regex.Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(videoUrl)
        return if (matcher.find()) {
            matcher.group()
        } else {
            null
        }
    }
}