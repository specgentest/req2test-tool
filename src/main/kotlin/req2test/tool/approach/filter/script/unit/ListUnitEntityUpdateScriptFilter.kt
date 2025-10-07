package req2test.tool.approach.filter.script.unit

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.TestArchitecture
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.*

class ListUnitEntityUpdateScriptFilter (data: MutableMap<String, Any?>) : Filter(data, null) {
    override fun filterKeyInput(): String {
        return "ListEntities"
    }

    override fun filterName(): String {
        return "ListUnitEntityUpdateScriptFilter"
    }

    override fun isMandatoryFilter(): Boolean {
        return false
    }

    override fun filterKeyOutput(): String {
        return "ListUnitEntityUpdateScript"
    }

    fun getInputs(entity: Entity): List<Attribute> {
        return entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }
    }

    fun createSteps(inputs: List<Attribute>, entity: Entity): List<String> {
        val steps = ArrayList<String>()
        steps.addAll(inputs.map { "Inform  ${it.name}" })
        steps.add("Update the existing instance of ${entity.name} with new values informed")
        steps.add("Call validate() method in ${entity.name}")
        return steps
    }

    fun getInputTechniques(): List<InputTechnique>{
        val inputTechniques = ArrayList<InputTechnique>()
        inputTechniques.add(InputTechnique.EQUIVALENCE_PARTITIONING)
        return inputTechniques
    }
    override fun compute(input: Any): Any? {
        val entities = input as List<Entity>
        val scripts = ArrayList<Script>()

        //Usado para gerar o arquivo de saída
        var textScript = ""
        var count = 1


        entities.forEach { entity ->
            val objective = "Find faults in validate() method int the ${entity.name} class after updating attributes"
            val preconditions = ArrayList<String>()
            preconditions.add("There is an existing instance of $entity to be updated")
            val inputs = getInputs(entity)
            val steps = createSteps(inputs, entity)
            val inputTechniques = getInputTechniques()
            val location = "validate() method int the ${entity.name} class"
            val testArchitecture = TestArchitecture.cleanArchitecture(ruleAction = "validate", entity = entity, inputs = inputs)
            val script = Script(Level.UNIT, objective, location, preconditions, steps, Technique.BLACKBOX, inputTechniques, inputs, entity)
            script.testArchitecture = testArchitecture
            scripts.add(script)



            textScript += "SCRIPT-${count++}\t\t\t\t\t| Update of ${entity.name}\n"
            textScript += "Script objective\t\t\t\t| ${script.objective}\n"
            textScript += "Location\t\t\t\t\t| ${script.location}\n"
            textScript += "Steps\t\t\t\t\t\t| ${script.steps}\n"
            textScript += "Level\t\t\t\t\t\t| ${script.level.value}\n"
            textScript += "Technique\t\t\t\t\t| ${script.technique.value}\n"
            textScript += "Selection Criteria\t\t\t\t| ${script.inputTechniques.map { it.value }}\n\n"

        }
        TestPlanGenerator.createFile(textScript, "outputArtifacts/", "UnitUpdateScripts.txt")
        return scripts
    }
}
