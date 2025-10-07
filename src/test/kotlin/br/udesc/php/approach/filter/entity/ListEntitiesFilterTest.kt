package br.udesc.php.approach.filter.entity

import req2test.tool.approach.core.FilterError
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.filter.entity.ListEntitiesFilter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ListEntitiesFilterTest {

    private lateinit var data: MutableMap<String, Any?>
    private lateinit var filter: ListEntitiesFilter
    private lateinit var entityA: String
    private lateinit var entityB: String
    private lateinit var entityC: String
    @BeforeEach
    fun setUp() {
        data = HashMap()
        filter = ListEntitiesFilter(data)
        entityA = """
            Customer
            idCustomer | Int | automatically generated incrementally by 1 [DB]
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
            idReservation | Int | automatically generated incrementally by 1 [DB]
            customer | %Customer | not null
            reservationDate | Date |  valid date greater than the current day
            reservationTime | Time | valid time between 08:00 and 18:00
            carType | list of String | (compact, air compact, executive, SUV)
            reservationCode | String | 6 randomly generated characters; unique [DB]
        """.trimIndent()

        entityC = """
            Triangulo
            A | Int | greater than 0
            B | Int | greater than 0
            C | Int | greater than 0
            VALIDATIONS:
            forma Triângulo | %Triangulo.A < ( %Triangulo.B + %Triangulo.C ) AND %Triangulo.B < ( %Triangulo.A + %Triangulo.C ) AND %Triangulo.C < ( %Triangulo.A + %Triangulo.B )
        """.trimIndent()

    }
    @Test
    fun shouldExecuteListEntitiesFilter(){
        val validRawListEntities = ArrayList<String>()
        validRawListEntities.add(entityA)
        validRawListEntities.add(entityB)
        validRawListEntities.add(entityC)
        data["ListRawEntities"] = validRawListEntities
        println(validRawListEntities)
        val res = filter.execute()
        val entities = data["ListEntities"] as MutableList<Entity>

        assertTrue(res, "Execution successfully")
        assertTrue(entities[1].attributeDependencies.contains("Customer"), "Dependencies must have entity: Customer")
    }

    @Test
    fun shouldFailWhenExecuteListEntitiesFilter(){
        val invalidRawListEntities = ArrayList<String>()
        invalidRawListEntities.add(entityB)
        data["ListRawEntities"] = invalidRawListEntities
        val exception = assertThrows<FilterError> {
            val res = filter.execute()
        }

        val entities = data["ListEntities"]

        assertEquals(null, entities, "Entities must be null")
        assertTrue(exception.message!!.contains("Entity Reservation depends on [Customer]"), "Error must contain: Entity Reservation depends on [Customer]")
        assertEquals(1, filter.criticalErrors().size, "Critical errors must contain one errors")
    }
}