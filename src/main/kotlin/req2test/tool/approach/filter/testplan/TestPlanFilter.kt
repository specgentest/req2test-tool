package req2test.tool.approach.filter.testplan

import req2test.tool.approach.core.APIKeys
import req2test.tool.approach.core.ApproachLevel
import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.OutputFolder
import req2test.tool.approach.filter.ac.rule.type.RuleVariable
import java.io.File
import java.io.PrintWriter
import req2test.tool.approach.processor.adapter.ChatGPTAdapter
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Rule
import req2test.tool.approach.entity.Script
import req2test.tool.approach.entity.TestPlan

class ScriptLevel(val level: String) {
    val scriptGroups: MutableMap<String, ScriptGroup> = HashMap()
}
class ScriptGroup(val name: String) {
    val scripts: MutableMap<String, List<Script>> = HashMap()

    fun getAllScripts(): List<Script> {
        return scripts.values.flatten()
    }

}

class RequestController {
    companion object {
        var delay: Long = 10000
        var delayBlock: Long = 90000
        var requestCount = 0

        fun waitDelay(){
            if(requestCount == 2) {
                println("Aguardando $delayBlock ms...")
                Thread.sleep(delayBlock)
                requestCount = 0
            }
            else {
                println("Aguardando $delay ms...")
                Thread.sleep(delay)
            }
        }

        fun controlledRequest(action: () -> String): String {
            waitDelay()

            var backoff = 60000L
            var result = ""
            for (attempt in 1..3) {
                try {
                    result = action()
                    break
                } catch (e: Exception) {
                    println("Erro na requisição: ${e.message}. Tentando novamente em $backoff ms.")
                    Thread.sleep(backoff)
                    backoff *= 2
                }
            }
            requestCount++
            return result
        }

    }

}
class TestPlanFilter(data: MutableMap<String, Any?>) : Filter(data, null) {

    override fun filterKeyInput(): String {
        return "ListUnitEntityScript"
    }

    override fun filterName(): String {
        return "TestPlanFilter"
    }

    override fun filterKeyOutput(): String {
        return "TestPlan"
    }

    private fun createUnitScripts(): ScriptLevel {
        val unitScripts = ScriptLevel("unit")
        val scriptEntityGroup = ScriptGroup("Entity")
        val scriptACRuleGroup = ScriptGroup("ACRule")
        val scriptEndpointGroup = ScriptGroup("Endpoint")
        val scriptGenericEndpointGroup = ScriptGroup("GenericEndpoint")

        unitScripts.scriptGroups["Entity"] = scriptEntityGroup
        unitScripts.scriptGroups["ACRule"] = scriptACRuleGroup
        unitScripts.scriptGroups["Endpoint"] = scriptEndpointGroup
        unitScripts.scriptGroups["GenericEndpoint"] = scriptGenericEndpointGroup

        scriptEntityGroup.scripts["ListUnitEntityScript"] = data["ListUnitEntityScript"] as List<Script>
        scriptEntityGroup.scripts["ListUnitEntityUpdateScript"] = data["ListUnitEntityUpdateScript"] as List<Script>

        scriptACRuleGroup.scripts["ListCreateACRuleMockScript"] = data["ListCreateACRuleMockScript"] as List<Script>
        scriptACRuleGroup.scripts["ListGetACRuleMockScript"] = data["ListGetACRuleMockScript"] as List<Script>
        scriptACRuleGroup.scripts["ListGetAllACRuleMockScript"] = data["ListGetAllACRuleMockScript"] as List<Script>
        scriptACRuleGroup.scripts["ListUpdateACRuleMockScript"] = data["ListUpdateACRuleMockScript"] as List<Script>
        scriptACRuleGroup.scripts["ListPartialUpdateACRuleMockScript"] = data["ListPartialUpdateACRuleMockScript"] as List<Script>
        scriptACRuleGroup.scripts["ListDeleteACRuleMockScript"] = data["ListDeleteACRuleMockScript"] as List<Script>
        scriptACRuleGroup.scripts["ListFilterACRuleMockScript"] = data["ListFilterACRuleMockScript"] as List<Script>

        scriptEndpointGroup.scripts["ListCreateEndpointMockScript"] = data["ListCreateEndpointMockScript"] as List<Script>
        scriptEndpointGroup.scripts["ListUpdateEndpointMockScript"] = data["ListUpdateEndpointMockScript"] as List<Script>
        scriptEndpointGroup.scripts["ListPartialUpdateEndpointMockScript"] = data["ListPartialUpdateEndpointMockScript"] as List<Script>
        scriptEndpointGroup.scripts["ListGetEndpointMockScript"] = data["ListGetEndpointMockScript"] as List<Script>
        scriptEndpointGroup.scripts["ListGetAllEndpointMockScript"] = data["ListGetAllEndpointMockScript"] as List<Script>
        scriptEndpointGroup.scripts["ListDeleteEndpointMockScript"] = data["ListDeleteEndpointMockScript"] as List<Script>
        scriptEndpointGroup.scripts["ListFilterEndpointMockScript"] = data["ListFilterEndpointMockScript"] as List<Script>

        scriptGenericEndpointGroup.scripts["ListGenericEndpointMockScript"] = data["ListGenericEndpointMockScript"] as List<Script>

        return unitScripts
    }

    private fun createIntegrationScripts(): ScriptLevel {
        val integrationScripts = ScriptLevel("integration")

        val scriptEntityGroup = ScriptGroup("Entity")
        val scriptEndpointGroup = ScriptGroup("Endpoint")
        val scriptGenericEndpointGroup = ScriptGroup("GenericEndpoint")

        integrationScripts.scriptGroups["Entity"] = scriptEntityGroup
        integrationScripts.scriptGroups["Endpoint"] = scriptEndpointGroup
        integrationScripts.scriptGroups["GenericEndpoint"] = scriptGenericEndpointGroup

        //scriptEntityGroup.scripts["ListIntegrationEntityScript"] = data["ListIntegrationEntityScript"] as List<Script>

        scriptEndpointGroup.scripts["ListCreateEndpointScript"] = data["ListCreateEndpointScript"] as List<Script>
        scriptEndpointGroup.scripts["ListUpdateEndpointScript"] = data["ListUpdateEndpointScript"] as List<Script>
        scriptEndpointGroup.scripts["ListPartialUpdateEndpointScript"] = data["ListPartialUpdateEndpointScript"] as List<Script>
        scriptEndpointGroup.scripts["ListGetEndpointScript"] = data["ListGetEndpointScript"] as List<Script>
        scriptEndpointGroup.scripts["ListGetAllEndpointScript"] = data["ListGetAllEndpointScript"] as List<Script>
        scriptEndpointGroup.scripts["ListDeleteEndpointScript"] = data["ListDeleteEndpointScript"] as List<Script>
        scriptEndpointGroup.scripts["ListFilterEndpointScript"] = data["ListFilterEndpointScript"] as List<Script>

        scriptGenericEndpointGroup.scripts["ListGenericEndpointScript"] = data["ListGenericEndpointScript"] as List<Script>

        return integrationScripts
    }

    val textUnityACRuleTC: (Script) -> String = { script ->
        var textTestCases = ""
        var ctCount = 1
        script.testCases.forEach {tc ->
            var tcDescription = tc.expectedResult

            var testName = tcDescription
            if(tc.testName != null)
               testName = tc.testName!!

            textTestCases += "test description: ${testName}\n"
//            textTestCases += "TC-${ctCount++}\t\t\t\t\t\t| test case name: ${testName}\n"
            textTestCases += "preconditions\t\t\t\t| ${tc.preconditions}\n"
            textTestCases += "Input of the Endpoint\t\t\t\t\t\t| ${tc.dataTest.input?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }}\n"
            textTestCases += "Steps\t\t\t\t\t\t| ${tc.steps.map { "\n\t\t\t\t\t\t\t$it" }}\n"
            textTestCases += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
        }
        textTestCases
    }

    val textUnityGenericEndpointTC: (Script) -> String = { script ->
        var textTestCases = ""
        var ctCount = 1
        script.testCases.forEach {tc ->
            var tcDescription = tc.expectedResult

            textTestCases += "test description: ${tcDescription}\n"
//            textTestCases += "TC-${ctCount++}\t\t\t\t\t\t| ${tcDescription}\n"
            textTestCases += "preconditions\t\t\t\t| ${tc.preconditions}\n"
            val rule = script.reference as Rule
            val inputs = (rule.attributes["input"] as List<RuleVariable>).map { it.name }
            println(inputs)
            val variablesInput = tc.dataTest.input
                ?.filter { inputs.contains(it.key) }
                ?.map { "\n\t\t\t\t\t\t\t${it.key}: ${it.value} " }

            textTestCases += "Input\t\t\t\t\t\t| ${variablesInput}\n"
            textTestCases += "Steps\t\t\t\t\t\t| ${tc.steps.map { "\n\t\t\t\t\t\t\t$it" }}\n"
            textTestCases += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
        }
        textTestCases
    }


    val textIntegrationEndpointTC: (Script) -> String = { script ->
        var textTestCases = ""
        var ctCount = 1
        script.testCases.forEach {tc ->
            var tcDescription = tc.expectedResult

            textTestCases += "test description: ${tcDescription}\n"
            textTestCases += "preconditions\t\t\t\t| ${tc.preconditions}\n"

            val inputs = script.inputAllAttributes?.map { it.name } as List<String>

            val variablesInput = tc.dataTest.input
                ?.filter { inputs.contains(it.key) }
                ?.map { (key, value) ->
                    val valueStr = value.toString()
                    val newValue = if (valueStr.trim().startsWith("{") && valueStr.contains("id")) {
                        Regex("""id\w+=([\w\-]+)""")
                            .find(valueStr)
                            ?.value
                            ?.let { "{$it}" } ?: valueStr
                    } else {
                        valueStr
                    }
                    "\n\t\t\t\t\t\t\t$key: $newValue"
                }

            textTestCases += "Input\t\t\t\t\t\t| ${variablesInput}\n"
            textTestCases += "Steps\t\t\t\t\t\t| ${tc.steps.map { "\n\t\t\t\t\t\t\t$it" }}\n"
            textTestCases += "Expected Result\t\t\t\t| ${tc.expectedResult}\n\n"
        }
        textTestCases
    }

    private fun automateUnitACRuleTCs(scripts: List<Script>, validations: Boolean = false): List<String> {
        val context = "As a test class generator, implement the following unit test cases using Java, JUnit5 and Mockito." +
                "Follow strict the steps provided in test case." +
                "Generate only the test class—no additional artifacts, comments, or explanatory text.\n"

        var basePrompt = ""

        var suffix = "UnitTest"
        var folderPath = "output/unit/rule/"
        if(validations) {
            folderPath = "output/unit/rule-validations/"
            suffix = "ValidationsUnitTest"
        }

        return automateTCs(scripts, suffix, context, basePrompt, folderPath, textUnityACRuleTC)
    }

    private fun automateUnitEndpointTCs(scripts: List<Script>, validations: Boolean = false): List<String> {
        val context = """As a test class generator, implement the following unit test cases using Java, Spring Boot, JUnit5, mockMvc and Mockito.
                Follow strict the steps provided in test case.
                Generate only the test class—no additional artifacts, comments, or explanatory text.
                When verifying that the response contains a message, use containsString (e.g .andExpect(content().string(containsString("string")).
                **When the return is a JSON, use MockMvcResultMatchers.jsonPath to verify its elements.**.
                **When using @WebMvcTest, always use @MockBean to mock dependencies of the controller, instead of @Mock.**
                All test methods using MockMvc must declare throws Exception.
                When mocking an object to throw an exception, always use doThrow; e.g., doThrow(new DataBaseException("")).when(operationUseCase).execute().
                 
                 Use the following template for endpoint test classes with mocks:

                 @WebMvcTest(ClassController.class)
                 public class ClassControllerUnitTest {

                     @Autowired
                     private MockMvc mockMvc;

                     @MockBean
                     private ClassUseCase classUseCase;

                 }
                 
                """.trimIndent()


        var basePrompt = ""

        var suffix = "UnitTest"
        var folderPath = "output/unit/endpoint/"
        if(validations) {
            folderPath = "output/unit/endpoint-validations/"
            suffix = "ValidationsUnitTest"
        }
        return automateTCs(scripts, suffix, context, basePrompt, folderPath, textUnityACRuleTC, true)
    }

    private fun automateUnitGenericEndpointTCs(scripts: List<Script>): List<String> {
        val context = """As a test class generator, implement the unit test cases below using Java, Spring Boot, JUnit, mockMvc, and Mockito.
        Provide:
	        The test class with the corresponding methods, executing only the steps listed in the test script.
        
        When one of the input JSON elements is another object, parameterize only the object’s ID (for example: {obj: 1} instead of {obj: {idObject: 1, name: objectname}}).

        Generate only the test class—no additional artifacts, comments, or explanatory text.
        
        When verifying that the response contains a message, use containsString (e.g .andExpect(content().string(containsString("string")). 
        **When the return is a JSON, use MockMvcResultMatchers.jsonPath to verify its elements.**.
        **When using @WebMvcTest, always use @MockBean to mock dependencies of the controller, instead of @Mock.**
        The request input is a JSON object, represent the request parameters using a Map<String, Object>.
        All test methods using MockMvc must declare throws Exception.   
        
        Use the following template for endpoint test classes with mocks:

        @WebMvcTest(ClassController.class)
        public class ClassControllerUnitTest {

            @Autowired
            private MockMvc mockMvc;

            @MockBean
            private ClassUseCase classUseCase;

        }
        
            """.trimIndent()
        val basePrompt = ""

        val suffix = "UnitTest"
        val folderPath = "output/unit/endpoint-generic/"

        return automateTCs(scripts, suffix, context, basePrompt, folderPath, textUnityGenericEndpointTC, true)
    }


    private fun automateIntegrationEndpointTCs(scripts: List<Script>, validations: Boolean = false): List<String> {

        val context = """As a test class generator, implement the integration test cases below using Java, Spring Boot, Rest Assured, Testcontainers, and Database Rider.
    For each test case, generate only the initial database dataset before the test execution (i.e., the initial state), and not the final state.
    
    Use the strict the 'Input of the Endpoint' as input of the test case.
    
    Follow strict the steps provided in test case.
    
    Provide:
        For each test case, If the initial database is not empty, create a single dataset file (YAML format) with the initial database state before test execution
        Use the input declared as parameters of the method/endpoint
        Do not create a YAML dataset or use the @DataSet annotation when the dataset is empty. For example, if the database should be initialized with no data (e.g., {}), skip the annotation entirely.
        **Each YAML must start with ```yaml**
        **Each YAML snippet must be enclosed within its own separate code block, starting with yaml and ending with**
        **Each test case must have its own dataset**
        Each YAML must start with the name of the dataset as a comment. Ex: #dataset/%testclass%-init-1.yml #dataset/%testclass%-init-2.yml 
        The dataset must be in the folder dataset. Ex: dataset/%testclass%-init-1.yml dataset/%testclass%-init-2.yml
        The count in the final YAML file name must reset in each class, starting at 1.
        The dataset declaration must following the format: @DataSet(value = "dataset/datasetname-init-2.yml", cleanBefore = true).
        When the test case does not have a dataset, use @DataSet(cleanBefore = true).
        Relative dates must be represented in the dataset using square brackets, for example:
        	currentDate: "[now]"
	        futureDate: "[now+10y]"
	        pastMonth: "[now-1M]"
	        yesterday: "[now-1d]"
        These are just examples—other variations are possible.
        
	    The test class with the corresponding methods, executing only the steps listed in the test script.
        It is not necessary inject controller (e.g. @Autowired private EntityController entityController).
        When the request input is a JSON object, represent the request parameters using a Map<String, Object>. 
        For date/time values (e.g., LocalDate, LocalDateTime), always convert them to String using .toString() before adding them to the map.
        **This test class does not import any model, repository, exception, use case, or controller classes.**
        **Dependency injection must not be used in this test class; avoid using @Autowired.**
                
        As initial database you will receive JSON input that can contain nested objects. 
        Your task is to generate a YAML dataset compatible with Database Rider.
        
        Rules:
        1. Never nest objects in the YAML.
        2. Each object type (e.g., Author, Book) must be a top-level YAML key (table).
        3. Use foreign key references via *_id fields instead of nesting objects.
        4. Maintain correct YAML indentation.
        5. Use primitive types only (string, integer, float, boolean, date in yyyy-MM-dd format).
        6. Output ONLY the YAML, with no explanation or comments.
        
        Example input (JSON):
        {
          "Book": [
            {
              "idBook": 1,
              "title": "1984",
              "publicationYear": 1949,
              "author": {
                "idAuthor": 1,
                "name": "George Orwell",
                "birthDate": "1903-06-25"
              }
            }
          ]
        }
        
        Expected YAML output:
        
        Author:
          - idAuthor: 1
            name: "George Orwell"
            birthDate: 1903-06-25
        
        Book:
          - idBook: 1
            title: "1984"
            publicationYear: 1949
            idAuthor: 1


    If the filter field is an object, use only the object’s id as the input parameter and for the lookup.
    When one of the input JSON elements is another object, parameterize only the object’s ID (for example: {obj: 1} instead of {obj: {idObject: 1, name: objectname}}).

    Use the following template for integration test classes:    
    
    import org.springframework.boot.test.web.server.LocalServerPort;

    @ExtendWith(SpringExtension.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DBRider
    public class ClassControllerIntegrationTest {
    
        @LocalServerPort
        private int port;
    
        @BeforeEach
        public void setUp() {
            RestAssured.port = port;
        }
    }
   
    Generate only the test class—no additional artifacts, comments, or explanatory text.
    """.trimIndent()
        var basePrompt = ""

        var suffix = "IntegrationTest"
        var folderPath = "output/integration/endpoint/"
        if(validations) {
            folderPath = "output/integration/endpoint-validations/"
            suffix = "ValidationsIntegrationTest"
        }
        return automateTCs(scripts, suffix, context, basePrompt, folderPath, textIntegrationEndpointTC, true)
    }

    private fun automateIntegrationGenericEndpointTCs(scripts: List<Script>): List<String> {
        val context = """As a test class generator, implement the integration test cases below using Java, **Spring Boot, Rest Assured, database H2, and Database Rider**.
    Do not call other methods, use only Rest Assured to call the endpoint and verify the result.
    Create the necessary files to initialize the database with Database Rider.

    Provide:
        Do not create a YAML dataset or use the @DataSet annotation when the dataset is empty. For example, if the database should be initialized with no data (e.g., {}), skip the annotation entirely.
        **Each YAML must start with ```yaml**
        **Each YAML snippet must be enclosed within its own separate code block, starting with yaml and ending with**
        **Each test case must have its own dataset**
        Each YAML must start with the name of the dataset as a comment. Ex: #dataset/%testclass%-init-1.yml #dataset/%testclass%-init-2.yml 
        The dataset must be in the folder dataset. Ex: dataset/%testclass%-init-1.yml dataset/%testclass%-init-2.yml
        The count in the final YAML file name must reset in each class, starting at 1.
        The dataset declaration must following the format: @DataSet(value = "dataset/datasetname-init-2.yml", cleanBefore = true).
        When the test case does not have a dataset, use @DataSet(cleanBefore = true).
        Relative dates must be represented in the dataset using square brackets, for example:
        	currentDate: "[now]"
	        futureDate: "[now+10y]"
	        pastMonth: "[now-1M]"
	        yesterday: "[now-1d]"
        These are just examples—other variations are possible.
        
	    The test class with the corresponding methods, executing only the steps listed in the test script.
        It is not necessary inject controller (e.g. @Autowired private EntityController entityController).
        When the request input is a JSON object, represent the request parameters using a Map<String, Object>. 
        For date/time values (e.g., LocalDate, LocalDateTime), always convert them to String using .toString() before adding them to the map.
        **This test class does not import any model, repository, exception, use case, or controller classes.**
        **Dependency injection must not be used in this test class; avoid using @Autowired.**
                
        As initial database you will receive JSON input that can contain nested objects. 
        Your task is to generate a YAML dataset compatible with Database Rider.
        
        Rules:
        1. Never nest objects in the YAML.
        2. Each object type (e.g., Author, Book) must be a top-level YAML key (table).
        3. Use foreign key references via *_id fields instead of nesting objects.
        4. Maintain correct YAML indentation.
        5. Use primitive types only (string, integer, float, boolean, date in yyyy-MM-dd format).
        6. Output ONLY the YAML, with no explanation or comments.
        
        Example input (JSON):
        {
          "Book": [
            {
              "idBook": 1,
              "title": "1984",
              "publicationYear": 1949,
              "author": {
                "idAuthor": 1,
                "name": "George Orwell",
                "birthDate": "1903-06-25"
              }
            }
          ]
        }
        
        Expected YAML output:
        
        Author:
          - idAuthor: 1
            name: "George Orwell"
            birthDate: 1903-06-25
        
        Book:
          - idBook: 1
            title: "1984"
            publicationYear: 1949
            idAuthor: 1

    If the filter field is an object, use only the object’s id as the input parameter and for the lookup.
    When one of the input JSON elements is another object, parameterize only the object’s ID (for example: {obj: 1} instead of {obj: {idObject: 1, name: objectname}}).

    Use the following template for integration test classes:    
    
    import org.springframework.boot.test.web.server.LocalServerPort;

    @ExtendWith(SpringExtension.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DBRider
    public class ClassControllerIntegrationTest {
    
        @LocalServerPort
        private int port;
    
        @BeforeEach
        public void setUp() {
            RestAssured.port = port;
        }
    }
    
    Generate only the test class—no additional artifacts, comments, or explanatory text.
    """.trimIndent()
        val basePrompt = ""

        val suffix = "IntegrationTest"
        val folderPath = "output/integration/endpoint-generic/"

        return automateTCs(scripts, suffix, context, basePrompt, folderPath, textUnityGenericEndpointTC, true)
    }

    private fun automateTCs(scripts: List<Script>,
                            suffix: String, context:
                    String, basePrompt:
                    String,
                            folderPath: String,
                            textTCConversion: (script: Script) -> String,
                            controllerLayer: Boolean = false,
                    ): List<String> {
        val classes = ArrayList<String>()
        val AIAdapter = ChatGPTAdapter(APIKeys.ChatGPT)
        scripts.forEach { script ->

            var testClassName = script.testArchitecture?.ruleLayerClass + suffix
            if(controllerLayer) {
                val action = script.testArchitecture?.ruleAction
                testClassName = action + script.testArchitecture?.controllerLayerClass + suffix
            }

            val basePromptWithTestClass = basePrompt.replace("%testclass%", testClassName)
            val textTestCases = textTCConversion(script)

            var prompt = basePromptWithTestClass +
                    "\nConsider type Date as LocalDate. Consider type Time as LocalTime.\n" +
                    """Structure each test case using the Arrange-Act-Assert pattern: 
                        first set up the necessary objects and data (Arrange), 
                        then execute the operation being tested (Act), 
                        and finally verify the result (Assert).
                        Add all necessary imports (e.g. Asserts and verify from JUnit5, LocalDate).
                        Always preserve the original data type. Do not convert numeric strings to integers.
                        Use UUID.fromString to initialize UUID.
                        All objects used in the test class must be imported.
                        When necessary:
                         - Ensure all model classes are imported from ${OutputFolder.packageName}.model
                         - Ensure all exception classes are imported from ${OutputFolder.packageName}.exception
                         - Ensure all repository classes are imported from ${OutputFolder.packageName}.repository
                         - Ensure all use case classes are imported from ${OutputFolder.packageName}.usecase
                         - Ensure all controller classes are imported from ${OutputFolder.packageName}.controller
                        
                        You must strictly follow these rules:
                        
                        Method Names
                            1.	Do not rename, replace, or modify any method names explicitly described in the test case or scenario.
                            2.	The following method names must be preserved exactly as they are:
                            - save
                            - findById
                            - getAll
                            - update
                            - partialUpdate
                            - deleteById
                            - filter
                            3.	You must not make any of the following substitutions:
                            - **Do not replace update with save**.
                            - Do not replace findById with getById.
                            - Do not replace deleteById with remove.
                            - Do not change partialUpdate to update or save.
                            - Do not change filter to search or any synonym.
                            4.	Preserve all method names, class names, and their signatures exactly as provided, even if they seem unusual or non-standard.
                            5.	Any deviation from these instructions constitutes a critical error and is unacceptable.
                        
                        Exception Names
                            6.	Do not rename, replace, generalize, or remove any exception names explicitly mentioned in the test case or code context.
                            7.	The following exception names must be preserved exactly as they are:
                            - DataIntegrityViolationException
                            8.	You must not:
                            - Replace DataIntegrityViolationException with a more general or different exception.
                            - Remove exception handling logic or change its semantics.
                            9.	Exception names, their handling, and any messages must be preserved exactly as provided in the test case or scenario.
                        
                        General
                            10.	You may only improve clarity, structure, or formatting of the text.
                            11.	You must not alter the semantics, logic, method names, or exception names in any way.
                            12.	Always prioritize fidelity to the original test case over generalization, simplification, or stylistic improvements.

                        Variable types:
                            1. Do not change varibales type: e.g, if the type is float do not change to float
                        
                        For a request test case: When the JSON input request includes a related object (e.g., Author), it must refer only to the object’s ID, not the full object representation.
                        Correct JSON input request
                        
                        {
                              "id": 1,
                              "title": "1984",
                              "publicationYear": 1949,
                              "idAuthor": 1
                        }
                        
                        Incorrect JSON input request
                        
                        {
                              "id": 1,
                              "title": "1984",
                              "publicationYear": 1949,
                              "author": {"idAuthor": 1}
                        }
                        
                        **The related object’s field must always use id as a prefix (e.g., idAuthor).**
                        
                        If time fields is a string in response assert request, then represent it in the format HH:mm (e.g., 09:00:00).

                        Use test description as base to test case name.
                        """ +
                    """Below are the test cases for the class $testClassName:
                
            """.trimIndent()
            prompt+= textTestCases

            val aiRequest: () -> String = {
                AIAdapter.getAnswerFromQuestion(prompt, context)
            }

            var result = RequestController.controlledRequest(aiRequest)

            val returnedClass = extractJava(result)
            val yamls = extractYamlMaps(result)
            yamls.forEach {it ->
                TestPlanGenerator.createFile(it.value, OutputFolder.basePath + folderPath, "${it.key}")
            }
            TestPlanGenerator.createFile(returnedClass, OutputFolder.basePath + folderPath, "${testClassName}.java")

            classes.add(returnedClass)
        }
        return classes
    }

    fun extractJava(text: String): String {
        // Regex para capturar código entre ```java e ```
        val regex = Regex("```java(.*?)```", RegexOption.DOT_MATCHES_ALL)

        // Procura o match e captura o grupo 1 (o código Java)
        val matchResult = regex.find(text)

        // Se encontrado, retorna o código capturado, senão retorna o texto original
        return matchResult?.groups?.get(1)?.value?.trim() ?: text.trim()
    }

    fun extractYamlMaps(text: String): Map<String, String> {
        val regex = Regex("```yaml(.*?)```", RegexOption.DOT_MATCHES_ALL)
        val matches = regex.findAll(text)

        return matches.mapNotNull { match ->
            val content = match.groups[1]?.value?.trim() ?: return@mapNotNull null
            val lines = content.lines()

            if (lines.isEmpty()) return@mapNotNull null

            val firstLine = lines.first().trim()
            val yamlName = if (firstLine.startsWith("#")) {
                firstLine.removePrefix("#").trim()
            } else {
                return@mapNotNull null  // Ignora blocos que não têm nome no primeiro comentário
            }

            val yamlContent = lines.drop(1).joinToString("\n").trim()

            yamlName to yamlContent
        }.toMap()
    }

    fun extractYaml(text: String): String {
        // Regex para capturar código entre ```yaml e ```
        val regex = Regex("```yaml(.*?)```", RegexOption.DOT_MATCHES_ALL)

        // Procura o match e captura o grupo 1 (o conteúdo YAML)
        val matchResult = regex.find(text)

        // Se encontrado, retorna o YAML capturado, senão retorna o texto original
        return matchResult?.groups?.get(1)?.value?.trim() ?: text.trim()
    }



    override fun compute(input: Any): Any? {
        val unitScripts = createUnitScripts()
        val integrationScripts = createIntegrationScripts()

        val scripts = unitScripts.scriptGroups["ACRule"]?.getAllScripts() as List<Script>
        val scriptsACRules = scripts.filter { it.reference is Entity }
        val scriptsACRuleValidations = scripts.filter { it.reference is Rule }
        if(ApproachLevel.levels.contains("unit")) {
            if (ApproachLevel.levels.contains("unit-usecase"))
                automateUnitACRuleTCs(scriptsACRules)
            if (ApproachLevel.levels.contains("unit-usecase-validations"))
                automateUnitACRuleTCs(scriptsACRuleValidations, true)
        }

        val scriptsAllEndpoints = unitScripts.scriptGroups["Endpoint"]?.getAllScripts() as List<Script>

        val scriptsEndpoint = scriptsAllEndpoints.filter { it.reference is Entity }
        val scriptsEndpointValidations = scriptsAllEndpoints.filter { it.reference is Rule }

        if(ApproachLevel.levels.contains("unit")) {
            if (ApproachLevel.levels.contains("unit-endpoint"))
                automateUnitEndpointTCs(scriptsEndpoint)
            if (ApproachLevel.levels.contains("unit-endpoint-validations"))
                automateUnitEndpointTCs(scriptsEndpointValidations, true)

            val scriptsGenericEndpoints = unitScripts.scriptGroups["GenericEndpoint"]?.getAllScripts() as List<Script>
            if (ApproachLevel.levels.contains("unit-endpoint-generic"))
                automateUnitGenericEndpointTCs(scriptsGenericEndpoints)
        }

        val integrationScriptsAllEndpoints = integrationScripts.scriptGroups["Endpoint"]?.getAllScripts() as List<Script>

        val integrationScriptsEndpoint = integrationScriptsAllEndpoints.filter { it.reference is Entity }
        val integrationScriptsEndpointValidations = integrationScriptsAllEndpoints.filter { it.reference is Rule }

        if(ApproachLevel.levels.contains("integration")) {
            if (ApproachLevel.levels.contains("integration-endpoint"))
                automateIntegrationEndpointTCs(integrationScriptsEndpoint)
            if (ApproachLevel.levels.contains("integration-endpoint-validations"))
                automateIntegrationEndpointTCs(integrationScriptsEndpointValidations, true)

            val integrationScriptsGenericEndpoints =
                integrationScripts.scriptGroups["GenericEndpoint"]?.getAllScripts() as List<Script>
            if (ApproachLevel.levels.contains("integration-endpoint-generic"))
                automateIntegrationGenericEndpointTCs(integrationScriptsGenericEndpoints)
        }

        val testPlan = TestPlan(scripts=scriptsEndpoint)
        return testPlan
    }

    private fun printToFile(testPlan: TestPlan) {

        val scripts = testPlan.scripts

        scripts.forEach { script ->
            println("Impriminido dados de " + script.reference)
            println(script.allDataTest)
        }


        var text = """
            \begin{table}[h!]
            \centering
            \begin{tabular}{|l|l|}
            \hline
        """
        var count = 1
        var ctCount = 1

        scripts.forEach { script ->
            val entity = script.reference as Entity
            text += """
                SCRIPT-${count++} & Creation of ${entity.name} \\ \hline
                Script objective & ${script.objective} \\ \hline
                Location & ${script.location} \\ \hline
                Steps & ${script.steps} \\ \hline
                Level & ${script.level.value} \\ \hline
                Technique & ${script.technique.value} \\ \hline
                Selection Criteria & ${script.inputTechniques.joinToString(", ") { it.value }} \\ \hline
            """

            script.testCases.forEach { tc ->
                val tcDescription = tc.dataTest.className

                text += """
                    TC-${ctCount++} & ${tcDescription} \\ \hline
                    Input & ${tc.dataTest.input?.map { "${it.key}: ${it.value}" }?.joinToString(", ")} \\ \hline
                    Expected Result & ${tc.expectedResult} \\ \hline
                """
            }

            script.testCasesAttributeGenerated.forEach { tc ->
                val tcDescription = if (tc.dataTest.isValidInput)
                    "Must create an instance of ${entity.name}"
                else
                    tc.dataTest.className

                text += """
                    TC-${ctCount++} & ${tcDescription} \\ \hline
                    Input & ${tc.dataTest.input?.map { "${it.key}: ${it.value}" }?.joinToString(", ")} \\ \hline
                    Expected Result & ${tc.expectedResult} \\ \hline
                """
            }
        }

        text += """
            \end{tabular}
            \caption{Test Case Scripts}
            \label{tab:test_case_scripts}
            \end{table}
        """

        scripts.forEach { script ->
            script.automatedTestCases.forEach {
                text += it
                text += "\n\n\n\n"
            }
        }

        println(text)
        createFile(text)
    }

    private fun createFile(content: String){
        val filePath = "plano.txt"
        val file = File(filePath)
        val writer = PrintWriter(file)
        writer.println(content)
        writer.close()
        println("Content saved in $filePath")
    }
}