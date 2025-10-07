package req2test.tool.approach.pipeline

import req2test.tool.approach.core.ApproachLevel
import req2test.tool.approach.core.PipelineBuilder
import req2test.tool.approach.entity.DataStructure
import req2test.tool.approach.filter.ac.rule.ListACRulesFilter
import req2test.tool.approach.filter.automation.integration.entity.AutomationEntitiesIntegrationTCFilter
import req2test.tool.approach.filter.automation.unit.entity.AutomationEntitiesAttributesGeneratedTCFilter
import req2test.tool.approach.filter.automation.unit.entity.AutomationEntitiesTestCasesFilter
import req2test.tool.approach.filter.datatest.ListEntitiesDataTestFilter
import req2test.tool.approach.filter.datatest.genericrule.ListGenericRulesDataTestFilter
import req2test.tool.approach.filter.datatest.rule.ListRuleDataTestFilter
import req2test.tool.approach.filter.datatest.rule.ListRuleValidationDataTestFilter
import req2test.tool.approach.filter.entity.ListEntitiesFilter
import req2test.tool.approach.filter.script.integration.ListIntegrationEntityScriptFilter
import req2test.tool.approach.filter.script.unit.ListUnitEntityScriptFilter
import req2test.tool.approach.filter.script.unit.ListUnitEntityUpdateScriptFilter
import req2test.tool.approach.filter.testcase.integration.entity.ListIntegrationTestCaseEntityFilter
import req2test.tool.approach.filter.testcase.unit.entity.ListUnitTestCaseEntityFilter
import req2test.tool.approach.filter.testcase.unit.entity.ListUnitTestCaseEntityUpdateFilter
import req2test.tool.approach.filter.testplan.TestPlanFilter
import req2test.tool.approach.filter.userstory.ValidateAndConvertListUSFilter
import req2test.tool.approach.filter.userstory.UserStoryFilter
import req2test.tool.approach.processor.AI.UserStoryProcessorAI
import req2test.tool.approach.processor.stub.RuleProcessor
import req2test.tool.approach.processor.UserStoryProcessor
import req2test.tool.approach.processor.adapter.AIAdapter
import req2test.tool.approach.filter.script.controller.*
import req2test.tool.approach.filter.script.integration.controller.*
import req2test.tool.approach.filter.script.unit.rule.*
import req2test.tool.approach.filter.testcase.integration.entity.controller.*
import req2test.tool.approach.filter.testcase.integration.entity.controller.validation.*
import req2test.tool.approach.filter.testcase.unit.controller.*
import req2test.tool.approach.filter.testcase.unit.controller.validation.*
import req2test.tool.approach.filter.testcase.unit.rule.*
import req2test.tool.approach.filter.testcase.unit.rule.validation.*


abstract class AbstractUserStoryPipelineBuilder(inputKeyValues: MutableMap<String, Any?>, val dataStructure: MutableList<DataStructure>? = null, val processor: Any):
    PipelineBuilder(inputKeyValues){
    init {
        if (dataStructure != null){
            data["DataStructure"] = dataStructure
        }
    }
}


class EQDPipelineBuilder(inputKeyValues: MutableMap<String, Any?>, dataStructure: MutableList<DataStructure>? = null, processor: AIAdapter):
    AbstractUserStoryPipelineBuilder(inputKeyValues, dataStructure, processor) {
    override fun createFilters() {
        val AIAdapter = processor as AIAdapter
        val usprocessor = UserStoryProcessorAI(AIAdapter)

        //Specification
        filters.add(UserStoryFilter(data, usprocessor))
        filters.add(ListEntitiesFilter(data))
        filters.add(ListACRulesFilter(data))

        //Script
        filters.add(ListUnitEntityScriptFilter(data))
        filters.add(ListIntegrationEntityScriptFilter(data))

        //Test Data
        filters.add(ListEntitiesDataTestFilter(data, processor))

        //Test Case
        filters.add(ListUnitTestCaseEntityFilter(data))
        filters.add(ListIntegrationTestCaseEntityFilter(data))

        //Test Plan
        filters.add(TestPlanFilter(data))

        //Automated Testing
        filters.add(AutomationEntitiesTestCasesFilter(data, processor))
        filters.add(AutomationEntitiesAttributesGeneratedTCFilter(data, processor))
        filters.add(AutomationEntitiesIntegrationTCFilter(data, processor))
    }
}

class RulePipelineBuilder(inputKeyValues: MutableMap<String, Any?>, dataStructure: MutableList<DataStructure>? = null, processor: AIAdapter):
    AbstractUserStoryPipelineBuilder(inputKeyValues, dataStructure, processor) {

        override fun createFilters() {
            val AIAdapter = processor as AIAdapter
            val usprocessor = UserStoryProcessorAI(AIAdapter)

            //Specification
            filters.add(ListEntitiesFilter(data))
            filters.add(ListACRulesFilter(data))

            //Script Entity Unit
            filters.add(ListUnitEntityScriptFilter(data))
            filters.add(ListUnitEntityUpdateScriptFilter(data))

            //Script Rules Unit
            filters.add(ListCreateACRuleMockScriptFilter(data))
            filters.add(ListGetACRuleMockScriptFilter(data))
            filters.add(ListGetAllACRuleMockScriptFilter(data))
            filters.add(ListUpdateACRuleMockScriptFilter(data))
            filters.add(ListPartialUpdateACRuleMockScriptFilter(data))
            filters.add(ListDeleteACRuleMockScriptFilter(data))
            filters.add(ListFilterACRuleMockScriptFilter(data))

            //Script Endpoint Unit
            filters.add(ListCreateEndpointMockScriptFilter(data))
            filters.add(ListUpdateEndpointMockScriptFilter(data))
            filters.add(ListPartialUpdateEndpointMockScriptFilter(data))
            filters.add(ListGetEndpointMockScriptFilter(data))
            filters.add(ListGetAllEndpointMockScriptFilter(data))
            filters.add(ListDeleteEndpointMockScriptFilter(data))
            filters.add(ListFilterEndpointMockScriptFilter(data))
            //Script Generic Endpoint Unit
            filters.add(ListGenericEndpointMockScriptFilter(data))

            //Script UseCase Integration
            filters.add(ListIntegrationEntityScriptFilter(data))

            //Script Endpoint Integration
            filters.add(ListCreateEndpointScriptFilter(data))
            filters.add(ListUpdateEndpointScriptFilter(data))
            filters.add(ListPartialUpdateEndpointScriptFilter(data))
            filters.add(ListGetEndpointScriptFilter(data))
            filters.add(ListGetAllEndpointScriptFilter(data))
            filters.add(ListDeleteEndpointScriptFilter(data))
            filters.add(ListFilterEndpointScriptFilter(data))
            //Script Generic Endpoint Integration
            filters.add(ListGenericEndpointScriptFilter(data))

            //Test Data
            filters.add(ListEntitiesDataTestFilter(data, processor))

            filters.add(ListRuleDataTestFilter(data, processor))

            if(ApproachLevel.levels.contains("unit-endpoint-validations") or
                ApproachLevel.levels.contains("integration-endpoint-validations"))
            filters.add(ListRuleValidationDataTestFilter(data, processor))

            if(ApproachLevel.levels.contains("unit-endpoint-generic") or
                ApproachLevel.levels.contains("integration-endpoint-generic"))
            filters.add(ListGenericRulesDataTestFilter(data, processor))


            if(ApproachLevel.levels.contains("unit")) {
                //TC - Entity
                filters.add(ListUnitTestCaseEntityUpdateFilter(data))
                filters.add(ListUnitTestCaseEntityFilter(data))

                //TC Rule Unit
                filters.add(ListMockTestCaseCreateACRuleFilter(data))
                filters.add(ListMockTestCaseGetACRuleFilter(data))
                filters.add(ListMockTestCaseGetAllACRuleFilter(data))
                filters.add(ListMockTestCaseUpdateACRuleFilter(data))
                filters.add(ListMockTestCasePartialUpdateACRuleFilter(data))
                filters.add(ListMockTestCaseDeleteACRuleFilter(data))
                filters.add(ListMockTestCaseFilterACRuleFilter(data))


                //TC Rule Validation Unit
                filters.add(ListMockTestCaseValidationsCreateACRuleFilter(data))
                filters.add(ListMockTestCaseValidationsUpdateACRuleFilter(data))
                filters.add(ListMockTestCaseValidationsPartialUpdateACRuleFilter(data))
                filters.add(ListMockTestCaseValidationsGetACRuleFilter(data))
                filters.add(ListMockTestCaseValidationsGetAllACRuleFilter(data))
                filters.add(ListMockTestCaseValidationsDeleteACRuleFilter(data))
                filters.add(ListMockTestCaseValidationsFilterACRuleFilter(data))

                //TC Endpoint Unit
                filters.add(ListMockTestCaseCreateEndpointFilter(data))
                filters.add(ListMockTestCaseUpdateEndpointFilter(data))
                filters.add(ListMockTestCasePartialUpdateEndpointFilter(data))
                filters.add(ListMockTestCaseGetEndpointFilter(data))
                filters.add(ListMockTestCaseGetAllEndpointFilter(data))
                filters.add(ListMockTestCaseDeleteEndpointFilter(data))
                filters.add(ListMockTestCaseFilterEndpointFilter(data))
                //Generic Endpoint Unit
                filters.add(ListMockTestCaseGenericEndpointFilter(data))

                //TC Endpoint Validations Unit
                filters.add(ListMockTestCaseCreateEndpointValidationsFilter(data))
                filters.add(ListMockTestCaseUpdateEndpointValidationsFilter(data))
                filters.add(ListMockTestCasePartialUpdateEndpointValidationsFilter(data))
                filters.add(ListMockTestCaseGetEndpointValidationsFilter(data))
                filters.add(ListMockTestCaseGetAllEndpointValidationsFilter(data))
                filters.add(ListMockTestCaseDeleteEndpointValidationsFilter(data))
                filters.add(ListMockTestCaseFilterEndpointValidationsFilter(data))
            }

            if(ApproachLevel.levels.contains("integration")) {
                //TC UseCase Integration
                filters.add(ListIntegrationTestCaseEntityFilter(data))

                //TC Endpoint Integration
                filters.add(ListTestCaseCreateEndpointFilter(data))
                filters.add(ListTestCaseUpdateEndpointFilter(data))
                filters.add(ListTestCasePartialUpdateEndpointFilter(data))
                filters.add(ListTestCaseGetEndpointFilter(data))
                filters.add(ListTestCaseGetAllEndpointFilter(data))
                filters.add(ListTestCaseDeleteEndpointFilter(data))
                filters.add(ListTestCaseFilterEndpointFilter(data))
                //TC Generic Endpoint Integration
                filters.add(ListTestCaseGenericEndpointFilter(data))

                //TC Endpoint Integration Validation
                filters.add(ListTestCaseCreateEndpointValidationsFilter(data))
                filters.add(ListTestCaseUpdateEndpointValidationsFilter(data))
                filters.add(ListTestCasePartialUpdateEndpointValidationsFilter(data))
                filters.add(ListTestCaseGetEndpointValidationsFilter(data))
                filters.add(ListTestCaseGetAllEndpointValidationsFilter(data))
                filters.add(ListTestCaseDeleteEndpointValidationsFilter(data))
                filters.add(ListTestCaseFilterEndpointValidationsFilter(data))
            }

            filters.add(TestPlanFilter(data))

            if(ApproachLevel.levels.contains("unit")) {
                if (ApproachLevel.levels.contains("unit-entity"))
                    filters.add(AutomationEntitiesTestCasesFilter(data, processor))
                if (ApproachLevel.levels.contains("unit-entity-attributes-generated"))
                    filters.add(AutomationEntitiesAttributesGeneratedTCFilter(data, processor))
            }


    }
}




class UserStoryPipelineBuilder(inputKeyValues: MutableMap<String, Any?>, dataStructure: MutableList<DataStructure>? = null, processor: UserStoryProcessor):
    AbstractUserStoryPipelineBuilder(inputKeyValues, dataStructure, processor) {
    override fun createFilters() {
        filters.add(UserStoryFilter(data, processor))
    }
}

class ListUserStoryPipelineBuilder(inputKeyValues: MutableMap<String, Any?>, dataStructure: MutableList<DataStructure>? = null, processor: UserStoryProcessor,
                                   val ruleProcessor: RuleProcessor
):
    AbstractUserStoryPipelineBuilder(inputKeyValues, dataStructure, processor) {
    override fun createFilters() {
        filters.add(ValidateAndConvertListUSFilter(data, processor))
    }
}
