package com.kennethdo.week6.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kennethdo.week6.viewmodel.CampusUiState
import com.kennethdo.week6.viewmodel.CampusViewModel
import com.kennethdo.week6.model.ClubProjectsRoute
import com.kennethdo.week6.model.CommunitiesRoute
import com.kennethdo.week6.model.Community
import com.kennethdo.week6.model.DashboardRoute
import com.kennethdo.week6.model.NewProjectForm
import com.kennethdo.week6.model.Project
import com.kennethdo.week6.model.ProjectDetailRoute
import com.kennethdo.week6.model.availableTechStacks

@Composable
fun CampusApp(viewModel: CampusViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = backStackEntry?.destination?.hasRoute<DashboardRoute>() == true ||
        backStackEntry?.destination?.hasRoute<CommunitiesRoute>() == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = backStackEntry?.destination?.hasRoute<DashboardRoute>() == true,
                        onClick = {
                            navController.navigate(DashboardRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = backStackEntry?.destination?.hasRoute<CommunitiesRoute>() == true,
                        onClick = {
                            navController.navigate(CommunitiesRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                        label = { Text("Communities") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<DashboardRoute> {
                DashboardScreen(
                    state = state,
                    onCommunityClick = { communityId ->
                        val community = state.communities.firstOrNull { it.id == communityId }
                        if (community?.isJoined == true) {
                            navController.navigate(ClubProjectsRoute(communityId))
                        } else {
                            Toast.makeText(
                                context,
                                "Harap bergabung ke komunitas terlebih dahulu!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onProjectClick = { projectId ->
                        navController.navigate(ProjectDetailRoute(projectId))
                    }
                )
            }

            composable<CommunitiesRoute> {
                CommunitiesScreen(
                    state = state,
                    onToggleJoin = viewModel::toggleCommunity,
                    onCommunityClick = { communityId ->
                        val community = state.communities.firstOrNull { it.id == communityId }
                        if (community?.isJoined == true) {
                            navController.navigate(ClubProjectsRoute(communityId))
                        } else {
                            Toast.makeText(
                                context,
                                "Harap bergabung ke komunitas terlebih dahulu!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            composable<ClubProjectsRoute> { entry ->
                val route = entry.toRoute<ClubProjectsRoute>()
                val community = state.communities.firstOrNull { it.id == route.communityId }
                ClubProjectsScreen(
                    community = community,
                    projects = state.projects.filter { it.communityId == route.communityId },
                    onBack = { navController.popBackStack() },
                    onAddProject = { form -> viewModel.addProject(route.communityId, form) },
                    onDeleteProject = viewModel::deleteProject,
                    onProjectClick = { projectId ->
                        navController.navigate(ProjectDetailRoute(projectId))
                    }
                )
            }

            composable<ProjectDetailRoute> { entry ->
                val route = entry.toRoute<ProjectDetailRoute>()
                val project = state.projects.firstOrNull { it.id == route.projectId }
                val community = state.communities.firstOrNull { it.id == project?.communityId }
                ProjectDetailScreen(
                    project = project,
                    communityName = community?.name ?: "Unknown Community",
                    onBack = { navController.popBackStack() },
                    onToggleStatus = { viewModel.toggleProjectStatus(route.projectId) }
                )
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    state: CampusUiState,
    onCommunityClick: (String) -> Unit,
    onProjectClick: (String) -> Unit
) {
    val joined = state.communities.filter { it.isJoined }
    val projects = state.projects
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("CampusTech Hub", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Your campus technology space", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AP", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(state.profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${state.profile.major} • ${state.profile.batch}")
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CounterCard("Joined Clubs", joined.size.toString(), Modifier.weight(1f))
                CounterCard("Total Projects", projects.size.toString(), Modifier.weight(1f))
            }
        }
        item { SectionTitle("My Communities") }
        if (joined.isEmpty()) {
            item { EmptyMessage("Belum bergabung dengan komunitas") }
        } else {
            items(joined, key = { it.id }) { community ->
                CommunitySummaryCard(community, projects.count { it.communityId == community.id }) {
                    onCommunityClick(community.id)
                }
            }
        }
        item { SectionTitle("My Projects") }
        if (projects.isEmpty()) {
            item { EmptyMessage("Belum ada proyek yang dicatat") }
        } else {
            items(projects, key = { it.id }) { project ->
                val clubName = state.communities.firstOrNull { it.id == project.communityId }?.name ?: "Community"
                ProjectCard(project, clubName, onClick = { onProjectClick(project.id) })
            }
        }
    }
}

@Composable
private fun CommunitiesScreen(
    state: CampusUiState,
    onToggleJoin: (String) -> Unit,
    onCommunityClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Communities", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Find your people. Build something meaningful.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(state.communities, key = { it.id }) { community ->
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(community.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(community.category, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    Text(community.description, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onToggleJoin(community.id) },
                            colors = if (community.isJoined) ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ) else ButtonDefaults.buttonColors()
                        ) {
                            Text(if (community.isJoined) "Joined" else "Join")
                        }
                        OutlinedButton(onClick = { onCommunityClick(community.id) }) {
                            Text("View Projects")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubProjectsScreen(
    community: Community?,
    projects: List<Project>,
    onBack: () -> Unit,
    onAddProject: (NewProjectForm) -> Unit,
    onDeleteProject: (String) -> Unit,
    onProjectClick: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${community?.name ?: "Community"} Projects", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add project")
            }
        }
    ) { padding ->
        if (projects.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                EmptyMessage("Belum ada proyek. Tekan + untuk menambahkan proyek.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        communityName = community?.name ?: "Community",
                        onClick = { onProjectClick(project.id) },
                        onDelete = { onDeleteProject(project.id) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddProjectDialog(
            onCancel = { showDialog = false },
            onSave = {
                onAddProject(it)
                showDialog = false
            }
        )
    }
}

@Composable
private fun AddProjectDialog(
    onCancel: () -> Unit,
    onSave: (NewProjectForm) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedTech by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Add Project") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tech Stack (select one or more)", style = MaterialTheme.typography.titleSmall)
                availableTechStacks.forEach { tech ->
                    FilterChip(
                        selected = tech in selectedTech,
                        onClick = {
                            selectedTech = if (tech in selectedTech) selectedTech - tech else selectedTech + tech
                        },
                        label = { Text(tech) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && description.isNotBlank() && selectedTech.isNotEmpty(),
                onClick = {
                    onSave(NewProjectForm(title, description, selectedTech.toList()))
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectDetailScreen(
    project: Project?,
    communityName: String,
    onBack: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (project == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Project not found.")
            }
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(project.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                AssistChip(onClick = {}, label = { Text(communityName) })
                Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(project.description)
                Text("Tech Stack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    project.techStack.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
                }
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Project Status", fontWeight = FontWeight.Bold)
                        Text(if (project.isCompleted) "Completed" else "In Progress")
                    }
                    Switch(checked = project.isCompleted, onCheckedChange = { onToggleStatus() })
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    communityName: String,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(project.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(communityName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(if (project.isCompleted) "Completed" else "In Progress", style = MaterialTheme.typography.bodySmall)
                Text(project.techStack.joinToString(" • "), style = MaterialTheme.typography.bodySmall)
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete project", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun CommunitySummaryCard(community: Community, projectCount: Int, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp)) {
            Text(community.name, fontWeight = FontWeight.Bold)
            Text("$projectCount projects", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CounterCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyMessage(message: String) {
    Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Preview(showBackground = true)
@Composable
fun CampusAppPreview() {
    val campusViewModel: CampusViewModel = viewModel()
    CampusApp(campusViewModel)
}