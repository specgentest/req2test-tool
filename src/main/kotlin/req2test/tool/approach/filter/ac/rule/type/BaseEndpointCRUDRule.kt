package req2test.tool.approach.filter.ac.rule.type

import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

abstract class BaseEndpointCRUDRule(rawACRule: String, entities: List<Entity>, val totalFields : List<Int>, val type: String): BaseRuleType(rawACRule, entities){
    override fun createRule(): Rule {
        val lines = rawACRule.split("\n")
        val ruleId = splitRawLine(lines[0])[0].trim()
        val entityName = splitRawLine(lines[0])[2].trim()

        if(lines.size == 1) {
            processLine1(lines[0])
            addImplicitInput()
        }
//        else if(lines.size == 2) {
//            processLine1(lines[0])
//            processLine2(lines[1])
//        }


        else {
            lines.forEach {
                val values = splitRawLine(it)
                when (values[0]) {
                    ruleId -> processLine1(it)
                    "$ruleId.${getLine2FieldName()}" -> processLine2(it)
                    "$ruleId.validation" -> processValidation(it)
                    "$ruleId.urlinput" -> processUrlInput(it)
                    "$ruleId.url" -> processUrl(it)
                    else -> throw GeneralException(Log.createCriticalError("ACRule - endpoint", "invalid format: $it"))
                }
            }
            //lines.forEach { it -> processValidation(it) }
        }

            //throw GeneralException(Log.createCriticalError("RuleType","Invalid format: number of lines should be 1 or 2"))

        val ruleType = ruleAttributes["type"] as String
        return Rule(rawACRule, ruleType, ruleAttributes, HashSet(), reference = entityName)
    }

    protected open fun processLine1(line1: String) {
        val values = splitRawLine(line1)

        if(values.size in totalFields){
            baseCRUDEndpoint(values[0], values[1], values[2], type)
        }
        else {
            throw GeneralException(Log.createCriticalError("RuleType","Invalid format: $rawACRule"))
        }
    }

    protected open fun addImplicitInput(){
        val entity = ruleAttributes["entity"] as Entity
        val inputs = entity.attributes
        ruleAttributes[getLine2FieldName()] = inputs.map { RuleVariable(it.name, it) }
    }

    protected open fun getLine2FieldName(): String {
        return "input"
    }
    protected open fun processLine2(line2: String) {
        val values = splitRawLine(line2)
        if(values.size != 2)
            throw GeneralException(Log.createCriticalError("RuleType","Invalid format: number of parameters should be 2"))

        val id = ruleAttributes["id"]
        if(values[0].trim() != "${id}.${getLine2FieldName()}")
            throw GeneralException(Log.createCriticalError("RuleType","Invalid format: should initiate with ${id}.${getLine2FieldName()}"))

        val inputs  = values[1].split(",").map { it.trim() }
        val entity = ruleAttributes["entity"] as Entity
        val entityName = entity.name
        inputs.forEach {validateEntityAttribute(entityName, it)}
        ruleAttributes[getLine2FieldName()] = inputs.map { RuleVariable(it, entity.getAttributeByName(it)) }
    }

    protected open fun processUrlInput(line: String) {
        val values = splitRawLine(line)
        if(values.size != 2)
            throw GeneralException(Log.createCriticalError("RuleType","Invalid format: number of parameters should be 2"))

        val id = ruleAttributes["id"]

        val inputs  = values[1].split(",").map { it.trim() }
        val entity = ruleAttributes["entity"] as Entity
        val entityName = entity.name
        val listUrlInput = ArrayList<RuleVariable>()
        inputs.forEach {
            val subinput = it.split(".")
            if(subinput.size > 1) {
                var auxEntityName = entityName
                for (i in 0..subinput.size - 2) {
                    val attr = subinput[i].trim()
                    val subAttr = subinput[i + 1].trim()

                    validateEntityAttribute(auxEntityName, attr)
                    val subEntityAttribute = entity.getAttributeByName(attr)
                    val strSubEntity = subEntityAttribute?.type?.replace("%", "").toString()
                    validateEntityAttribute(strSubEntity, subAttr)

                    val subEntity = getEntityByName(strSubEntity)
                    auxEntityName = strSubEntity
                    if (i == subinput.size - 2) {
                        val subEntityAttr = subEntity.getAttributeByName(subAttr)
                        listUrlInput.add(RuleVariable(it, subEntityAttr))
//                        ruleAttributes["urlinput"] = inputs.map { RuleVariable(it, subEntityAttr) }

                    }
                }
            }
            else {
                validateEntityAttribute(entityName, it)
                listUrlInput.add(RuleVariable(it, entity.getAttributeByName(it)))
//                ruleAttributes["urlinput"] = inputs.map { RuleVariable(it, entity.getAttributeByName(it)) }

            }
        }
        ruleAttributes["urlinput"] = listUrlInput
    }


    protected open fun processUrl(line: String) {
        val values = splitRawLine(line)
        if(values.size != 2)
            throw GeneralException(Log.createCriticalError("RuleType","Invalid format: number of parameters should be 2"))

        val url  = values[1].trim()


        val regex = "\\{([^}]*)\\}".toRegex()
        val parameters = regex.findAll(url).map { it.groupValues[1] }.toList()

        val urlinput = ruleAttributes["urlinput"] as List<RuleVariable>
        val listUrlInput = urlinput.map { it.name.trim() }

        parameters.forEach {
            if (!listUrlInput.contains(it.trim()))
                throw GeneralException(Log.createCriticalError("RuleType","Invalid url parameters: $it"))
        }

        ruleAttributes["url"] = url
    }

    private fun baseCRUDEndpoint(id: String, type: String, entityName: String, expectedType: String){
        if(type.trim() != expectedType)
            throw GeneralException(Log.createCriticalError("RuleType","Invalid format: $type"))
        val entityName = entityName.replace("%", "").trim()
//        validateEntity(entity)
        ruleAttributes["id"] = id.trim()
        ruleAttributes["type"] = type.trim()
        ruleAttributes["entity"] = getEntityByName(entityName)
        ruleAttributes["validations"] = validations
    }
}