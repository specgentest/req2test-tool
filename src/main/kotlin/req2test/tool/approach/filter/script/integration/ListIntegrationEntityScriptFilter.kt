package req2test.tool.approach.filter.script.integration

import req2test.tool.approach.core.Filter
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.*

class ListIntegrationEntityScriptFilter (data: MutableMap<String, Any?>) : Filter(data, null) {
    override fun filterKeyInput(): String {
        return "ListEntities"
    }

    override fun filterName(): String {
        return "ListIntegrationEntityScriptFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListIntegrationEntityScript"
    }

    fun getInputs(entity: Entity): List<Attribute> {
        return entity.attributes.filter { !it.generatedDB and !it.generatedConstructor }
    }

    fun hasDBAttributes(entity: Entity): Boolean {
        return entity.attributes.any { it.dbValidations.isNotEmpty() or it.generatedDB }
    }

    fun createSteps(inputs: List<Attribute>, entity: Entity): List<String> {
        val steps = ArrayList<String>()
        steps.addAll(inputs.map { "Inform  ${it.name}" })
        steps.add("Persist a instance of ${entity.name}")
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
            if(hasDBAttributes(entity)) {
                val objective = "Find faults when persisting ${entity.name}"
                val preconditions = ArrayList<String>()
                val inputs = getInputs(entity)
                val steps = createSteps(inputs, entity)
                val inputTechniques = getInputTechniques()
                val location = "Use case create ${entity.name}"
                val script = Script(
                    Level.INTEGRATION,
                    objective,
                    location,
                    preconditions,
                    steps,
                    Technique.BLACKBOX,
                    inputTechniques,
                    inputs,
                    entity
                )
                scripts.add(script)

                textScript += "SCRIPT-${count++}\t\t\t\t\t| Creation of ${entity.name}\n"
                textScript += "Script objective\t\t\t\t| ${script.objective}\n"
                textScript += "Location\t\t\t\t\t| ${script.location}\n"
                textScript += "Steps\t\t\t\t\t\t| ${script.steps}\n"
                textScript += "Level\t\t\t\t\t\t| ${script.level.value}\n"
                textScript += "Technique\t\t\t\t\t| ${script.technique.value}\n"
                textScript += "Selection Criteria\t\t\t\t| ${script.inputTechniques.map { it.value }}\n\n"


            }
        }
        TestPlanGenerator.createFile(textScript, "outputArtifacts/", "IntegrationScripts.txt")

        return scripts
    }
}
