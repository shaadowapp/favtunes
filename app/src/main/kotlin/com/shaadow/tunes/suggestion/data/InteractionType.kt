package com.shaadow.tunes.suggestion.data

enum class InteractionType { 
    PLAY, 
    SKIP, 
    LIKE, 
    DISLIKE, 
    COMPLETE 
}



enum class EndReason { 
    COMPLETED, 
    SKIPPED, 
    USER_STOPPED 
}