package req2test.tool.approach.filter.testcase.unit.controller.validation

import req2test.tool.approach.filter.testcase.unit.controller.ListMockTestCaseCreateEndpointFilter
import req2test.tool.approach.entity.*

class ListMockTestCaseCreateEndpointValidationsFilter (data: MutableMap<String, Any?>): ListMockTestCaseCreateEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListCreateEndpointMockScript" }
    override fun filterName(): String {

        return "ListMockTestCaseCreateValidationsEndpointFilter"
    }
    override fun filterKeyOutput(): String { return "ListMockTestCaseCreateEndpoint" }

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


    override fun createTestCases(entityDataTest: ListDataTest, ruleDataTest: ListDataTest, invalidIDDataTest: DataTest?, script: Script, entity: Entity): List<TestCase> {
        val testCases = ArrayList<TestCase>()
        testCases.addAll(super.createTestCases(entityDataTest, ruleDataTest, invalidIDDataTest, script, entity))
        return testCases
    }

}
