package com.shaadow.tunes.data

object OnboardingData {
    
    data class MusicGenre(
        val id: String,
        val name: String,
        val emoji: String = "🎵"
    )
    
    data class MusicMood(
        val id: String,
        val name: String,
        val emoji: String = "😊"
    )
    
    data class Language(
        val id: String,
        val name: String,
        val emoji: String = "🌍"
    )
    
    val genres = listOf(
        MusicGenre("pop", "Pop", "🎤"),
        MusicGenre("rock", "Rock", "🎸"),
        MusicGenre("hip_hop", "Hip Hop", "🎤"),
        MusicGenre("electronic", "Electronic", "🎛️"),
        MusicGenre("jazz", "Jazz", "🎺"),
        MusicGenre("classical", "Classical", "🎼"),
        MusicGenre("country", "Country", "🤠"),
        MusicGenre("r_and_b", "R&B", "🎶"),
        MusicGenre("indie", "Indie", "🎸"),
        MusicGenre("folk", "Folk", "🪕"),
        MusicGenre("blues", "Blues", "🎷"),
        MusicGenre("reggae", "Reggae", "🏝️"),
        MusicGenre("punk", "Punk", "⚡"),
        MusicGenre("metal", "Metal", "🤘"),
        MusicGenre("funk", "Funk", "🕺"),
        MusicGenre("soul", "Soul", "💫"),
        MusicGenre("disco", "Disco", "🪩"),
        MusicGenre("house", "House", "🏠"),
        MusicGenre("techno", "Techno", "🤖"),
        MusicGenre("ambient", "Ambient", "🌙"),
        MusicGenre("trap", "Trap", "🔥"),
        MusicGenre("dubstep", "Dubstep", "🎧"),
        MusicGenre("latin", "Latin", "💃"),
        MusicGenre("afrobeat", "Afrobeat", "🥁"),
        MusicGenre("k_pop", "K-Pop", "🇰🇷"),
        MusicGenre("bollywood", "Bollywood", "🇮🇳"),
        MusicGenre("alternative", "Alternative", "🎭"),
        MusicGenre("grunge", "Grunge", "🎸"),
        MusicGenre("ska", "Ska", "🎺"),
        MusicGenre("gospel", "Gospel", "⛪")
    )
    
    val moods = listOf(
        MusicMood("energetic", "Energetic", "⚡"),
        MusicMood("chill", "Chill", "😌"),
        MusicMood("focus", "Focus", "🎯"),
        MusicMood("party", "Party", "🎉"),
        MusicMood("romantic", "Romantic", "💕"),
        MusicMood("workout", "Workout", "💪"),
        MusicMood("study", "Study", "📚"),
        MusicMood("sleep", "Sleep", "😴"),
        MusicMood("happy", "Happy", "😊"),
        MusicMood("sad", "Sad", "😢"),
        MusicMood("angry", "Angry", "😠"),
        MusicMood("nostalgic", "Nostalgic", "🌅"),
        MusicMood("adventurous", "Adventurous", "🗺️"),
        MusicMood("peaceful", "Peaceful", "🕊️"),
        MusicMood("confident", "Confident", "💯"),
        MusicMood("creative", "Creative", "🎨"),
        MusicMood("melancholic", "Melancholic", "🌧️"),
        MusicMood("uplifting", "Uplifting", "🌈"),
        MusicMood("dramatic", "Dramatic", "🎭"),
        MusicMood("mysterious", "Mysterious", "🔮")
    )
    
    val languages = listOf(
        Language("en", "English", "🇺🇸"),
        Language("es", "Spanish", "🇪🇸"),
        Language("fr", "French", "🇫🇷"),
        Language("de", "German", "🇩🇪"),
        Language("it", "Italian", "🇮🇹"),
        Language("pt", "Portuguese", "🇵🇹"),
        Language("ru", "Russian", "🇷🇺"),
        Language("ja", "Japanese", "🇯🇵"),
        Language("ko", "Korean", "🇰🇷"),
        Language("zh", "Chinese", "🇨🇳"),
        Language("hi", "Hindi", "🇮🇳"),
        Language("ar", "Arabic", "🇸🇦"),
        Language("tr", "Turkish", "🇹🇷"),
        Language("pl", "Polish", "🇵🇱"),
        Language("nl", "Dutch", "🇳🇱"),
        Language("sv", "Swedish", "🇸🇪"),
        Language("no", "Norwegian", "🇳🇴"),
        Language("da", "Danish", "🇩🇰"),
        Language("fi", "Finnish", "🇫🇮"),
        Language("he", "Hebrew", "🇮🇱")
    )
}