package req2test.tool.approach.core

import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.filter.ac.rule.type.RuleVariable


data class TestArchitecture(val ruleAction: String,
                            var repositoryLayerClass: String,
                            var ruleLayerClass: String,
                            var ruleLayerInstance: String,
                            var ruleLayerMethod:String,
                            var ruleValidationLayerMethod:String,
                            var controllerLayerClass: String,
                            var controllerLayerInstance: String,
                            var controllerLayerMethod:String,
                            var controllerValidationLayerMethod:String,
                            var DTOLayerRequestClass:String,
                            var DTOLayerResponseClass:String,
                            var entityConstructor:String,
                            var dbErrorAction:String,
                            var dbError:String,
                            var dbErrorFramework:String,
                            var ruleError:String,
                            var ruleErrorAction:String,
                            var findMethod:String,
                            var mapMethods:Map<String, String>,
                            var endpoints:MutableMap<String, String>,
                            var endpointsWithValidations:MutableMap<String, String>,
                            var findMethodReturnTrue:String,
                            var findMethodReturnFalse:String,
                            var urlinput: List<RuleVariable>?,
                            var url: String? = null){
    companion object {
        fun cleanArchitecture(ruleAction: String, entity: Entity, inputs: List<Attribute>, urlinput: List<RuleVariable>? = null, url: String? = null): TestArchitecture {
            val methodParameters = inputs.map { it.name }
            val ruleLayerClass = "$ruleAction${entity.name}UseCase"
            val controllerLayerClass = "${entity.name}Controller"
            val DTOLayerRequestClass = "$ruleAction${entity.name}Request"
            val DTOLayerResponseClass = "$ruleAction${entity.name}Response"
            val methods = HashMap<String, String>()
            methods["create"] = "save"
            methods["retrieve"] = "findById"
            methods["retrieve all"] = "getAll"
            methods["retrieve all validation"] = "getAllWithExtraValidation"
            methods["update"] = "update"
            methods["partial update"] = "partialUpdate"
            methods["delete"] = "deleteById"
            methods["delete all"] = "deleteAll"
            methods["filter"] = "filter"
            methods["filter validation"] = "filterWithExtraValidation"
            methods["find"] = "findBy"


            val endpoints = HashMap<String, String>()
            if(url != null)
                endpoints["url"] = url

            endpoints["generic"] = "/{rule}"
            endpoints["create"] = "/${entity.name.replaceFirstChar { it.lowercase() }}"
            endpoints["retrieve"] = "/${entity.name.replaceFirstChar { it.lowercase() }}/{id}"
            endpoints["retrieve all"] = "/${entity.name.replaceFirstChar { it.lowercase() }}"
            endpoints["update"] = "/${entity.name.replaceFirstChar { it.lowercase() }}/{id}"
            endpoints["partial update"] = "/${entity.name.replaceFirstChar { it.lowercase() }}/{id}"
            endpoints["delete"] = "/${entity.name.replaceFirstChar { it.lowercase() }}/{id}"
            endpoints["filter"] = "/${entity.name.replaceFirstChar { it.lowercase() }}/filter"
            endpoints["filter validation"] = "filterWithExtraValidation"

            val endpointsWithValidationsSuffix = "/with-validations"
            val endpointsWithValidations = HashMap<String, String>()
            endpointsWithValidations["create"] = "/${entity.name.replaceFirstChar { it.lowercase() }}$endpointsWithValidationsSuffix"
            endpointsWithValidations["retrieve"] = "/${entity.name.replaceFirstChar { it.lowercase() }}/{id}$endpointsWithValidationsSuffix"
            endpointsWithValidations["retrieve all"] = "/${entity.name.replaceFirstChar { it.lowercase() }}$endpointsWithValidationsSuffix"
            endpointsWithValidations["update"] = "/${entity.name.replaceFirstChar { it.lowercase() }}/{id}$endpointsWithValidationsSuffix"
            endpointsWithValidations["partial update"] = "/${entity.name.replaceFirstChar { it.lowercase() }}/{id}$endpointsWithValidationsSuffix"
            endpointsWithValidations["delete"] = "/${entity.name.replaceFirstChar { it.lowercase() }}/{id}$endpointsWithValidationsSuffix"
            endpointsWithValidations["filter"] = "/${entity.name.replaceFirstChar { it.lowercase() }}/filter$endpointsWithValidationsSuffix"

            return TestArchitecture( ruleAction = ruleAction,
                    repositoryLayerClass = "${entity.name}Repository",
                    ruleLayerClass = ruleLayerClass,
                    ruleLayerInstance = ruleLayerClass.replaceFirstChar { it.lowercase() },
                    ruleLayerMethod = "execute${formatParameters(methodParameters)}",
                    ruleValidationLayerMethod = "executeWithExtraValidation${formatParameters(methodParameters)}",
                    controllerLayerClass = controllerLayerClass,
                    controllerLayerInstance = controllerLayerClass.replaceFirstChar { it.lowercase() },
                    controllerLayerMethod = "${entity.name.replaceFirstChar { it.lowercase() }}${ruleAction}${formatParameters(methodParameters)}",
                    controllerValidationLayerMethod = "${entity.name.replaceFirstChar { it.lowercase() }}${ruleAction}WithExtraValidation${formatParameters(methodParameters)}",
                    DTOLayerRequestClass = DTOLayerRequestClass,
                    DTOLayerResponseClass = DTOLayerResponseClass,
                    entityConstructor = "${entity.name}${formatParameters(methodParameters)}",
                    dbErrorAction = "thrown",
                    dbError = "DataBaseException",
                    dbErrorFramework = "org.springframework.dao.DataIntegrityViolationException",
                    ruleError = "${entity.name}Exception",
                    ruleErrorAction = "thrown",
                    findMethod = "existsBy",
                    mapMethods = methods,
                    endpoints = endpoints,
                    endpointsWithValidations = endpointsWithValidations,
                    findMethodReturnTrue = "true",
                    findMethodReturnFalse = "false",
                    urlinput,
                    url = url)
        }

        private fun formatParameters(parameters: List<String>): String {
            return parameters.joinToString(prefix = "(", separator = ", ", postfix = ")")
        }

    }


}