package com.kennethdo.week6.viewmodel

import androidx.lifecycle.ViewModel
import com.kennethdo.week6.model.Community
import com.kennethdo.week6.model.NewProjectForm
import com.kennethdo.week6.model.Project
import com.kennethdo.week6.model.StudentProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CampusUiState(
    val profile: StudentProfile = StudentProfile(),
    val communities: List<Community> = listOf(
        Community(
            "gdsc",
            "Google Developer Student Clubs",
            "Build, learn, and connect with other developers.",
            "Development"
        ),
        Community(
            "cyber",
            "CyberSecurity Guild",
            "Explore cybersecurity, ethical hacking, and digital safety.",
            "Security"
        ),
        Community(
            "ai",
            "AI Society",
            "Learn machine learning and artificial intelligence.",
            "Artificial Intelligence"
        ),
        Community(
            "robotics",
            "Robotics Club",
            "Design and build robots with fellow makers.",
            "Engineering"
        ),
        Community(
            "web",
            "Web Development Community",
            "Create modern websites and web applications.",
            "Development"
        ),
        Community(
            "game",
            "Game Developers Association",
            "Explore game design, programming, and interactive media.",
            "Game Development"
        )
    ),
    val projects: List<Project> = emptyList()
)

class CampusViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CampusUiState())
    val uiState = _uiState.asStateFlow()

    fun toggleCommunity(communityId: String) {
        _uiState.update { state ->
            state.copy(
                communities = state.communities.map {
                    if (it.id == communityId) it.copy(isJoined = !it.isJoined) else it
                }
            )
        }
    }

    fun addProject(communityId: String, form: NewProjectForm) {
        val project = Project(
            communityId = communityId,
            title = form.title.trim(),
            description = form.description.trim(),
            techStack = form.techStack
        )
        _uiState.update { it.copy(projects = it.projects + project) }
    }

    fun deleteProject(projectId: String) {
        _uiState.update { state ->
            state.copy(projects = state.projects.filterNot { it.id == projectId })
        }
    }

    fun toggleProjectStatus(projectId: String) {
        _uiState.update { state ->
            state.copy(
                projects = state.projects.map {
                    if (it.id == projectId) it.copy(isCompleted = !it.isCompleted) else it
                }
            )
        }
    }
}
