package req2test.tool.approach.filter.datatest

import req2test.tool.approach.core.Filter
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.ListDataTest

class ListEntitiesDataTestFilter (data: MutableMap<String, Any?>, processor: Any) : Filter(data, processor) {
    override fun filterKeyInput(): String {
        return "ListEntities"
    }

    override fun filterName(): String {
        return "ListEntitiesDataTestFilter"
    }

    override fun filterKeyOutput(): String {
        return "ListEntitiesDataTest"
    }

    override fun compute(input: Any): Any? {
        val entities = input as List<Entity>
        val entitiesDataTest = ArrayList<ListDataTest>()
        if (processor != null) {
            entities.forEach {
                data["Entity"] = it
                val filter = EntityDataTestFilter(data, processor)
                if(filter.execute()){
                    val entityDataTest = data["EntityDataTest"] as ListDataTest
                    entityDataTest.reference = it
                    entitiesDataTest.add(entityDataTest)
                }

            }
        }
        return entitiesDataTest
    }
}
