package br.udesc.php.approach.filter

import req2test.tool.approach.entity.Entity
import req2test.tool.approach.filter.entity.ListEntitiesFilter
import org.junit.jupiter.api.BeforeEach

abstract class BaseTest {
    protected lateinit var data: MutableMap<String, Any?>
    protected lateinit var filter: ListEntitiesFilter
    protected lateinit var entityA: String
    protected lateinit var entityB: String
    protected lateinit var entityC: String
    protected lateinit var entities: List<Entity>
    @BeforeEach
    fun setUp() {
        data = HashMap()
        filter = ListEntitiesFilter(data)
        entityA = """
            Customer
            idCustomer | Int | generated: incrementally by 1 [DB]
            fullName | String | It does not allow numbers and special characters; not null
            CNH | String | only numbers; not null
            CNHExpirationDate | Date | valid date; not null
            birthDate | Date | valid date less than the current year; not null
            dependentCustomer | %Customer | can be null
            VALIDATIONS:
            validate CNH is expired | %Customer.CNHExpirationDate < current date
            validate CNH is one month to expire | %Customer.CNHExpirationDate > current date and  %Customer.CNHExpirationDate < current date + 1 month
        """.trimIndent()

        entityB = """
            Reservation
            idReservation | Int | generated: incrementally by 1 [DB]
            customer | %Customer | not null
            reservationDate | Date |  valid date greater than the current day
            reservationTime | Time | valid time between 08:00 and 18:00
            carType | list of String | (compact, air compact, executive, SUV)
            reservationCode | String | generated: 6 randomly characters; unique [DB]
        """.trimIndent()

        entityC = """
            Triangle
            A | Int | greater than 0
            B | Int | greater than 0
            C | Int | greater than 0
            VALIDATIONS:
            forma Triangle | %Triangle.A < ( %Triangle.B + %Triangle.C ) AND %Triangle.B < ( %Triangle.A + %Triangle.C ) AND %Triangle.C < ( %Triangle.A + %Triangle.B )
        """.trimIndent()

        val validRawListEntities = ArrayList<String>()
        validRawListEntities.add(entityA)
        validRawListEntities.add(entityB)
        validRawListEntities.add(entityC)
        data["ListRawEntities"] = validRawListEntities
        filter.execute()
        entities = data["ListEntities"] as List<Entity>

    }

}