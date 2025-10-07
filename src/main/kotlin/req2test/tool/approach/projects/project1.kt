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

    System.out.println()
    lateinit var data: MutableMap<String, Any?>
    data = HashMap()

    val entityCustomer = """
    Customer
        idCustomer | String | generated: UUID 36 characters [DB]
        fullName | String | It does not allow numbers and special characters; not null
        licenseNumber | String | not null; must match format XXXX-XX (7 digits: 4 digits, hyphen, 2 digits); not null
        licenseNumberExpirationDate | Date | generated: must be equals current date + 5 years
        createdAt | Date | generated: current date [DB]
        birthDate | Date | date less than the current date - 18 years; not null
        """.trimIndent()

    var rawRules = """        
    US1 | endpoint-create | Customer
    US2 | endpoint-get | Customer
    US3 | endpoint-delete | Customer
    US4 | endpoint-filter | Customer
    US4.fields | fullName, licenseNumber, birthDate, createdAt
        
        """.trimIndent()

    data["ListRawACRules"] = rawRules

    val validRawListEntities = ArrayList<String>()
    validRawListEntities.add(entityCustomer)
    data["ListRawEntities"] = validRawListEntities

    ApproachLevel.addAllLevels()
    levels.add("unit")

    OutputFolder.createTestFolderStructure("outputArtifacts/project1")

    val AIAdapter = ChatGPTAdapter(APIKeys.ChatGPT)
    var pipe = RulePipelineBuilder(data, null, AIAdapter).build()
    pipe.execute()

    val finalLocalDateTime = LocalDateTime.now()

    val delta: Duration = Duration.between(initLocalDateTime, finalLocalDateTime)

    println("Delta in seconds: ${delta.seconds}")
    println("Delta in ms: ${delta.toMillis()}")

    TestPlanGenerator.createFile("Delta in seconds: ${delta.seconds}", OutputFolder.basePath, "time.txt")

}