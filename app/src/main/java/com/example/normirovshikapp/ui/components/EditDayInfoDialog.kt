package com.example.normirovshikapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.normirovshikapp.data.DayEntity

@Composable
fun EditDayInfoDialog(day: DayEntity, onDismiss: () -> Unit, onSave: (DayEntity) -> Unit) {
    var location by remember { mutableStateOf(day.location) }
    var objectName by remember { mutableStateOf(day.objectName) }
    var organization by remember { mutableStateOf(day.organization) }
    var workType by remember { mutableStateOf(day.workType) }
    var processName by remember { mutableStateOf(day.processName) }
    var docsInfo by remember { mutableStateOf(day.docsInfo) }
    var brigadeNumber by remember { mutableStateOf(day.brigadeNumber) }
    var brigadeLeader by remember { mutableStateOf(day.brigadeLeader) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать общую информацию") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Место проведения") },
                        colors = gazTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = objectName,
                        onValueChange = { objectName = it },
                        label = { Text("Наименование объекта") },
                        colors = gazTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = organization,
                        onValueChange = { organization = it },
                        label = { Text("Строительная организация") },
                        colors = gazTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = workType,
                        onValueChange = { workType = it },
                        label = { Text("Вид работ") },
                        colors = gazTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = processName,
                        onValueChange = { processName = it },
                        label = { Text("Наименование техпроцесса") },
                        colors = gazTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = docsInfo,
                        onValueChange = { docsInfo = it },
                        label = { Text("Документы") },
                        colors = gazTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = brigadeNumber,
                        onValueChange = { brigadeNumber = it },
                        label = { Text("Бригада №") },
                        colors = gazTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = brigadeLeader,
                        onValueChange = { brigadeLeader = it },
                        label = { Text("Бригадир") },
                        colors = gazTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    day.copy(
                        location = location,
                        objectName = objectName,
                        organization = organization,
                        workType = workType,
                        processName = processName,
                        docsInfo = docsInfo,
                        brigadeNumber = brigadeNumber,
                        brigadeLeader = brigadeLeader
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
