package com.kennethdo.week6.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
object DashboardRoute

@Serializable
object CommunitiesRoute

@Serializable
data class ClubProjectsRoute(val communityId: String)

@Serializable
data class ProjectDetailRoute(val projectId: String)

data class StudentProfile(
    val name: String = "Aditya Pratama",
    val major: String = "Informatika",
    val batch: String = "Angkatan 2024"
)

data class Community(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val isJoined: Boolean = false
)

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val communityId: String,
    val title: String,
    val description: String,
    val techStack: List<String>,
    val isCompleted: Boolean = false
)

data class NewProjectForm(
    val title: String,
    val description: String,
    val techStack: List<String>
)

val availableTechStacks = listOf(
    "Kotlin", "Jetpack Compose", "Firebase", "Ktor",
    "Room", "Python", "Java", "Git", "TensorFlow"
)
