package com.example.tripi.stickers.model

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StickerAssetMap {
    fun loadFromJson(context: Context): Map<String, String> {
        return try {
            val json = context.assets.open("stickers/sticker_assets.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<Map<String, String>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            Log.w("StickerAssetMap", "Failed to load JSON: ${e.message}")
            Toast.makeText(context, "Could not load sticker data. Using default setup.", Toast.LENGTH_LONG).show()
            emptyMap()
        }
    }
}
