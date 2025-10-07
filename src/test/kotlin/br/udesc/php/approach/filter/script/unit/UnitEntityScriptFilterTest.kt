package br.udesc.php.approach.filter.script.unit

import req2test.tool.approach.entity.InputTechnique
import req2test.tool.approach.entity.Script
import req2test.tool.approach.entity.Technique
import req2test.tool.approach.filter.entity.ListEntitiesFilter
import req2test.tool.approach.filter.script.unit.ListUnitEntityScriptFilter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UnitEntityScriptFilterTest {
    private lateinit var data: MutableMap<String, Any?>
    private lateinit var filter: ListEntitiesFilter
    private lateinit var entityA: String
    @BeforeEach
    fun setUp() {
        data = HashMap()
        filter = ListEntitiesFilter(data)
        entityA = """
            Triangle
            idTriangle | Int | generated: incrementally by 1 [DB]
            A | Int | greater than 0
            B | Int | greater than 0
            C | Int | greater than 0
            VALIDATIONS:
            form Triangle | %Triangle.A < ( %Triangle.B + %Triangle.C ) AND %Triangle.B < ( %Triangle.A + %Triangle.C ) AND %Triangle.C < ( %Triangle.A + %Triangle.B )
        """.trimIndent()

        val validRawListEntities = ArrayList<String>()
        validRawListEntities.add(entityA)
        data["ListRawEntities"] = validRawListEntities
        filter.execute()
    }
    @Test
    fun shouldExecuteUnitEntityScriptFilter(){
        val listUnitEntityScriptFilter = ListUnitEntityScriptFilter(data)
        listUnitEntityScriptFilter.execute()
        val scripts = data["ListUnitEntityScript"] as List<Script>

        val script = scripts[0]
        assertEquals("Find faults when creating a new Triangle", script.objective, "Objective")
        assertEquals(Technique.BLACKBOX, script.technique, "Technique")
        assertEquals("Triangle constructor", script.location, "Location")
        assertEquals(InputTechnique.EQUIVALENCE_PARTITIONING, script.inputTechniques[0], "Input Technique")
        assertEquals(3, script.inputs.size, "3 inputs")
    }

}