package req2test.tool.approach.core

import req2test.tool.approach.entity.DataStructure
import java.io.File

class FilterError(message: String, val logs:MutableList<Log>) : Exception(message)
class FilterCriticalError(message: String) : Exception(message)

class ApproachLevel {
    companion object {
        var levels: MutableList<String> = mutableListOf()
        fun addAllLevels(){
            levels.add("unit-entity")
            levels.add("unit-entity-attributes-generated")
            levels.add("unit-usecase")
            levels.add("unit-usecase-validations")
            levels.add("unit-endpoint")
            levels.add("unit-endpoint-validations")
            levels.add("unit-endpoint-generic")

            levels.add("integration-endpoint")
            levels.add("integration-endpoint-validations")
            levels.add("integration-endpoint-generic")
        }
    }
}
class OutputFolder {
    companion object {
        var basePath = ""
        var basePackage = "req2test.tool"
        var packageName = ""
        fun createTestFolderStructure(folderName: String) {
            packageName = "$basePackage.$folderName"
            basePath = "$folderName/"
            val folders = listOf(
                "prompts",
                "output/unit/entity",
                "output/unit/endpoint",
                "output/unit/endpoint-generic",
                "output/unit/endpoint-validations",
                "output/unit/rule",
                "output/unit/rule-validations",
                "output/integration/entity",
                "output/integration/entity/dataset",
                "output/integration/endpoint/dataset",
                "output/integration/endpoint-generic/dataset",
                "output/integration/endpoint-validations/dataset"
            )
            folders.forEach { path ->
                val dir = File(basePath, path)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
            }
        }

    }
    }

abstract class Filter(var data: MutableMap<String, Any?>, val processor: Any? =  null) {
    abstract fun filterKeyInput(): String
    abstract fun filterName(): String
    abstract fun filterKeyOutput(): String
    abstract fun compute(input: Any): Any?
    var baseTestName = ""
    open fun isMandatoryFilter(): Boolean {
        return false
    }

    fun extractBetweenParentheses(input: String): String {
        val start = input.indexOf('(')
        val end = input.indexOf(')')

        return if (start != -1 && end != -1 && start < end) {
            "{" + input.substring(start + 1, end) + "}"
        } else {
            ""
        }
    }

    fun extractWithoutFirstParam(input: String): String {
        val start = input.indexOf('(')
        val end = input.indexOf(')')

        return if (start != -1 && end != -1 && start < end) {
            val content = input.substring(start + 1, end)
            val parts = content.split(",").map { it.trim() }
            if (parts.size > 1) {
                "{" + parts.drop(1).joinToString(", ") + "}"
            } else {
                "{}"
            }
        } else {
            ""
        }
    }

    protected val logs = ArrayList<Log>()

    //The compute functions will return true even if exists warning
    fun warnings(): List<Log> { return logs.filter { it.logType == LogType.WARNING } }
    //The compute functions will return false if exists error, but it will not interrupt the program (do not throw exception)
    fun errors(): List<Log> { return logs.filter { it.logType == LogType.ERROR } }
    //The system will be interrupted by CriticalErrorException
    fun criticalErrors(): List<Log> { return logs.filter { it.logType == LogType.CRITICAL_ERROR } }


    fun throwsCriticalErrors(){
        if(criticalErrors().isNotEmpty())
            throw FilterError("${filterName()}: ${criticalErrors().toString()}", logs)
    }


    private fun getFilterKeyInput(): Any {
        if(isMandatoryFilter())
            return data[filterKeyInput()] ?: throw FilterCriticalError("Input ${filterKeyInput()} does not exist")
        else
            return data[filterKeyInput()] ?: return false
    }

    private fun addWarnings(){
        val exitingWarnings = data.get("warnings")
        if (exitingWarnings == null)
            data["warnings"] = ArrayList<Log>()
        (data["warnings"] as MutableList<Log>).addAll(warnings())
    }

    private fun addErrors(){
        val exitingErrors = data.get("errors")
        if (exitingErrors == null)
            data["errors"] = ArrayList<Log>()
        (data["errors"] as MutableList<Log>).addAll(errors())
    }

    fun execute(): Boolean {
        val input = getFilterKeyInput()
        //Condition added to be flexible for filters that is not mandatory
        if(input == false)
            return false
        val output = compute(input)
        addWarnings()
        addErrors()
        throwsCriticalErrors()
        data[filterKeyOutput()] = output
        return true
    }
}

class Pipeline(private val filters: MutableList<Filter>, private val data: MutableMap<String, Any?>) {
    fun execute(): MutableMap<String, Any?> {
        for(filter in filters) {
            println(filter.filterName())
            try {
                filter.execute()
            }catch (ex: Exception){
                throw ex
            }
        }
        return data
    }
}

abstract class PipelineBuilder(val inputKeyValues: MutableMap<String, Any?>) {
    val filters: MutableList<Filter> = ArrayList()
    val data: MutableMap<String, Any?> = HashMap()
    init {
        for ((inputKey, inputValue) in inputKeyValues) {
            data[inputKey] = inputValue
        }
    }
    open fun build(): Pipeline {
        createFilters()
        return Pipeline(filters, data)
    }
    abstract fun createFilters()
}

class ExamplePipelineBuilder(inputKeyValues: MutableMap<String, Any?>, val dataStructure: MutableList<DataStructure>? = null, val processor: Any):
    PipelineBuilder(inputKeyValues){
    init {
        if (dataStructure != null){
            data["DataStructure"] = dataStructure
        }
    }

    override fun createFilters() {
        filters.add(ExampleFilter(data))
    }
}

open class ExampleFilter(data: MutableMap<String, Any?>) : Filter(data, null) {
    override fun filterKeyInput(): String {
        TODO("Not yet implemented")
    }

    override fun filterName(): String {
        TODO("Not yet implemented")
    }

    override fun filterKeyOutput(): String {
        TODO("Not yet implemented")
    }

    override fun compute(input: Any): Any? {
        TODO("Not yet implemented")
    }

}
