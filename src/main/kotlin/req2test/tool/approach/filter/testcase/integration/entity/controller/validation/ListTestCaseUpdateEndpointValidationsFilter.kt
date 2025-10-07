package req2test.tool.approach.filter.testcase.integration.entity.controller.validation

import req2test.tool.approach.filter.testcase.integration.entity.controller.ListTestCaseUpdateEndpointFilter
import req2test.tool.approach.entity.*

class ListTestCaseUpdateEndpointValidationsFilter (data: MutableMap<String, Any?>): ListTestCaseUpdateEndpointFilter(data){

    override fun filterKeyInput(): String { return "ListUpdateEndpointScript" }
    override fun filterName(): String {

        return "ListTestCaseUpdateEndpointValidationsFilter"
    }
    override fun filterKeyOutput(): String { return "ListTestCaseUpdateEndpoint" }

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
