package com.example.normirovshikapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
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
            LazyColumn {
                item {
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Место проведения") })
                    OutlinedTextField(value = objectName, onValueChange = { objectName = it }, label = { Text("Наименование объекта") })
                    OutlinedTextField(value = organization, onValueChange = { organization = it }, label = { Text("Строительная организация") })
                    OutlinedTextField(value = workType, onValueChange = { workType = it }, label = { Text("Вид работ") })
                    OutlinedTextField(value = processName, onValueChange = { processName = it }, label = { Text("Наименование техпроцесса") })
                    OutlinedTextField(value = docsInfo, onValueChange = { docsInfo = it }, label = { Text("Документы") })
                    OutlinedTextField(value = brigadeNumber, onValueChange = { brigadeNumber = it }, label = { Text("Бригада №") })
                    OutlinedTextField(value = brigadeLeader, onValueChange = { brigadeLeader = it }, label = { Text("Бригадир") })
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
