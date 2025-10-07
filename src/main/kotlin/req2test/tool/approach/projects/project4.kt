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
            idCustomer | String | generated: UUID 36 characters [DB]
            fullName | String | It does not allow numbers and special characters; not null
            licenseNumber | String | It does not allow numbers and special characters; not null
            licenseNumberExpirationDate | Date | not null
            birthDate | Date | date less than the current date - 18 years; not null
        """.trimIndent()

    val entityReservation = """
            Reservation
            idReservation | String | generated: UUID 36 characters [DB]
            customer | %Customer | must be not null; ${'$'}customer.licenseNumberExpirationDate must be greater than current date
            reservationDate | Date |  must be not null
            carType | String | value in (compact, air compact, executive, SUV)
            status | String | generated: must be equals to OPEN; value in (OPEN, CLOSED)
            reservationType | String | value in (STANDARD, SPECIAL)
            reservationCode | String | generated: 6 randomly characters
        """.trimIndent()

    var rawRules = """        
    US1 | endpoint-create | Customer

    US2 | endpoint-create | Reservation

    US3 | endpoint-create | Reservation
    US3.input | customer, reservationDate, carType
    US3.validation | if ${'$'}customer.birthDate and ${'$'}reservationDate is not the same day and month, then error message: birthDate and reservationDate is not the same day and month
    US3.validation | ${'$'}reservationType is always equals to SPECIAL

    US4 | endpoint | Close Reservation
    US4.data | reservation | %Reservation | not null; where ${'$'}reservation.reservationDate < current date
    US4.data | codeReservation | String | where ${'$'}codeReservation equals to ${'$'}reservation.reservationCode
            US4.data | reservationOutput | %Reservation | where ${'$'}reservationOutput.reservationDate equals to ${'$'}reservation.reservationDate; where ${'$'}reservationOutput and ${'$'}reservation must match exactly on: ${'$'}reservation.idReservation, ${'$'}reservation.customer (all attributes), ${'$'}reservation.reservationDate, ${'$'}reservation.carType, ${'$'}reservation.reservationType, ${'$'}reservation.reservationCode, and must differ only on: ${'$'}reservation.status: ${'$'}reservationOutput.status = "CLOSED", ${'$'}reservation.status = "OPEN"
    US4.input | codeReservation
    US4.validation | if ${'$'}reservation.status == CLOSED, then error message: reservation already CLOSED
    US4.validation | if ${'$'}reservation.reservationDate > current date, then error message: reservationDate must be less than current date
    US4.output | reservationOutput
    US4.dbinit | reservation      
      
        """.trimIndent()

    data["ListRawACRules"] = rawRules

    val validRawListEntities = ArrayList<String>()
    validRawListEntities.add(entityCustomer)
    validRawListEntities.add(entityReservation)
    data["ListRawEntities"] = validRawListEntities

    ApproachLevel.addAllLevels()
    levels.add("unit")

    OutputFolder.createTestFolderStructure("outputArtifacts/project4")

    val AIAdapter = ChatGPTAdapter(APIKeys.ChatGPT)
    var pipe = RulePipelineBuilder(data, null, AIAdapter).build()
    pipe.execute()

    val finalLocalDateTime = LocalDateTime.now()

    val delta: Duration = Duration.between(initLocalDateTime, finalLocalDateTime)

    println("Delta in seconds: ${delta.seconds}")
    println("Delta in ms: ${delta.toMillis()}")

    TestPlanGenerator.createFile("Delta in seconds: ${delta.seconds}", OutputFolder.basePath, "time.txt")



}