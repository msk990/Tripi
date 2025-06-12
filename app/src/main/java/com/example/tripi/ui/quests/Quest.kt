package com.example.tripi.ui.quests

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val image: String,
    val reward: Int,
    val status: QuestStatus
)

enum class QuestStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

