package req2test.tool.approach.filter.datastructure

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.DataStructure

class ValidateAndConvertListDataStructureFilter(data: MutableMap<String, Any?>): Filter(data, null) {
    override fun filterKeyInput(): String { return "ListRawDataStructure" }
    override fun filterName(): String { return "ValidateAndConvertListDataStructureFilter" }
    override fun filterKeyOutput(): String { return "ListDataStructure" }
    private val listDS = ArrayList<DataStructure>()

    private fun validateExistingDataStructure(dataStructure: DataStructure): Boolean {
        return !listDS.any { it.name == dataStructure.name }
    }

    private fun validateDependencies(dependencies: MutableList<String>): Boolean {
        for(dependency in dependencies){
            if(!listDS.any { it.name == dependency })
                return false
        }
        return true
    }
    override fun compute(input: Any): Any {
        val listDataStructure = input as MutableList<String>

        for(rawDS in listDataStructure){
            data["RawDataStructure"] = rawDS
            val dsFilter = ValidateAndConvertDataStructureFilter(data)
            if(dsFilter.execute()){
                var result = data["DataStructure"] as DataStructure
                if(!validateExistingDataStructure(result)){
                    logs.add(Log.createError("ValidateAndConvertListDataStructureFilter", "DataStructure ${result.name } already exists"))
                }
                else if(!validateDependencies(result.dependencies)){
                    logs.add(Log.createError("ValidateAndConvertListDataStructureFilter", "DataStructure ${result.name } depends of ${result.dependencies}"))
                }
                else {
                    result.validated = true
                    listDS.add(result)
                }
            }
        }
        return listDS
    }
}