package com.shaadow.tunes.suggestion.engine

data class SuggestionWeights(
    var similarity: Float = 0.4f,
    var popularity: Float = 0.3f,
    var recency: Float = 0.3f
) {
    fun normalize() {
        val total = similarity + popularity + recency
        if (total > 0) {
            similarity /= total
            popularity /= total
            recency /= total
        }
    }
    
    fun reset() {
        similarity = 0.4f
        popularity = 0.3f
        recency = 0.3f
    }
}