package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreCategory
import com.example.data.model.StoreNote
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.components.PlainEmptyState
import com.example.ui.components.SearchGlassField
import com.example.ui.components.VividButton
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Step 9: Manage Categories Screen
 * Two tabs: Expenses and Income.
 * Add category, Rename category, Archive category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val categories by storeViewModel.categories.collectAsState()
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Expense, 1 = Income
    val currentType = if (selectedTabIndex == 0) "expense" else "income"

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToRename by remember { mutableStateOf<StoreCategory?>(null) }
    var categoryToArchive by remember { mutableStateOf<StoreCategory?>(null) }

    val activeCategories = remember(categories, currentType) {
        categories.filter { it.type == currentType && !it.isArchived }
    }

    BackHandler {
        storeViewModel.navigateBackInBusiness()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            color = Color(0xF20F0F0F),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { storeViewModel.navigateBackInBusiness() },
                    modifier = Modifier.testTag("categories_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "ক্যাটেগরি ব্যবস্থাপনা" else "Manage Categories",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Customize expense & income categories",
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )
                }
                VividButton(
                    text = "+ Add",
                    onClick = { showAddCategoryDialog = true },
                    icon = Icons.Default.Add,
                    testTag = "btn_add_category"
                )
            }
        }

        // Two Tabs: Expense & Income
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DarkBackground,
            contentColor = VividGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = VividGreen
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "খরচ ক্যাটেগরি" else "Expense Categories",
                        color = if (selectedTabIndex == 0) VividGreen else TextSecondary,
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "আয় ক্যাটেগরি" else "Income Categories",
                        color = if (selectedTabIndex == 1) VividGreen else TextSecondary,
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (activeCategories.isEmpty()) {
                item {
                    PlainEmptyState(message = "No active categories for this type")
                }
            } else {
                items(activeCategories, key = { it.id }) { cat ->
                    CategoryRowCard(
                        category = cat,
                        onRename = { categoryToRename = cat },
                        onArchive = { categoryToArchive = cat }
                    )
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            type = currentType,
            existingNames = activeCategories.map { it.name.trim().lowercase() },
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name ->
                storeViewModel.createCategory(
                    name = name,
                    type = currentType,
                    onSuccess = { showAddCategoryDialog = false }
                )
            }
        )
    }

    if (categoryToRename != null) {
        RenameCategoryDialog(
            category = categoryToRename!!,
            existingNames = activeCategories.map { it.name.trim().lowercase() },
            onDismiss = { categoryToRename = null },
            onConfirm = { newName ->
                storeViewModel.updateCategory(
                    category = categoryToRename!!,
                    newName = newName,
                    onSuccess = { categoryToRename = null }
                )
            }
        )
    }

    if (categoryToArchive != null) {
        BasicAlertDialog(onDismissRequest = { categoryToArchive = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = DarkBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Archive Category?",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Category '${categoryToArchive!!.name}' will be archived and hidden from new records. Existing past records will keep this category name intact.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassOutlinedButton(
                            text = "Cancel",
                            onClick = { categoryToArchive = null },
                            modifier = Modifier.weight(1f)
                        )
                        VividButton(
                            text = "Archive",
                            onClick = {
                                storeViewModel.archiveCategory(
                                    category = categoryToArchive!!,
                                    onSuccess = { categoryToArchive = null }
                                )
                            },
                            containerColor = AlertRed,
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRowCard(
    category: StoreCategory,
    onRename: () -> Unit,
    onArchive: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "category_card_${category.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x265DD62C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = VividGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = category.name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onRename,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onArchive,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = "Archive",
                        tint = AlertRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    type: String,
    existingNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = DarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add ${if (type == "expense") "Expense" else "Income"} Category",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                GlassTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        localError = null
                    },
                    label = "Category Name *",
                    placeholder = "e.g. Packaging, Printing, Delivery",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "input_new_category_name"
                )

                if (localError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localError ?: "",
                        color = AlertRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    VividButton(
                        text = "Add Category",
                        onClick = {
                            val trimmed = name.trim()
                            if (trimmed.isBlank()) {
                                localError = "Category name is required"
                                return@VividButton
                            }
                            if (existingNames.contains(trimmed.lowercase())) {
                                localError = "A category with this name already exists"
                                return@VividButton
                            }
                            onConfirm(trimmed)
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameCategoryDialog(
    category: StoreCategory,
    existingNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (newName: String) -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var localError by remember { mutableStateOf<String?>(null) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = DarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Rename Category",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                GlassTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        localError = null
                    },
                    label = "New Category Name *",
                    modifier = Modifier.fillMaxWidth()
                )

                if (localError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localError ?: "",
                        color = AlertRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    VividButton(
                        text = "Save",
                        onClick = {
                            val trimmed = name.trim()
                            if (trimmed.isBlank()) {
                                localError = "Category name is required"
                                return@VividButton
                            }
                            if (trimmed.equals(category.name.trim(), ignoreCase = true)) {
                                onDismiss()
                                return@VividButton
                            }
                            if (existingNames.contains(trimmed.lowercase())) {
                                localError = "A category with this name already exists"
                                return@VividButton
                            }
                            onConfirm(trimmed)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Step 10: Store Notes Screen
 * Simple note: title (required), text, date (default today).
 * List with search, tap opens note.
 * Notice: Store notes do NOT affect account balances.
 */
@Composable
fun StoreNotesScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val notes by storeViewModel.notes.collectAsState()
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var noteToEdit by remember { mutableStateOf<StoreNote?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) {
            notes.sortedByDescending { it.date }
        } else {
            val q = searchQuery.trim().lowercase()
            notes.filter {
                it.title.lowercase().contains(q) || it.text.lowercase().contains(q)
            }.sortedByDescending { it.date }
        }
    }

    BackHandler {
        storeViewModel.navigateBackInBusiness()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            color = Color(0xF20F0F0F),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { storeViewModel.navigateBackInBusiness() },
                    modifier = Modifier.testTag("notes_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "দোকানের নোটসমূহ (Store Notes)" else "Store Notes",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Memos & reminders (does not affect balances)",
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )
                }
                VividButton(
                    text = "+ Add Note",
                    onClick = { isCreatingNew = true },
                    icon = Icons.Default.Add,
                    testTag = "btn_add_note"
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SearchGlassField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    label = "Search Notes",
                    placeholder = "Search by title or contents...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (filteredNotes.isEmpty()) {
                item {
                    PlainEmptyState(
                        message = if (searchQuery.isNotBlank())
                            "No notes match your search"
                        else
                            "No store notes saved. Tap '+ Add Note' to write shop reminders, vendor contacts, or to-do memos."
                    )
                }
            } else {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteRowCard(
                        note = note,
                        onClick = { noteToEdit = note }
                    )
                }
            }
        }
    }

    if (isCreatingNew) {
        AddEditNoteDialog(
            existingNote = null,
            onDismiss = { isCreatingNew = false },
            onConfirm = { id, title, text, date ->
                storeViewModel.saveNote(
                    id = id,
                    title = title,
                    text = text,
                    date = date,
                    onSuccess = { isCreatingNew = false }
                )
            },
            onDelete = null
        )
    }

    if (noteToEdit != null) {
        AddEditNoteDialog(
            existingNote = noteToEdit,
            onDismiss = { noteToEdit = null },
            onConfirm = { id, title, text, date ->
                storeViewModel.saveNote(
                    id = id,
                    title = title,
                    text = text,
                    date = date,
                    onSuccess = { noteToEdit = null }
                )
            },
            onDelete = {
                storeViewModel.deleteNote(
                    noteId = noteToEdit!!.id,
                    onSuccess = { noteToEdit = null }
                )
            }
        )
    }
}

@Composable
fun NoteRowCard(
    note: StoreNote,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        testTag = "note_card_${note.id}"
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x265DD62C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.StickyNote2,
                            contentDescription = null,
                            tint = VividGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = note.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = dateFormat.format(Date(note.date)),
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            if (note.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.text,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteDialog(
    existingNote: StoreNote?,
    onDismiss: () -> Unit,
    onConfirm: (id: String, title: String, text: String, date: Long) -> Unit,
    onDelete: (() -> Unit)?
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var text by remember { mutableStateOf(existingNote?.text ?: "") }
    var selectedDateMillis by remember { mutableLongStateOf(existingNote?.date ?: System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = DarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingNote == null) "New Store Note" else "Edit Note",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = AlertRed)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                GlassTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        localError = null
                    },
                    label = "Title (Required) *",
                    placeholder = "e.g. Supplier delivery schedules",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "input_note_title"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .clickable { showDatePickerDialog = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = VividGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Date: ${dateFormat.format(Date(selectedDateMillis))}",
                            color = TextPrimary,
                            fontSize = 13.5.sp
                        )
                    }
                    Text(
                        text = "Change",
                        color = VividGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = "Note Body",
                    placeholder = "Write details, reminders, phone numbers...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    singleLine = false,
                    testTag = "input_note_body"
                )

                if (localError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localError ?: "",
                        color = AlertRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    VividButton(
                        text = "Save Note",
                        onClick = {
                            val trimmed = title.trim()
                            if (trimmed.isBlank()) {
                                localError = "Note title is required"
                                return@VividButton
                            }
                            onConfirm(
                                existingNote?.id ?: "",
                                trimmed,
                                text.trim(),
                                selectedDateMillis
                            )
                        },
                        modifier = Modifier.weight(1.3f),
                        testTag = "btn_save_note"
                    )
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDateMillis = it
                    }
                    showDatePickerDialog = false
                }) {
                    Text("OK", color = VividGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
