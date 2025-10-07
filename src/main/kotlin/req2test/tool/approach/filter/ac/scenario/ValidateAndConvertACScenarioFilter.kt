package req2test.tool.approach.filter.ac.scenario

import req2test.tool.approach.core.Filter
import req2test.tool.approach.entity.DataStructure

class ValidateAndConvertACScenarioFilter(data: MutableMap<String, Any?>): Filter(data) {
    override fun filterKeyInput(): String { return "RawACScenario" }
    override fun filterName(): String { return "ValidateAndConvertACScenarioFilter" }
    override fun filterKeyOutput(): String { return "ACScenario" }

    private fun validateScenario(rawRule: String): Boolean {
        val values = rawRule.split("|")
        return (values.size == 3) and ((values[1].trim() == "formula") or (values[1].trim() == "validation"))
    }

//    private fun createScenario(rawRule: String): Rule {
//        var values = rawRule.split("|")
//        val name = values[0].trim()
//        lateinit var type: RuleType
//        var description = values[2].trim()
//        var formula: String? = null
//        if(values[1].trim() == "formula") {
//            type = RuleType.FORMULA
//            val formulaValues = description.split(':')
//            description = formulaValues[0]
//            formula = formulaValues[1]
//        }
//        else if(values[1].trim() == "validation") {
//            type = RuleType.VALIDATION
//        }
//        return Rule(rawRule, name, type, description, formula)
//    }

    //Colocar na superclasse
    private fun validateDependencyEntities(rawRule: String): Boolean {
        val padrao = Regex("""%\w+""")
        val correspondencias = padrao.findAll(rawRule)


        val entities = mutableListOf<String>()
        for (match in correspondencias) {
            entities.add(match.value)
        }

        for(entity in entities) {
            val listDataStructure = data["ListDataStructure"] as MutableList<DataStructure>
            if (listDataStructure != null) {
                if(!listDataStructure.any { it.name.trim() == entity.replace("%","") }) return false
            }
        }
        return true
    }

    //Colocar na superclasse
    private fun extractDependencyRules(rawRule: String): MutableList<String>{
        val dependenciesRules = ArrayList<String>()
        val padrao = Regex("\\[(.*?)\\]") // Expressão regular para encontrar texto entre colchetes

        val correspondencias = padrao.findAll(rawRule)
        for (match in correspondencias) {
            val palavraEntreColchetes = match.groupValues[1]
            dependenciesRules.add(palavraEntreColchetes)
        }
        return dependenciesRules
    }

    override fun compute(input: Any): Any? {
        //Validation if template is correct
       /* var rawRule = input as String
        var validated = true
        if(!validateRule(rawRule)) {
            logs.add(Log.createCriticalError("AC Rule", "Error validating rule"))
            return null
        }
        if(!validateDependencyEntities(rawRule)) {
            logs.add(Log.createError("AC Rule", "Error validating dependency entities for rule"))
            validated = false
        }
        val rule = createRule(rawRule)
        rule.dependencies = extractDependecyRules(rawRule)
        rule.validated = validated
        return rule */
        return null
    }
}




fun validarUserStory(userStory: String): Boolean {
    val padrao = Regex("(DADO .*? QUANDO .*? ENTÃO .*?)") // Expressão regular para validar o template

    return padrao.matches(userStory)
}

fun main() {
    val userStory = "DADO que estou na tela de efetuar reserva E o sistema apresenta meus dados de %Cliente na tela (nome completo, número da CNH, data de expiração da CNH e data de nascimento) QUANDO preencho os campos de data da reserva (campo de texto), hora da reserva (campo de texto), tipo de carro (campo de texto) E clico em efetuar reserva ENTÃO o sistema cria uma %Reserva [R2] E o sistema apresenta na tela o código da reserva gerada pelo sistema"

    if (validarUserStory(userStory)) {
        println("A user story está no formato válido.")
    } else {
        println("A user story não está no formato válido.")
    }
}
