package com.example.habit_tracker.utils // Or .utils, adjust as needed

import android.content.Context
import android.util.Log // For logging errors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object MaterialSymbolsRepository {

    private const val CODEPOINTS_FILENAME =
        "MaterialSymbolsOutlined.codepoints"
    private var codepointsMap: Map<String, Int>? = null // Cache the map

    /**
     * Gets the map of Material Symbol names to their codepoints.
     * Loads and parses the map from assets on the first call.
     * Uses a cached version on subsequent calls.
     * Must be called from a coroutine context.
     *
     * @param context Application or Activity context to access assets.
     * @return A map where the key is the symbol name (String) and the value is the codepoint (Int),
     *         or an empty map if loading fails.
     */
    suspend fun getCodepoints(context: Context): Map<String, Int> {
        // Return cached map if already loaded
        codepointsMap?.let { return it }

        // Load and parse on a background thread
        return withContext(Dispatchers.IO) {
            val loadedMap = mutableMapOf<String, Int>()
            try {
                context.assets.open(CODEPOINTS_FILENAME).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                        lines.forEach { line ->
                            val parts = line.split(" ")
                            if (parts.size == 2) {
                                val name = parts[0]
                                val codepointHex = parts[1]
                                // Convert hex string to Int
                                val codepointInt = codepointHex.toIntOrNull(16)
                                if (codepointInt != null) {
                                    loadedMap[name] = codepointInt
                                } else {
                                    Log.w(
                                        "MaterialSymbolsRepo",
                                        "Could not parse codepoint for: $line"
                                    )
                                }
                            }
                        }
                    }
                }
                Log.i("MaterialSymbolsRepo", "Successfully loaded ${loadedMap.size} codepoints.")
                codepointsMap = loadedMap // Cache the successfully loaded map
                loadedMap // Return the newly loaded map
            } catch (e: IOException) {
                Log.e(
                    "MaterialSymbolsRepo",
                    "Error reading codepoints file: $CODEPOINTS_FILENAME",
                    e
                )
                emptyMap() // Return empty map on error
            }
        }
    }

    /**
     * Gets the character string for a given symbol name using the loaded codepoints map.
     *
     * @param name The name of the material symbol (e.g., "home", "settings").
     * @return The character representation of the symbol, or null if not found or map not loaded.
     */
    fun getSymbolChar(name: String): String? {
        return codepointsMap?.get(name)?.let { Character.toChars(it).joinToString("") }
    }

    /**
     * Gets the character string for a given symbol name using the loaded codepoints map.
     * Provides a fallback character if the symbol is not found.
     *
     * @param name The name of the material symbol (e.g., "home", "settings").
     * @param fallbackSymbolName The name of the symbol to use if the requested one isn't found (e.g., "help").
     * @return The character representation of the symbol, or the fallback symbol, or "?" if fallback also fails.
     */
    fun getSymbolCharSafe(name: String?, fallbackSymbolName: String = "help"): String {
        val map = codepointsMap ?: return "?" // Return "?" if map isn't loaded at all yet

        val codepoint = name?.let { map[it] } ?: map[fallbackSymbolName] ?: return "?"

        return Character.toChars(codepoint).joinToString("")
    }

    /**
     * Pre-loads the codepoints map. Useful to call during app startup.
     * Must be called from a coroutine context.
     *
     * @param context Application or Activity context.
     */
    suspend fun preload(context: Context) {
        if (codepointsMap == null) {
            getCodepoints(context)
        }
    }
}