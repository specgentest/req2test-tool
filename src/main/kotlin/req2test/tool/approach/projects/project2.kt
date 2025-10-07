package req2test.tool.approach.projects

import req2test.tool.approach.core.APIKeys
import req2test.tool.approach.core.ApproachLevel
import req2test.tool.approach.core.ApproachLevel.Companion.levels
import req2test.tool.approach.core.OutputFolder
import req2test.tool.approach.pipeline.RulePipelineBuilder
import req2test.tool.approach.processor.adapter.ChatGPTAdapter
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import java.time.Duration
import java.time.LocalDateTime

fun main() {
    val initLocalDateTime = LocalDateTime.now()
    lateinit var data: MutableMap<String, Any?>
    data = HashMap()

    val entityEmployee = """
    Employee
        idEmployee | Int | generated: incrementally by 1 [DB]
        fullName | String | It does not allow numbers and special characters; not null
        hiringDate | Date | must be a date before or equal to the current date; not null
        role | String | must be one of (TECHNICIAN, SUPPORT, MANAGER); not null
        """.trimIndent()

    val entitySupportRequest = """
    SupportRequest
        idSupportRequest | Int | generated: incrementally by 1 [DB]
        employee | %Employee | not null
        description | String | not null; minimum 20 characters
        requestDate | Date | generated: must be equal to current date
        status | String | must be one of (OPEN, IN_PROGRESS, CLOSED)
        """.trimIndent()

    var rawRules = """        
    US1 | endpoint-create | Employee
    US2 | endpoint-create | SupportRequest
    US3 | endpoint-update | SupportRequest
    US4 | endpoint-filter | SupportRequest
    US4.fields | status
        """.trimIndent()

    data["ListRawACRules"] = rawRules

    val validRawListEntities = ArrayList<String>()
    validRawListEntities.add(entityEmployee)
    validRawListEntities.add(entitySupportRequest)
    data["ListRawEntities"] = validRawListEntities

    ApproachLevel.addAllLevels()
    levels.add("unit")

    OutputFolder.createTestFolderStructure("outputArtifacts/project2")

    val AIAdapter = ChatGPTAdapter(APIKeys.ChatGPT)
    var pipe = RulePipelineBuilder(data, null, AIAdapter).build()
    pipe.execute()

    val finalLocalDateTime = LocalDateTime.now()

    val delta: Duration = Duration.between(initLocalDateTime, finalLocalDateTime)

    println("Delta in seconds: ${delta.seconds}")
    println("Delta em ms: ${delta.toMillis()}")

    TestPlanGenerator.createFile("Delta in seconds: ${delta.seconds}", OutputFolder.basePath, "time.txt")


}