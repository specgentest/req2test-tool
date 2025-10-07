package req2test.tool.approach.filter.ac.rule.type

import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

//R# | view | $A | number | blank | validations

//R# | view | A | number
//R#.action | click | R1, R2
//R.validation | validation

//R1 | view | A | input
//R1.action | input | R1
//R1.validation | validation

//R# | view | btnEnter | button
//R#.action | click | R1, R2

//R2 | view | email | textfield
//R2.validation | valid email

//R6 | view | perimeter | text
//R6.validation | It can be empty or a number greater than 0

class ViewType {
    companion object {
        fun getEventTypes(): List<String>{
            return listOf("click", "change", "focusIn", "focus", "blur", "input")
        }

        fun getFields(): List<String>{
            return listOf("edit", "number", "text", "button", "select")
        }
    }
}

data class RuleAction(val eventName: String, val rulesId: List<String>){
    val rules = HashMap<String, Rule?>()
    init {
        rulesId.forEach{ rules[it] = null }
    }
}
class ViewRule(rawACRule: String, entities: List<Entity>): BaseRuleType(rawACRule, entities) {
    private val actions = ArrayList<RuleAction>()
    private lateinit var rule: Rule
    override fun createRule(): Rule {
        val lines = rawACRule.split("\n")
        val ruleId = splitRawLine(lines[0])[0].trim()
        ruleAttributes["id"] = ruleId
        ruleAttributes["validations"] = validations
        ruleAttributes["actions"] = actions

        rule = Rule(rawACRule, "view", ruleAttributes, HashSet())

        lines.forEach {
            val values = splitRawLine(it)
            when (values[0]) {
                ruleId -> processLine1(it)
                //"$ruleId.init" -> processInit(it)
                "$ruleId.action" -> processAction(it)
                "$ruleId.validation" -> processValidation(it)
                else -> throw GeneralException(Log.createCriticalError("ACRule - view", "invalid format: $it"))
            }
        }

        return rule
    }
//    private fun processInit(line: String) {
//        ruleAttributes["init"] = line.trim()
//    }

    private fun processAction(line: String) {
        //R#.action | click | R1, R2
        val values = splitRawLine(line)
        if(values.size != 3)
            throw GeneralException(Log.createCriticalError("RuleType - view", "Invalid action: $line"))
        val eventTypes = ViewType.getEventTypes()
        val event = values[1]
        if(!eventTypes.any { it == event})
            throw GeneralException(Log.createCriticalError("RuleType - view", "Invalid event: $event"))
        val rulesId = values[2]
        val extractedRulesId = rulesId.split(",").map { it.trim() }

        val action = RuleAction(event, extractedRulesId)
        rule.dependencies.addAll(extractedRulesId)
        actions.add(action)
    }

    private fun processLine1(line1: String) {
        //R# | view | $A | number
        val values = splitRawLine(line1)
        val type = values[1].trim()
        val variable = values[2].trim()
        val fieldType = values[3].trim()

        if (values.size == 4) {
            if (type.trim() != "view")
                throw GeneralException(Log.createCriticalError("RuleType - view", "Invalid format: $type"))
            ruleAttributes["variable"] = variable
            val fields = ViewType.getFields()
            if(fieldType in fields)
                ruleAttributes["field"] = fieldType
            else
                throw GeneralException(Log.createCriticalError("RuleType - view", "Invalid field type: $fieldType"))


        } else {
            throw GeneralException(Log.createCriticalError("RuleType", "Invalid format: should have 4 fields"))
        }
    }


//    private fun processValidation(line1: String) {
//        //R1.validation | create %Triangle
//        val values = splitRawLine(line1)
//
//        //Validate validation
//        val pattern = Regex("%\\S+")
//        val dp = pattern.findAll(values[1]).map { it.value.replace("%", "").trim() }.toSet()
//        dp.forEach {
//            if(it.split(".").size == 1)
//                getEntityByName(it)
//            else if(it.split(".").size == 2)
//                getEntityAttributeByName(it)
//            else
//                throw GeneralException(Log.createCriticalError("RuleType", "Entity/attribute format invalid: $it"))
//        }
//        validations.add(values[1])
//    }

}