package com.shaadow.tunes.utils

import java.util.*

object RandomUtils {

    fun generateRandomUsername(): String {
        val words = listOf(
            "Lion", "Tiger", "Elephant", "Panda", "Wolf", "Bear", "Jaguar", "Cheetah", "Leopard", "Rhino",
            "Giraffe", "Koala", "Kangaroo", "Zebra", "Eagle", "Falcon", "Hawk", "Owl", "Peacock", "Sparrow",
            "Crow", "Parrot", "Dove", "Raven", "Stork", "Flamingo", "Penguin", "Seagull", "Puffin", "Albatross",

            // Countries
            "USA", "Canada", "Mexico", "Brazil", "Argentina", "Germany", "France", "Italy", "Spain", "UK",
            "Australia", "India", "China", "Russia", "SouthAfrica", "Japan", "SouthKorea", "Indonesia", "Egypt",
            "Nigeria", "Sweden", "Norway", "Finland", "Denmark", "Greece", "Netherlands", "Portugal", "Switzerland",
            "Ireland", "Poland", "Belgium", "Austria", "CzechRepublic", "Hungary", "Turkey", "Thailand", "Malaysia",

            // Cities
            "NewYork", "London", "Paris", "Tokyo", "Berlin", "Sydney", "LosAngeles", "Rome", "Barcelona", "Vienna",
            "Dubai", "DubaiCity", "Istanbul", "Amsterdam", "Stockholm", "CapeTown", "Mumbai", "Delhi", "Shanghai",
            "Seoul", "Beijing", "Singapore", "Bangkok", "Melbourne", "Madrid", "Zurich", "Warsaw", "Prague", "Moscow",
            "Helsinki", "Oslo", "Athens", "Cairo", "Dubai", "Lagos", "RioDeJaneiro", "Portland", "Chicago", "Vancouver",
            "Bali", "Zurich", "Florence", "Dubai", "SanFrancisco", "Munich", "Brisbane", "Lisbon", "Edinburgh", "Madrid",

            // Nature/Elements
            "Sky", "Sun", "Moon", "Cloud", "Star", "Lightning", "Thunder", "Wind", "Earth", "Fire", "Ocean",
            "Wave", "Storm", "Rain", "Blizzard", "Flood", "Snow", "Ice", "Sand", "Rock", "Mountain", "Valley",
            "River", "Lake", "Water", "Forest", "Jungle", "Desert", "Canyon", "Cave", "Cliff", "Creek", "Waterfall",
            "Beach", "Field", "Hill", "Peak", "Glacier", "Volcano", "Cove", "Tide",

            // Colors
            "Red", "Blue", "Green", "Yellow", "Orange", "Purple", "Pink", "Black", "White", "Gray", "Brown", "Gold",
            "Silver", "Copper", "Emerald", "Jade", "Ruby", "Diamond", "Sapphire", "Topaz", "Pearl", "Turquoise",
            "Amber", "Crimson", "Indigo", "Aqua", "Ivory", "Onyx", "Lavender", "Maroon", "Scarlet", "Beige", "Mauve",

            // Positive Attributes
            "Bright", "Shining", "Radiant", "Glowing", "Vibrant", "Charming", "Magnetic", "Graceful", "Bold", "Clever",
            "Smart", "Creative", "Unique", "Inventive", "Dynamic", "Inspiring", "Energetic", "Motivated", "Strong",
            "Free", "Swift", "Noble", "Vigorous", "Lively", "Chill", "Peaceful", "Radiant", "Happy", "Sincere", "Gentle",

            // Miscellaneous
            "Shadow", "Echo", "Vibe", "Echo", "Wave", "Chime", "Whisper", "Pulse", "Strum", "Riff", "Beat", "Rhythm",
            "Flux", "Blaze", "Storm", "Twist", "Crash", "Jolt", "Crush", "Splash", "Groove", "Tone", "Pitch", "Sound",
            "Bass", "Chord", "Frequency", "Echo", "Flow", "Tide", "Force", "Bolt", "Clash", "Surge", "Shock", "Vibration",

            // Abstract Words
            "Vision", "Dream", "Zenith", "Quest", "Journey", "Glory", "Infinity", "Cosmic", "Myth", "Pulse", "Force",
            "Mystic", "Aether", "Oracle", "Aura", "Legend", "Flare", "Hype", "Aura", "Odyssey", "Illusion", "Chroma",
            "Exile", "Eclipse", "Stellar", "Nebula", "Nova", "Solstice", "Eden", "Genesis", "Reverie", "Odyssey", "Omen",
        )

        val randomWord = words.random()  // Pick a random word
        val randomNumber = 1000 + Random().nextInt(9000)  // Generate 4-digit number (1000–9999)

        return "$randomWord$randomNumber"
    }

    fun generatePublicKey(): String {
        return UUID.randomUUID().toString().replace("-", "_")
    }

    fun generatePrivateKey(): String {
        return UUID.randomUUID().toString().replace("-", "_")
    }
}
