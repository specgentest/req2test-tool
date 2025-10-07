package req2test.tool.approach.processor.generator.file

import java.io.File
import java.io.PrintWriter
import org.apache.poi.xwpf.usermodel.*
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.TestPlan
import java.io.FileOutputStream
import java.math.BigInteger

class TestPlanGenerator {

    fun printToWordFile(testPlan: TestPlan) {
        val scripts = testPlan.scripts

        val document = XWPFDocument()

        val table = document.createTable()

        var count = 1
        var ctCount = 1

        // Cores personalizadas
        val orange = "ff6400"      // Cor para Scripts (laranja)
        val lightOrange = "ffa500" // Cor clara para Scripts (laranja claro)
        val lightBlue = "9fd7f9"   // Cor para Casos de Testes (azul claro)
        val lightBlue2 = "cfebfc"  // Cor azul mais claro

        scripts.forEach { script ->
            val entity = script.reference as Entity

            addTableRowMerged(table, "SCRIPT-${count++}: Creation of ${entity.name}", orange)

            // Outras linhas com detalhes do Script
            addTableRow(table, "Script Objective", script.objective, lightOrange)
            addTableRow(table, "Location", script.location, lightOrange)
            addTableRow(table, "Steps", script.steps.toString(), lightOrange)
            addTableRow(table, "Level", script.level.value, lightOrange)
            addTableRow(table, "Technique", script.technique.value, lightOrange)
            addTableRow(table, "Selection Criteria", script.inputTechniques.joinToString(", ") { it.value }, lightOrange)

            // Test cases
            script.testCases.forEach { tc ->
                val tcDescription = tc.dataTest.className
                addTableRowMerged(table, "TC-${ctCount++}: ${tcDescription}", lightBlue)
                addTableRow(table, "Input", tc.dataTest.input?.map { "${it.key}: ${it.value}" }?.joinToString(", ").toString(), lightBlue2)
                addTableRow(table, "Expected Result", tc.expectedResult, lightBlue2)
            }

            // Test cases with attributes generated
            script.testCasesAttributeGenerated.forEach { tc ->
                val tcDescription = if (tc.dataTest.isValidInput)
                    "Must create an instance of ${entity.name}"
                else
                    tc.dataTest.className

                addTableRow(table, "TC-${ctCount++}: ${tcDescription}", "", lightBlue)
                addTableRow(table, "Input", tc.dataTest.input?.map { "${it.key}: ${it.value}" }?.joinToString(", ").toString(), lightBlue2)
                addTableRow(table, "Expected Result", tc.expectedResult, lightBlue2)
            }
        }

        val docxFilePath = "outputArtifacts/${countDoc++} - testplan.docx"
        val pdfFolderPath = "outputArtifacts"

        val out = FileOutputStream(docxFilePath)
        document.write(out)
        out.close()
        convertDocxToPdf(docxFilePath, pdfFolderPath)
    }


    fun addTableRow(table: XWPFTable, column1: String, column2: String, color: String) {
        val row = table.createRow()

        if (row.getCell(0) == null) {
            row.createCell()
        }
        if (row.getCell(1) == null) {
            row.createCell()
        }

        row.getCell(0).setText(column1)
        row.getCell(1).setText(column2)

        setCellColor(row.getCell(0), color)
        setCellColor(row.getCell(1), color)
    }

    fun addTableRowMerged(table: XWPFTable, column1: String, color: String) {
        val row = table.createRow()

        if (row.getCell(0) == null) {
            row.createCell()
        }

        row.getCell(0).setText(column1)

        setCellColor(row.getCell(0), color)

        val cellProperties: CTTcPr = row.getCell(0).ctTc.addNewTcPr()
        cellProperties.addNewGridSpan().`val` = BigInteger.valueOf(2)
    }

    fun setCellColor(cell: XWPFTableCell, color: String) {
        cell.getCTTc().addNewTcPr().addNewShd().setFill(color);
    }


    fun convertDocxToPdf(docxFilePath: String, pdfFolderPath: String) {

        val docxFile = File(docxFilePath).absolutePath
        val outputDir = File(pdfFolderPath).absolutePath

        val libreOfficePath = "libreoffice"

        val command = "$libreOfficePath --headless --convert-to pdf --outdir ${outputDir} $docxFile"


        val process = ProcessBuilder(command.split(" "))
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        if (exitCode == 0) {
            println("Ok")
        } else {
            println("Error")
        }
    }

    companion object {

        var countDoc = 1
        fun createFile(content: String, folderPath: String, fileName: String, presentCount: Boolean=false) {
            var filePath = "${folderPath}${fileName}"
            if(presentCount)
                filePath = "${folderPath}${countDoc++} - ${fileName}"
            val file = File(filePath)
            val writer = PrintWriter(file)
            writer.println(content)
            writer.close()
            println("Content saved in $filePath")

        }
    }
}