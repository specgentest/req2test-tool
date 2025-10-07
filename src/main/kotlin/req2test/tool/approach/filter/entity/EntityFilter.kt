package req2test.tool.approach.filter.entity

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.EntityValidation
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.filter.ac.rule.type.GeneralException

open class EntityFilter(data: MutableMap<String, Any?>) : Filter(data, null) {
    override fun filterKeyInput(): String {
        return "RawEntity"
    }

    companion object {
        fun splitRawEntity(rawEntity: String): Triple<String, List<String>, List<String>?> {
            val sentences = rawEntity.split("\n")
            val name = sentences[0]
            val index = sentences.indexOfFirst { it.contains("VALIDATIONS:") }
            if(index == -1){
                val rawAttributes = sentences.subList(1, sentences.size)
                return Triple(name, rawAttributes, null)
            }
            else {
                val rawAttributes = sentences.subList(1, index)
                val rawValidations = sentences.subList(index+1, sentences.size)
                return Triple(name, rawAttributes, rawValidations)
            }
        }
    }

    override fun filterName(): String {
        return "ValidateAndConvertEntityFilter"
    }

    override fun filterKeyOutput(): String {
        return "Entity"
    }

    private fun validateEntityName(dsName: String): Boolean {
        return (dsName.length > 1)
    }

    private fun splitRawLine(rawLine: String, totalFields: Int): List<String>? {
        val values = rawLine.split("|")
        if (values.any { it.trim().isBlank() }) {
            logs.add(Log.createCriticalError("Entity", "Blank field in: $rawLine"))
            return null
        }
        return if (values.size == totalFields)
            values
        else {
            logs.add(Log.createCriticalError("Entity","Invalid format: $rawLine"))
            null
        }
    }
    fun validateAttributeValidation(rawValidation: String): Boolean {
        val validValidations = splitRawLine(rawValidation, 2)
        return validValidations != null
    }

    fun extractAttributeValidations(rawValidations: List<String>): MutableList<EntityValidation>? {
        var validAttributeValidation = true

        val entityValidations = ArrayList<EntityValidation>()
        for (rawAttributeValidation in rawValidations) {
            if (validateAttributeValidation(rawAttributeValidation)) {
                val values = splitRawLine(rawAttributeValidation, 2) as List<String>
                val description = values[0].trim()
                val detailedRule = values[1].trim()
                val entityValidation = EntityValidation(description, detailedRule)
                entityValidations.add(entityValidation)
            }
            else
                validAttributeValidation = false
        }
        if (!validAttributeValidation)
            return null

        return entityValidations
    }


    fun validateAttribute(rawAttribute: String): Boolean {
        val validAttribute = splitRawLine(rawAttribute, 3)
        if(validAttribute == null) return false

        var validAttributeValidations = true

        val validations: List<String> = validAttribute[2].split(';')

        if(validations.filter { it.contains("generated:") }.size > 1) {
            logs.add(Log.createCriticalError("Entity", "Attribute: more than 1 generated validation"))
            return false
        }

        validations.forEach {
            if (!validateAttributeValidationTagDB(it)) {
                validAttributeValidations = false
            }
        }
        return validAttributeValidations
    }

    fun extractListAttributes(rawAttributes: List<String>): List<Attribute>? {
        var validAttribute = true

        val attributes = ArrayList<Attribute>()
        for (rawAttribute in rawAttributes) {
            if (validateAttribute(rawAttribute)) {
                val values = rawAttribute.split("|")
                val attributeName = values[0].trim()
                val attributeType = values[1].trim()
                var validations: List<String> = values[2].split(';')
                validations = validations.map { it.trim() }

                val generatedConstructor = validations.any {it.contains("generated:") and !it.contains("[DB]")}
                val generatedDB = validations.any {it.contains("generated:") and it.contains("[DB]")}

                var generated = false
                var generatedFormula = validations.find { it.startsWith("generated:") }
                if(generatedFormula != null) {
                    generated = true
                    generatedFormula = generatedFormula.replace("generated:", "")
                        .replace("[DB]", "")
                        .trim()
                }

                var calculated = false
                var calculatedFormula = validations.find { it.startsWith("calculated:") }
                if(calculatedFormula != null) {
                    calculated = true
                    generatedFormula = calculatedFormula.replace("calculated:", "")
                        .replace("[DB]", "")
                        .trim()
                }

                //Remove generated form validations
                validations = validations.filter {!it.contains("generated:")}
                //creating dbValidations
                val dbValidations = validations.filter {it.contains("[DB]")}.map { it.replace("[DB]", "").trim() }
                //Remove DB validations from validations
                validations = validations.filter {!it.contains("[DB]")}

                val attribute = Attribute(attributeName, attributeType, validations, dbValidations, generatedConstructor = generatedConstructor,
                    generatedDB = generatedDB, generatedFormula=generatedFormula, generated = generated, calculated = calculated)
                attributes.add(attribute)
            }
            else
                validAttribute = false
        }
        if (!validAttribute)
            return null

        return attributes
    }

    fun createEntity(name: String, rawAttributes: List<String>, rawAttributeValidations: List<String>? = null): Entity? {
        var validEntity = true
        if (!validateEntityName(name)) {
            logs.add(Log.createCriticalError("Entity", "Error validating entity name"))
            validEntity = false
        }

        val attributes = extractListAttributes(rawAttributes)

        if (attributes != null) {
            if (!validateTypes(attributes))
                validEntity = false
        }

        if ((!validEntity) or (attributes == null))
            return null

        var entityValidations: MutableList<EntityValidation>? = null
        if(rawAttributeValidations != null)
            entityValidations = extractAttributeValidations(rawAttributeValidations)

            val entity = Entity(
                name,
                attributes as MutableList<Attribute>,
                entityValidations
            )

            entity.attributeDependencies.addAll(extractDependencies(entity.attributes))
            entity.attributeValidationDependencies.addAll(extractAttributeValidationsDependencies(entity))
            return entity
    }

    fun validateAttributeValidationTagDB(validation: String): Boolean {
        val pattern = Regex("\\[(.*?)\\]") // Expressão regular para encontrar texto entre colchetes
        val correspondence = pattern.findAll(validation)
        correspondence.forEach {
            val tag = it.value.replace("[", "").replace("]", "")
            if (tag != "DB") {
                logs.add(Log.createCriticalError("Entity", "Error validating attribute: invalid $tag"))
                return false
            }
        }
        return if (correspondence.count() <= 1)
            true
        else {
            logs.add(Log.createCriticalError("Entity", "Error validating attribute: more than one [DB] tag"))
            false
        }
    }



    fun validateTypes(attributes: List<Attribute>): Boolean {
        var isValid = true
        attributes.forEach {
            val type = it.type
            try {
                Entity.validateType(type)
            }
            catch (ex: GeneralException){
                logs.add(ex.log)
                isValid = false
            }
        }
        return isValid
    }

    fun extractDependencies(attributes: MutableList<Attribute>): Set<String> {
        val dependencies = HashSet<String>()

        for (attribute in attributes) {
            val type = attribute.type.trim()
            val types = arrayOf("String", "Int", "Float", "Double", "Char", "Date", "ENUM")
            if (!types.any { it == type.trim() } and !type.contains("list of") and !type.contains("map of") and !type.contains(
                    "set of") and type.startsWith("%"))
                dependencies.add(type)
            else if (type.contains("list of")){
                val typeList = type.replace("list of", "").trim()
                if(typeList.startsWith("%"))
                    dependencies.add(typeList)
            }
            else if (type.contains("set of")){
                val typeSet = type.replace("set of", "").trim()
                if(typeSet.startsWith("%"))
                    dependencies.add(typeSet)
            }
            else if (type.contains("map of")){
                val mapTypes = type.replace("map of", "").split(",")
                mapTypes.forEach{ mapIt ->
                    if(!types.any { it == mapIt.trim() })
                        dependencies.add(mapIt.trim())
                }
            }
        }
        //Remove % of all dependencies
        return (dependencies.map { it.replace("%", "") }).toSet()
    }

    fun extractAttributeValidationsDependencies(entity: Entity): Set<String> {
        val dependencies = HashSet<String>()
        if(entity.entityValidations != null) {
            for (attributeValidation in entity.entityValidations) {
                var pattern = Regex("%\\S+")
                var dp = pattern.findAll(attributeValidation.detailedRule).map { it.value }.toSet()
                dependencies.addAll(dp)

                //Alias to $
                pattern = Regex("""\$\w+""")
                dp = pattern.findAll(attributeValidation.detailedRule).map { it.value }.toSet()
                dp = dp.map { it.replace("$", "${entity.name}.") }.toSet()
                dependencies.addAll(dp)
            }
        }
        return (dependencies.map { it.replace("%", "") }).toSet()
    }


    override fun compute(input: Any): Any? {
        val (name, rawAttributes, rawAttributeValidations) = splitRawEntity(input as String)

        val entity = createEntity(name, rawAttributes, rawAttributeValidations)

        throwsCriticalErrors()

        return entity
    }
}