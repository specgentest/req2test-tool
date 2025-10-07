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

    val entityCustomer = """
        Customer
        idCustomer | Int | generated: incrementally by 1 [DB]
        fullName | String | It does not allow numbers and special characters; not null
        birthDate | Date | date less than the current date - 18 years; not null
        """.trimIndent()


    val entityAccount = """
        Account
        idAccount | Int | generated: incrementally by 1 [DB]
        customer | %Customer | must be not null
        agency | Int | must have 3 digits
        accountNumber | Int | must have 4 digits
        balance | Float | greater than or equals to zero
        accountLimit | Float | generated: random number between 0 and 500
        pin | String | generated: 4 randomly characters
        """.trimIndent()

    var rawRules = """
        US1 | endpoint-create | Customer
        US2 | endpoint-create | Account
        US3 | endpoint-filter | Account
        US3.fields | accountNumber, agency
        
        US4 | endpoint | withdraw
        US4.data | number | Int | greater than 0
        US4.data | account | %Account | where ${'$'}number equals to ${'$'}account.accountNumber
        US4.data | amount | Float | greater than 0
        US4.data | pin | String | not null
        US4.data | accountOutput | %Account | where ${'$'}accountOutput equals to ${'$'}account; ${'$'}accountOutput.balance = ${'$'}accountOutput.balance - ${'$'}amount
        US4.data | finalBalance | Float | where ${'$'}finalBalance = ${'$'}accountOutput.balance
        US4.input | number, amount, pin
        US4.validation | if ${'$'}amount is less than or equal to zero, then error message: negative amount
        US4.validation | if ${'$'}amount is less than or equal to ${'$'}account.balance + ${'$'}account.accountLimit, then error message: insufficient balance
        US4.validation | if ${'$'}pint is not equals to ${'$'}account.pin, then error message: incorrect pin
        US4.output | finalBalance
        US4.dbinit | account
        """.trimIndent()

    data["ListRawACRules"] = rawRules

    val validRawListEntities = ArrayList<String>()
    validRawListEntities.add(entityCustomer)
    validRawListEntities.add(entityAccount)
    data["ListRawEntities"] = validRawListEntities

    OutputFolder.createTestFolderStructure("outputArtifacts/project3")

    ApproachLevel.addAllLevels()
    levels.add("unit")

    val AIAdapter = ChatGPTAdapter(APIKeys.ChatGPT)
    var pipe = RulePipelineBuilder(data, null, AIAdapter).build()
    pipe.execute()

    val finalLocalDateTime = LocalDateTime.now()

    val delta: Duration = Duration.between(initLocalDateTime, finalLocalDateTime)

    println("Delta in seconds: ${delta.seconds}")
    println("Delta in ms: ${delta.toMillis()}")

    TestPlanGenerator.createFile("Delta in seconds: ${delta.seconds}", OutputFolder.basePath, "time.txt")


}