package req2test.tool.approach.filter.datastructure

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.DataStructure

open class ValidateAndConvertDataStructureFilter(data: MutableMap<String, Any?>): Filter(data, null) {
    override fun filterKeyInput(): String { return "RawDataStructure" }
    override fun filterName(): String { return "ValidateAndConvertDataStructureFilter" }
    override fun filterKeyOutput(): String { return "DataStructure" }

    private fun validateDSName(dsName: String): Boolean {
        return !dsName.any { it == ':' } and (dsName.length > 1)
    }

    private fun validateAttribute(rawAttribute: String): Boolean {
        val values = rawAttribute.split("|")
        return (values.size == 3)
    }

    private fun createDataStructure(name: String, rawAttributes: List<String>): DataStructure {
        val attributes = ArrayList<Attribute>()
        for(rawAttribute in rawAttributes){
            var values = rawAttribute.split("|")
            var validations: List<String> = values[2].split('.')
            val attribute = Attribute(values[0].trim(), values[1].trim(), validations, validations, false, false)
            attributes.add(attribute)
        }
        return DataStructure(name, attributes)
    }

    private fun extractDependencies(attributes: MutableList<Attribute>): MutableSet<String> {
        val dependencies = HashSet<String>()

        for(attribute in attributes){
            val type = attribute.type
            val types = arrayOf("String", "Int", "Float", "Double", "Char", "Date", "ENUM")
            if(!types.any { it == type.trim() } and !type.contains("List<") and !type.contains("Map<") and !type.contains("Set<"))
                dependencies.add(type.trim())
            //TODO verificar tipos entre < >. Se nao for primitivo adicionar na lista de dependencia
        }
        return dependencies
    }

    override fun compute(input: Any): Any? {
        //Validation if template is correct
        var sentences = (input as String).split("\n")
        if(!validateDSName(sentences[0])) {
            logs.add(Log.createCriticalError("Data Structure", "Error validating DS name"))
        }
        val rawAttributes = sentences.subList(1, sentences.size)
        for(rawattribute in rawAttributes){
            if(!validateAttribute(rawattribute)){
                logs.add(Log.createCriticalError("Data Structure", "Error validating attribute: $rawattribute"))
            }
        }


        if(criticalErrors().isEmpty()) {
            val ds = createDataStructure(sentences[0], rawAttributes)
            ds.dependencies.addAll(extractDependencies(ds.attributes))
            println(ds.dependencies)
            return ds
        }
        return null
    }
}