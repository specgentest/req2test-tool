package req2test.tool.approach.entity

import req2test.tool.approach.core.Log
import req2test.tool.approach.filter.ac.rule.type.GeneralException
import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Serializable

class UserStory(val rawUserStory: String) {
    var role: String? =  null
    var task: String? = null
    var purpose: String? = null
    var validated: Boolean = false
    val logs = ArrayList<Log>()
    var scenarios = ArrayList<Scenario>()
    var rules = ArrayList<Rule>()

    //Syntatic
    var isWellFormed: Boolean = false
    var isAtomic: Boolean = false
    var isMinimal: Boolean = false
    //Semantic
    var isConcepttuallySound: Boolean = false
    var isProblemOriented: Boolean = false
    var isUnambiguous: Boolean = false
    //Pragmatic
    var isFullSentence: Boolean = false
    var isEstimatable: Boolean = false

    fun validationDetails(): String {
        return "UserStory(validated=$validated, isWellFormed=$isWellFormed, isAtomic=$isAtomic, isMinimal=$isMinimal, isConcepttuallySound=$isConcepttuallySound, isProblemOriented=$isProblemOriented, isUnambiguous=$isUnambiguous, isFullSentence=$isFullSentence, isEstimatable=$isEstimatable)"
    }

    override fun toString(): String {
        return "UserStory(role='$role', task='$task', purpose='$purpose', validated=$validated, logs=$logs, scenarios=$scenarios, rules=$rules)"
    }



}


abstract class AcceptanceCriteria(val raw: String){
    var validated = false
}

enum class ScenarioType(val value: String) {
    MAIN_FLOW("Main Flow"),
    ALTERNATIVE_FLOW("Alternative Flow"),
    EXCEPTION_FLOW("Exception Flow")
}


enum class ScenarioPart(val value: String) {
    GIVEN("GIVEN"),
    WHEN("WHEN"),
    THEN("THEN")
}

enum class RuleType(val value: String) {
    FORMULA("FORMULA"),
    VALIDATION("VALIDATION")
}

class Rule(raw: String, val ruleType: String, val attributes: MutableMap<String, Any>, val dependencies: MutableSet<String>, @JsonIgnore var reference: String?=null): AcceptanceCriteria(raw){
    override fun toString(): String {
        return "Rule(ruleType='$ruleType', attributes=$attributes, dependencies=$dependencies)"
    }

    fun hasValidations(): Boolean {
        return (attributes["validations"] as List<String>).isNotEmpty()
    }
}

class Scenario(raw: String, name: String, val type: ScenarioType): AcceptanceCriteria(raw) {
    private val givenPart = ScenarioSteps(ScenarioPart.GIVEN)
    private val whenPart = ScenarioSteps(ScenarioPart.WHEN)
    private val thenPart = ScenarioSteps(ScenarioPart.THEN)

    fun addGiven(step: String){
        givenPart.steps.add(step)
    }

    fun addWhen(step: String){
        whenPart.steps.add(step)
    }
    fun addThen(step: String){
        thenPart.steps.add(step)
    }
    override fun toString(): String {
        val text = StringBuilder()
        text.append(givenPart)
        text.append(whenPart)
        text.append(thenPart)
        return text.toString()
    }
}

class ScenarioSteps(val part: ScenarioPart){
    val steps = ArrayList<String>()

    override fun toString(): String {
        val text = StringBuilder()
        for(i in 0..<steps.size){
            if(i==0) {
                if(part != ScenarioPart.GIVEN)
                    text.append("\n")
                text.append(part.value + " ")
            }
            else
                text.append(" AND ")
            text.append(steps[i])
        }
        return text.toString()
    }
}
@Serializable
data class DataStructure(val name: String, val attributes: MutableList<Attribute>){
    //It is validated when name and all attributes are validated, inclusive dependencies
    var validated: Boolean = false
    var dependencies = ArrayList<String>()

    override fun toString(): String {
        val str = StringBuilder()
        str.append("$name\n")
        for(attr in attributes)
            str.append("name: ${attr.name} - type: ${attr.type} - validations: ${attr.validations}\n")
        return str.toString()
    }
}

@Serializable
data class Entity(val name: String, val attributes: MutableList<Attribute>, val entityValidations: MutableList<EntityValidation>?){
    //It is validated when name and all attributes are validated, inclusive dependencies
    var validated: Boolean = false
    var attributeDependencies = ArrayList<String>()
    var attributeValidationDependencies = ArrayList<String>()
//    val dataTest = ArrayList<DataTest>()

    override fun toString(): String {
        val str = StringBuilder()
        str.append("$name\n")
        for(attr in attributes)
            str.append("name: ${attr.name} - type: ${attr.type} - validations: ${attr.validations}\n")

        if(entityValidations != null) {
            for (attrVal in entityValidations)
                str.append("name: ${attrVal.description} - detailed rule: ${attrVal.detailedRule}\n")
        }
        return str.toString()
    }

    fun getAttributeByName(name: String): Attribute? {
        return attributes.find { it.name == name }
    }

    companion object {
        fun validateType(type: String): Boolean {
            val primitiveTypes = arrayOf("String", "Boolean", "Int", "Float", "Double", "Char", "Date", "Time", "ENUM")
            if ((!primitiveTypes.any { it == type })) {
                if (!type.contains("list of") and !type.contains("map of") and !type.contains("set of")) {
                    if (!type.startsWith("%")) {
                        throw GeneralException(Log.createCriticalError("Entity", "1 Attribute type error: $type"))
                    } else if (type.contains("%") and primitiveTypes.any { type.contains(it) }) {
                        throw GeneralException(Log.createCriticalError("Entity", "2 Attribute type error: $type"))
                    }
                } else {
                    if (type.contains("map of")) {
                        val mapTypes = type.replace("map of", "").split(",")
                        mapTypes.forEach {
                            if (!validateType(it.trim())) {
                                throw GeneralException(Log.createCriticalError("Entity", "3 Attribute type error: $type"))
                            }
                        }
                    } else if (!type.contains("%") and !primitiveTypes.any { type.contains(it) }) {
                        throw GeneralException(Log.createCriticalError("Entity", "4 Attribute type error: $type"))
                    } else if (type.contains("%") and primitiveTypes.any { type.contains(it) }) {
                        throw GeneralException(Log.createCriticalError("Entity", "3 Attribute type error: $type"))
                    }
                }
            }
            return true
        }
    }
}

@Serializable
data class Attribute(
    var name: String, val type: String, val validations: List<String>, val dbValidations: List<String>,
    val generatedConstructor: Boolean, val generatedDB: Boolean, val generatedFormula: String? = null, val hasDependency: Boolean = false, val generated: Boolean = false, val calculated: Boolean = false)

@Serializable
data class EntityValidation(val description: String, val detailedRule: String)

