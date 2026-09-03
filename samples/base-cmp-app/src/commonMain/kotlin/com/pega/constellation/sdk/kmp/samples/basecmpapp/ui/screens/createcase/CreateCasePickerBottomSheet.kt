package com.pega.constellation.sdk.kmp.samples.basecmpapp.ui.screens.createcase

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pega.constellation.sdk.kmp.samples.basecmpapp.data.CaseType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCasePickerBottomSheet(
    viewModel: CreateCaseViewModel,
    onCreateCase: (CaseType) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    if (!state.pickerVisible) return

    val filteredCaseTypes = state.caseTypes.filter { caseType ->
        val query = state.query.trim()
        query.isEmpty() || caseType.label.contains(query, ignoreCase = true) ||
            caseType.className.contains(query, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = viewModel::closePicker,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 16.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Create another case",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "Choose a case type available in this environment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    onClick = viewModel::closePicker,
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search case types") },
                singleLine = true,
                enabled = !state.loading,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when {
                    state.loading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    state.errorMessage != null -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Unable to load case types.",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = state.errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = viewModel::retry) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Text("Retry", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }

                    state.caseTypes.isEmpty() -> {
                        Text(
                            text = "No available case types.",
                            modifier = Modifier.padding(vertical = 20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    filteredCaseTypes.isEmpty() -> {
                        Text(
                            text = "No case types match your search.",
                            modifier = Modifier.padding(vertical = 20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(filteredCaseTypes, key = { it.className }) { caseType ->
                                CaseTypeRow(
                                    caseType = caseType,
                                    selected = state.selectedCaseType == caseType,
                                    onClick = { viewModel.selectCaseType(caseType) },
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = viewModel::closePicker) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        state.selectedCaseType?.let {
                            viewModel.closePicker()
                            onCreateCase(it)
                        }
                    },
                    enabled = state.selectedCaseType != null && !state.loading,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Create case", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun CaseTypeRow(
    caseType: CaseType,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (selected) Icons.Default.Check else Icons.Default.Add,
            contentDescription = if (selected) "Selected" else null,
            modifier = Modifier.size(20.dp),
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = caseType.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            Text(
                text = caseType.className,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
