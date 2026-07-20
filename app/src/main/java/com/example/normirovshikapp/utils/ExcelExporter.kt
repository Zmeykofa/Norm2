package com.example.normirovshikapp.utils

import com.example.normirovshikapp.data.DayEntity
import com.example.normirovshikapp.data.OperationEntity
import com.example.normirovshikapp.data.StaffEntity
import com.example.normirovshikapp.data.ToolEntity
import com.example.normirovshikapp.data.EquipmentEntity
import com.example.normirovshikapp.data.MaterialEntity
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.*

class ExcelExporter {

    init {
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl")
    }

    fun generateExcelWorkbook(
        day: DayEntity,
        operations: List<OperationEntity>,
        staffList: List<StaffEntity> = emptyList(),
        toolsList: List<ToolEntity> = emptyList(),
        equipmentList: List<EquipmentEntity> = emptyList(),
        materialsList: List<MaterialEntity> = emptyList()
    ): Workbook {
        val workbook = XSSFWorkbook()

        // --- Лист 1: Паспорт дня ---
        val sheetPassport = workbook.createSheet("Паспорт")
        val boldFont = workbook.createFont().apply { bold = true }
        val boldStyle = workbook.createCellStyle().apply { setFont(boldFont) }

        var rowIndex = 0
        fun addRow(label: String, value: String) {
            val row = sheetPassport.createRow(rowIndex++)
            row.createCell(0).apply {
                setCellValue(label)
                cellStyle = boldStyle
            }
            row.createCell(1).setCellValue(value)
        }

        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        addRow("Название дня", day.name)
        addRow("Дата создания", sdf.format(Date(day.createdAt)))
        addRow("Место проведения", day.location)
        addRow("Объект", day.objectName)
        addRow("Организация", day.organization)
        addRow("Вид работ", day.workType)
        addRow("Техпроцесс", day.processName)
        addRow("Документы", day.docsInfo)
        addRow("Бригада №", day.brigadeNumber)
        addRow("Бригадир", day.brigadeLeader)
        
        sheetPassport.createRow(rowIndex++) // пустая строка
        
        val workersStr = if (day.workersList.isNotBlank()) {
            day.workersList
        } else {
            staffList.joinToString(", ") { it.displayName() }
        }
        addRow("Исполнители (список шаблонов)", formatWorkersForExcel(workersStr))

        val toolsStr = if (day.toolsList.isNotBlank()) {
            day.toolsList
        } else {
            toolsList.joinToString(", ") { it.name }
        }
        addRow("Инструменты (список шаблонов)", toolsStr)

        val equipmentStr = if (day.equipmentList.isNotBlank()) {
            day.equipmentList
        } else {
            equipmentList.joinToString(", ") { it.displayName() }
        }
        addRow("Техника (список шаблонов)", formatEquipmentForExcel(equipmentStr))
        addRow("Машинисты (список шаблонов)", formatMachinistsForExcel(equipmentStr))

        val materialsStr = if (day.materialsList.isNotBlank()) {
            day.materialsList
        } else {
            materialsList.joinToString(", ") { it.name }
        }
        addRow("Материалы (список шаблонов)", materialsStr)

        sheetPassport.setColumnWidth(0, 6000) // ~20 символов
        sheetPassport.setColumnWidth(1, 10000) // ~40 символов



        // --- Лист 2: Хронометраж ---
        val sheetOps = workbook.createSheet("Хронометраж")
        val headerRow = sheetOps.createRow(0)
        val headers = listOf("Название", "Начало", "Конец", "Люди", "Исполнители", "Кол-во рабочих", "Инструменты", "Техника", "Кол-во машин", "Машинисты", "Материалы", "Заметки")
        
        headers.forEachIndexed { index, title ->
            headerRow.createCell(index).apply {
                setCellValue(title)
                cellStyle = boldStyle
            }
        }

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        operations.sortedBy { it.startEpoch }.forEachIndexed { index, op ->
            val row = sheetOps.createRow(index + 1)
            
            row.createCell(0).setCellValue(op.name)
            row.createCell(1).setCellValue(timeFormat.format(Date(op.startEpoch)))
            
            val stopText = if (op.stopEpoch != null) timeFormat.format(Date(op.stopEpoch)) else "Активна"
            row.createCell(2).setCellValue(stopText)

            row.createCell(3).setCellValue(op.people.toDouble())
            row.createCell(4).setCellValue(formatWorkersForExcel(op.workers))
            
            val workersCount = op.workers.split(Regex(",(?![^(]*\\))")).count { it.isNotBlank() }
            row.createCell(5).setCellValue(workersCount.toDouble())
            
            row.createCell(6).setCellValue(op.tools)
            
            val equipmentItems = op.equipment.split(Regex(",(?![^\\[]*\\])")).filter { it.isNotBlank() }.map { it.trim() }
            val equipmentCount = equipmentItems.size
            val equipmentNames = formatEquipmentForExcel(op.equipment)
            val machinistsList = formatMachinistsForExcel(op.equipment)

            row.createCell(7).setCellValue(equipmentNames)
            row.createCell(8).setCellValue(equipmentCount.toDouble())
            row.createCell(9).setCellValue(machinistsList)
            
            row.createCell(10).setCellValue(op.materials)
            row.createCell(11).setCellValue(op.notes)
        }

        // --- Автоширина для колонок ---
        // Устанавливаем ширину колонок вручную во избежание ошибки AWT on Android
        val columnWidths = listOf(
            4000, 3000, 3000,
            2000, 6000, 3000, 6000, 6000, 3000, 6000, 6000, 8000
        )
        for (i in headers.indices) {
            if (i < columnWidths.size) {
                sheetOps.setColumnWidth(i, columnWidths[i])
            } else {
                sheetOps.setColumnWidth(i, 4000)
            }
        }

        return workbook
    }

    private fun formatWorkersForExcel(workersStr: String): String {
        if (workersStr.isBlank()) return ""
        val workerStrings = workersStr.split(Regex(",(?![^(]*\\))")).map { it.trim() }.filter { it.isNotBlank() }
        if (workerStrings.isEmpty()) return ""

        val counts = mutableMapOf<String, Int>()
        val regexParens = Regex("\\(([^)]+)\\)")

        for (w in workerStrings) {
            var position = ""
            var grade = ""

            val matchResult = regexParens.find(w)
            if (matchResult != null) {
                val inner = matchResult.groupValues[1]
                val details = inner.split(",").map { it.trim() }
                if (details.size >= 2) {
                    position = details[0]
                    grade = details[1]
                } else if (details.isNotEmpty()) {
                    val valStr = details[0]
                    if (valStr.any { it.isDigit() }) {
                        grade = valStr
                    } else {
                        position = valStr
                    }
                }
            } else {
                if (w.contains(",")) {
                    val details = w.split(",").map { it.trim() }
                    position = details[0]
                    grade = details[1]
                } else {
                    val trimmed = w.trim()
                    if (trimmed.any { it.isDigit() }) {
                        grade = trimmed
                    } else {
                        position = trimmed
                    }
                }
            }

            var posFormatted = position.trim()
            if (posFormatted.isNotEmpty()) {
                posFormatted = posFormatted.substring(0, 1).uppercase(Locale.getDefault()) + posFormatted.substring(1)
            }

            var gradeFormatted = grade.trim()
            if (gradeFormatted.isNotEmpty()) {
                val gradeClean = gradeFormatted.replace(Regex("\\s*р\\.?$"), "")
                gradeFormatted = gradeClean + "р."
            }

            val key = when {
                posFormatted.isNotEmpty() && gradeFormatted.isNotEmpty() -> "$posFormatted $gradeFormatted"
                posFormatted.isNotEmpty() -> posFormatted
                gradeFormatted.isNotEmpty() -> gradeFormatted
                else -> "Сотрудник"
            }

            counts[key] = (counts[key] ?: 0) + 1
        }

        return counts.entries.joinToString(", ") { "${it.key} - ${it.value} чел." }
    }

    private fun formatEquipmentForExcel(equipmentStr: String): String {
        if (equipmentStr.isBlank()) return ""
        val eqStrings = equipmentStr.split(Regex(",(?![^\\[]*\\])")).map { it.trim() }.filter { it.isNotBlank() }
        if (eqStrings.isEmpty()) return ""

        val counts = mutableMapOf<String, Int>()
        for (e in eqStrings) {
            val name = e.substringBefore(" [").substringBefore("=").trim()
            var nameFormatted = name
            if (nameFormatted.isNotEmpty()) {
                nameFormatted = nameFormatted.substring(0, 1).uppercase(Locale.getDefault()) + nameFormatted.substring(1)
            } else {
                nameFormatted = "Техника"
            }
            counts[nameFormatted] = (counts[nameFormatted] ?: 0) + 1
        }

        return counts.entries.sortedBy { it.key }.joinToString(", ") { "${it.key} - ${it.value} шт." }
    }

    private fun formatMachinistsForExcel(equipmentStr: String): String {
        if (equipmentStr.isBlank()) return ""
        val eqStrings = equipmentStr.split(Regex(",(?![^\\[]*\\])")).map { it.trim() }.filter { it.isNotBlank() }
        if (eqStrings.isEmpty()) return ""

        val counts = mutableMapOf<String, Int>()
        val regexParens = Regex("\\[([^\\]]+)\\]")

        for (e in eqStrings) {
            var position = ""
            var grade = ""

            val matchResult = regexParens.find(e)
            if (matchResult != null) {
                val inner = matchResult.groupValues[1]
                val details = inner.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                
                val isFio = { str: String ->
                    str.contains(Regex("[А-ЯA-Z]\\.[А-ЯA-Z]?\\.")) || (str.split(" ").size > 1 && str.any { it.isUpperCase() })
                }

                if (details.size >= 3) {
                    position = details[1]
                    grade = details[2]
                } else if (details.size == 2) {
                    val hasDigit0 = details[0].any { it.isDigit() }
                    val hasDigit1 = details[1].any { it.isDigit() }
                    if (hasDigit0) {
                        grade = details[0]
                        if (!isFio(details[1])) position = details[1]
                    } else if (hasDigit1) {
                        grade = details[1]
                        if (!isFio(details[0])) position = details[0]
                    } else {
                        if (!isFio(details[0])) position = details[0]
                        if (!isFio(details[1])) position = details[1]
                    }
                } else if (details.size == 1) {
                    val valStr = details[0]
                    if (valStr.any { it.isDigit() }) {
                        grade = valStr
                    } else if (!isFio(valStr)) {
                        position = valStr
                    }
                }
            }

            var posFormatted = position.trim()
            if (posFormatted.isNotEmpty()) {
                posFormatted = posFormatted.substring(0, 1).uppercase(Locale.getDefault()) + posFormatted.substring(1)
            }

            var gradeFormatted = grade.trim()
            if (gradeFormatted.isNotEmpty()) {
                val gradeClean = gradeFormatted.replace(Regex("\\s*р\\.?$"), "")
                gradeFormatted = gradeClean + "р."
            }

            val key = when {
                posFormatted.isNotEmpty() && gradeFormatted.isNotEmpty() -> "$posFormatted $gradeFormatted"
                posFormatted.isNotEmpty() -> posFormatted
                gradeFormatted.isNotEmpty() -> "Машинист $gradeFormatted"
                else -> "Машинист"
            }

            counts[key] = (counts[key] ?: 0) + 1
        }

        return counts.entries.joinToString(", ") { "${it.key} - ${it.value} чел." }
    }
}
