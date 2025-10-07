package req2test.tool.approach.filter.entity

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.processor.generator.file.TestPlanGenerator

class ListEntitiesFilter(data: MutableMap<String, Any?>): Filter(data, null) {
    override fun filterKeyInput(): String { return "ListRawEntities" }
    override fun filterName(): String {
        return "ListEntitiesFilter"
    }
    override fun filterKeyOutput(): String { return "ListEntities" }
    private val listEntities = ArrayList<Entity>()

    private fun validateExistingEntity(entity: Entity): Boolean {
        return !listEntities.any { it.name == entity.name }
    }

    private fun validateDependencies(entity: Entity): Boolean {
        for(dependency in entity.attributeDependencies){
            if(dependency == entity.name)
                return true
            if(!listEntities.any { it.name == dependency })
                return false
        }
        return true
    }

    private fun validateAttributeValidationDependencies(entity: Entity): Boolean {
        for(dependency in entity.attributeValidationDependencies) {
            val values = dependency.split(".")
            var validEntity = false
            val entityName = if(values.size == 2)
                values[0]
            else
                dependency

            validEntity =  listEntities.any { it.name == entityName } or (entityName == entity.name)
            if(!validEntity){
                logs.add(Log.createCriticalError("List Entities", "Dependency $entityName is not defined"))
                return false
            }

            if(values.size == 2){
                val dependencyEntity = if(entityName == entity.name)
                    entity
                else {
                    listEntities.find { it.name == entityName }
                }

                if(dependencyEntity == null){
                    logs.add(Log.createCriticalError("List Entities", "Dependency $entityName is not defined"))
                    return false
                }
                else {
                    val attributeName = values[1]
                    if(!dependencyEntity.attributes.any { it.name == attributeName }) {
                        logs.add(Log.createCriticalError("List Entities", "Dependency $entityName is defined, but $attributeName is not defined"))
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun createListEntities(listRawEntities: MutableList<String>) {
        for(rawDS in listRawEntities){
            data["RawEntity"] = rawDS
            val entityFilter = EntityFilter(data)
            if(entityFilter.execute()){
                var entityResult = data["Entity"] as Entity
                if(!validateExistingEntity(entityResult)){
                    logs.add(Log.createCriticalError("List Entities", "Entity ${entityResult.name } already exists"))
                }
                else if(!validateDependencies(entityResult)){
                    logs.add(Log.createCriticalError("List Entities", "Entity ${entityResult.name } depends on ${entityResult.attributeDependencies}"))
                }
                else if(!validateAttributeValidationDependencies(entityResult)){
                    logs.add(Log.createCriticalError("List Entities", "Entity ${entityResult.name } depends on ${entityResult.attributeValidationDependencies}"))
                }
                else {
                    entityResult.validated = true
                    this.listEntities.add(entityResult)
                }
            }
        }
    }
    override fun compute(input: Any): Any {
        val listEntities = input as MutableList<String>
        createListEntities(listEntities)
        throwsCriticalErrors()
        TestPlanGenerator.createFile(this.listEntities.toString(), "outputArtifacts/", "entities.txt")
        return this.listEntities
    }
}