package req2test.tool.approach.filter.ac.rule.type

import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

class GeneralException(val log: Log): Exception(log.toString())
data class RuleVariable(val name: String, val attributeReference: Attribute? = null, var type: String? = null, var validations: MutableList<String>? = null,
                        var generated: Boolean = false, val generatedFormula: String? = null)
abstract class BaseRuleType(val rawACRule: String, val entities: List<Entity>) {
    abstract fun createRule(): Rule
    protected val ruleAttributes = HashMap<String, Any>()

    //All rules may have validations
     val validations = ArrayList<String>()

    protected fun getEntityByName(entityName: String): Entity {
        return entities.find { it.name == entityName }
            ?: throw GeneralException(Log.createCriticalError("RuleType", "Entity $entityName not found"))
    }
    fun getEntityAttributeByName(attributeFullname: String): Attribute {
        val values = attributeFullname.split(".").map { it.trim() }
        if(values.size != 2)
            throw GeneralException(Log.createCriticalError("RuleType", "Entity attribute format invalid: $attributeFullname"))

        val entity = getEntityByName(values[0])
        val attribute = entity.attributes.find { it.name == values[1] }
        if(attribute == null)
            throw GeneralException(Log.createCriticalError("RuleType", "Entity attribute $attributeFullname not found"))
        return attribute
    }

    protected fun validateEntityAttribute(entityName: String, attributeName: String): Boolean {
        val entity = entities.find { it.name == entityName }
        if(entity == null)
            throw GeneralException(Log.createCriticalError("RuleType", "Entity $entityName not found"))

        val isValid = entity.attributes.any { it.name == attributeName }
        if(!isValid)
            throw GeneralException(Log.createCriticalError("RuleType", "$entityName attribute $attributeName not found"))
        return true
    }

    protected fun validateEntity(entityName: String): Boolean {
        val isValid = entities.any { it.name == entityName }
        if(!isValid)
            throw GeneralException(Log.createCriticalError("RuleType", "Entity $entityName not found"))
        return true
    }

    fun splitRawLine(line: String) : List<String>{
        val values = line.split("|")
        if (values.any { it.trim().isBlank() }) {
            throw GeneralException(Log.createCriticalError("RuleType", "Blank field in: $line"))
        }
        return values.map { it.trim() }
    }

    fun processValidation(line1: String) {
        //R1.validation | create %Triangle
        val values = splitRawLine(line1)

        //Validate validation
        val pattern = Regex("%\\S+")
        val dp = pattern.findAll(values[1]).map { it.value.replace("%", "").trim() }.toSet()
        dp.forEach {
            if(it.split(".").size == 1)
                getEntityByName(it)
            else if(it.split(".").size == 2)
                getEntityAttributeByName(it)
            else
                throw GeneralException(Log.createCriticalError("RuleType", "Entity/attribute format invalid: $it"))
        }
        validations.add(values[1])
    }
}