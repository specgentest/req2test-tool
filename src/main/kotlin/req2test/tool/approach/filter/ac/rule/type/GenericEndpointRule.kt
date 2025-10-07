package req2test.tool.approach.filter.ac.rule.type

import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule

/** Example
R1 | endpoint | calculate perimeter of a Triangle
R1.var | A | %Triangle.A
R1.var | B | %Triangle.B
R1.var | C | %Triangle.C
R1.var | perimeter | Int | $perimeter = $A + $B + $C
R1.input | A, B, C
R1.output | perimeter
R1.validation | create %Triangle
**/
class GenericEndpointRule(rawACRule: String, entities: List<Entity>): BaseRuleType(rawACRule, entities) {
    private val variables = ArrayList<RuleVariable>()
    private val input = ArrayList<RuleVariable>()
    private val output = ArrayList<RuleVariable>()
    private val dbInit = ArrayList<RuleVariable>()
    private val dbFinal = ArrayList<RuleVariable>()
    private lateinit var rule: Rule

    override fun createRule(): Rule {
        val lines = rawACRule.split("\n")
        val ruleId = splitRawLine(lines[0])[0].trim()
        ruleAttributes["id"] = ruleId
        ruleAttributes["input"] = input
        ruleAttributes["output"] = output
        ruleAttributes["dbinit"] = dbInit
        ruleAttributes["dbfinal"] = dbFinal
        ruleAttributes["variables"] = variables
        ruleAttributes["validations"] = validations
        rule = Rule(rawACRule, "endpoint", ruleAttributes, HashSet())

        lines.forEach {
            val values = splitRawLine(it)
            when (values[0]) {
                ruleId -> processLine1(it)
                "$ruleId.input" -> processInput(it)
                "$ruleId.output" -> processOutput(it)
                "$ruleId.data" -> processVar(it)
                "$ruleId.validation" -> processValidation(it)
                "$ruleId.dbinit" -> processDBInit(it)
                "$ruleId.dbfinal" -> processDBFinal(it)
                else -> throw GeneralException(Log.createCriticalError("ACRule - endpoint", "invalid format: $it"))
            }
        }
        return rule
    }

    private fun processLine1(line1: String) {
        //R1 | endpoint | calculate perimeter of a Triangle
        val values = splitRawLine(line1)
        val type = values[1].trim()
        val description = values[2].trim()

        if (values.size == 3) {
            if (type.trim() != "endpoint")
                throw GeneralException(Log.createCriticalError("RuleType", "Invalid format: $type"))
            ruleAttributes["description"] = description

        } else {
            throw GeneralException(Log.createCriticalError("RuleType", "Invalid format: should have 3 fields"))
        }
    }

    private fun processInputOutput(line1: String, db: Boolean = false): List<RuleVariable>{
        //R1.input | A, B, C
        val values = splitRawLine(line1)
        val res = ArrayList<RuleVariable>()
        if (values.size == 2) {
            val inputs = values[1].split(",").map { it.trim() }
            inputs.forEach {inputString ->
                val variable = variables.find { it.name == inputString }
                if(variable == null)
                    throw GeneralException(Log.createCriticalError("RuleType", "Input/Output variable $inputString not defined"))

                //Usado apenas em processDBInit e processDBFinal
                if(db) {
                    if (!variable.type?.contains("%")!!)
                        throw GeneralException(
                            Log.createCriticalError(
                                "RuleType",
                                "variable $inputString is not an Entity"
                            )
                        )
                }

                res.add(variable)
            }
        } else {
            throw GeneralException(Log.createCriticalError("RuleType", "Invalid format: should have 2 fields"))
        }
        return res
    }

    private fun processInput(line1: String) {
        //R1.input | A, B, C
        val inputs = processInputOutput(line1)
        input.addAll(inputs)
    }

    private fun processOutput(line1: String) {
        //R1.output | perimeter
        val outputs = processInputOutput(line1)
        output.addAll(outputs)
    }

    private fun processDBInit(line1: String) {
        //R1.dbinit| customer
        val outputs = processInputOutput(line1, true)
        dbInit.addAll(outputs)
    }

    private fun processDBFinal(line1: String) {
        //R1.dbfinal | customer
        val outputs = processInputOutput(line1, true)
        dbFinal.addAll(outputs)
    }

    private fun processVar(line1: String) {
        val values = splitRawLine(line1)

        val ruleVariable: RuleVariable
        val name: String
        if (values.size in listOf(3,4))
            name = values[1]
        else
            throw GeneralException(Log.createCriticalError("RuleType - process data", "Invalid format: should have 3 or 4 fields"))

        val validationConstrains = values[2].split(";")
        if (values.size == 3) {

            if(validationConstrains[0].contains("%")) {
                //verify if it is an attribute
                val field3 = validationConstrains[0].split(".")
                val entityName = field3[0].replace("%", "").trim()
                val entity = entities.find { it.name == entityName }
                if (entity == null)
                    throw GeneralException(
                        Log.createCriticalError(
                            "RuleType - process data",
                            "Entity $entityName not found"
                        )
                    )

                if (field3.size == 1) {
                    //It is an entity Type with no validations
                    //R1.data | T | %Triangle
                    ruleVariable = RuleVariable(name = name, type = entityName)
                } else if (field3.size == 2) {
                    //It is an entity attribute
                    //R1.data | A | %Triangle.A
                    val attributeName = field3[1].trim()
                    val attribute = entity.getAttributeByName(attributeName)
                    if (attribute != null)
                        ruleVariable = RuleVariable(name, attribute)
                    else
                        throw GeneralException(
                            Log.createCriticalError(
                                "RuleType - process data",
                                "Attribute Entity $attributeName not found"
                            )
                        )
                } else
                    throw GeneralException(
                        Log.createCriticalError(
                            "RuleType - process data",
                            "Invalid format: ${validationConstrains[0]}"
                        )
                    )
            }
            else {
                //R1.data | perimeter | Int
                val type = validationConstrains[0]
                Entity.validateType(type)
                ruleVariable = RuleVariable(name=name, type=type)
            }

            validationConstrains.subList(1, validationConstrains.size).forEach {
                if(ruleVariable.validations == null)
                    ruleVariable.validations = ArrayList<String>()
                ruleVariable.validations?.add(it)
            }

        }
        else if (values.size == 4) {
            //R1.data | perimeter | Int | generated: $perimeter = $A + $B + $C
            //R1.data | perimeter | Int | generated: $perimeter = $A + $B + $C; not null
            var generated = false
            val type = values[2]
            Entity.validateType(type)
            //Validations or gnerated formula
            val validationsAndGenerationPart = values[3].split(";")
            val validations = ArrayList<String>()
            var generatedFormula: String? = null
            validationsAndGenerationPart.forEach {
                if(it.startsWith("generated: ")) {
                    generatedFormula = it.replace("generated: ", "")
                    generatedFormula = it.replace("generated:", "")
                    generated = true
                }
                else {
                    validations.add(it)
                }
            }
            //generatedFormula = values[3]

            ruleVariable = RuleVariable(name=name, type=type, generated = generated, validations = validations, generatedFormula=generatedFormula)
        }
        else {
            throw GeneralException(Log.createCriticalError("RuleType", "Invalid format: should have 2 fields"))
        }
        //copy data from attributeReference
        if(ruleVariable.attributeReference != null){
            val validations = ArrayList<String>()
            validations.addAll(ruleVariable.attributeReference.validations)
            ruleVariable.validations?.let { validations.addAll(it) }
            ruleVariable.validations = validations
            ruleVariable.generated = ruleVariable.attributeReference.generatedDB or ruleVariable.attributeReference.generatedConstructor
            ruleVariable.type = ruleVariable.attributeReference.type
        }

        variables.add(ruleVariable)
    }
}