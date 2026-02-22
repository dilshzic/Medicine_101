package com.algorithmx.medicine101.utils

/**
 * Extracts the 11-character YouTube Video ID from any standard or shortened YouTube URL.
 * Handles:
 * - https://youtu.be/V0ai-gQP2wY?si=bamQQVt...
 * - https://www.youtube.com/watch?v=V0ai-gQP2wY
 * - https://www.youtube.com/shorts/V0ai-gQP2wY
 */
fun extractYoutubeId(input: String): String {
    // Regex that matches all standard YouTube URL formats and isolates the 11-char ID
    val regex = Regex("""(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?|shorts)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})""")
    val matchResult = regex.find(input)
    
    // Return the matched ID, or if no match is found, just return the input 
    // (in case the user manually typed just the 11-character ID)
    return matchResult?.groupValues?.get(1) ?: input
}