package req2test.tool.approach.filter.testcase.integration.entity.controller.validation

import req2test.tool.approach.entity.*
import req2test.tool.approach.filter.testcase.integration.entity.controller.ListTestCaseDeleteEndpointFilter

class ListTestCaseDeleteEndpointValidationsFilter (data: MutableMap<String, Any?>): ListTestCaseDeleteEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListDeleteEndpointScript" }
    override fun filterName(): String {

        return "ListTestCaseDeleteEndpointValidationsFilter"
    }
    override fun filterKeyOutput(): String { return "ListTestCaseDeleteEndpoint" }

    override fun setEndpoints() {
        endpoints = testArchitecture.endpointsWithValidations
    }

    override fun setRuleLayerMethod() {
        ruleLayerMethod = testArchitecture.ruleValidationLayerMethod
    }

    override fun setControllerLayerMethod() {
        controllerLayerMethod = testArchitecture.controllerValidationLayerMethod
    }

    override fun filterScripts(scripts: List<Script>): List<Script> {
        return scripts.filter { it.reference is Rule }
    }

    override fun isValidationEndpoint(): Boolean {
        return true
    }


    //Nao usar entityDataTest pois nao respeita as Validations do UseCaseValidations e Controller
    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        //Neste cenário entityDataTest == ruleDataTest
        testCases.addAll(super.createTestCases(entityDataTest, ruleDataTest, invalidIDDataTest, script, entity))
        return testCases
    }

}
