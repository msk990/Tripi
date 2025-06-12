package com.example.tripi.ui.quests

import android.content.Context
import com.google.gson.Gson

object QuestRepository {
    fun loadQuests(context: Context): List<Quest> {
        val json = context.assets.open("quests/quests.json").bufferedReader().use { it.readText() }
        return Gson().fromJson(json, Array<Quest>::class.java).toList()
    }
}
