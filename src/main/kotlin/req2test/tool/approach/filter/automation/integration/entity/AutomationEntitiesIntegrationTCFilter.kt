package req2test.tool.approach.filter.automation.integration.entity

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.core.OutputFolder
import req2test.tool.approach.filter.testplan.RequestController
import req2test.tool.approach.processor.adapter.AIAdapter
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import req2test.tool.approach.entity.Attribute
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.Script

class AutomationEntitiesIntegrationTCFilter (data: MutableMap<String, Any?>, processor: Any) : Filter(data, processor) {
    private val AIAdapter = processor as AIAdapter
    private var entities: List<Entity> = ArrayList()
    override fun filterKeyInput(): String {
        return "ListEntityScriptIntegration"
    }

    override fun filterName(): String {
        return "AutomationEntitiesIntegrationTCFilter"
    }

    override fun filterKeyOutput(): String {
        return "AutomationIntegrationEntity"
    }

    private fun createEntitiesDeclarations(entities: List<Entity>): String {
        var declaration = ""
        entities.forEach { entity ->
            declaration += "\n${entity.name}\n"
            entity.attributes.forEach {
                if(!it.generatedDB) {
                    declaration += "- ${it.name}: ${it.type.replace("%", "")}\n"
                }
            }
        }
        return declaration
    }
    private fun generateAutomatedEntityTestClass(script: Script, entities: List<Entity>): String {
        val entity = script.reference as Entity

        val testClassName = "${entity.name}IntegrationAttributeTest"

        var prompt = """As a test class generator, implement the integration test cases below using Java, Spring Boot, JUnit5, Testcontainers, and Database Rider.
    For each test case, generate only the initial database dataset before the test execution (i.e., the initial state), and not the final state. The asserts must use JUnit5."""
    
        prompt += "Create the class $testClassName.\n" +
                "The tests must use:" +
                "\nthe class Create${entity.name}UseCase" +
//                "\nthe class ${entity.name}Repository" +
                ".\n\n Create automated test for each test case bellow:\n"

        script.testCasesAttributeGenerated.forEach {tc ->
            val attribute = tc.expectedResultReference as Attribute
            prompt += "- ${attribute.name} as ${attribute.type} and with the following validation: ${attribute.generatedFormula}\n\n"
//            prompt += "\nGiven a valid ${entity.name} below:\n"
            prompt += "consider tje following preconditions:\n"
            prompt += "${tc.preconditions}\n"
            prompt += "Use the following data as execute method parameters.\n"
            prompt += "${entity.name} = ${tc.dataTest.input}\n"
//            prompt += "Create a unit test with JUnit5 to validate:\n"
            prompt += "The test must create and save ${entity.name} and validate if ${attribute.name} was generated correctly.\n"
        }

        prompt += "Consider the existing classes (Do not implement them):\n"
        val entitiesDeclarations = createEntitiesDeclarations(entities)

        prompt += """
             Provide:
        For each test case, If the initial database is not empty, create a single dataset file (YAML format) with the initial database state before test execution
        Use the input declared as parameters of the method/endpoint
        Do not create a YAML dataset or use the @DataSet annotation when the dataset is empty. For example, if the database should be initialized with no data (e.g., {}), skip the annotation entirely.
        Each YAML must start with the name of the dataset as a comment. Ex: #dataset/$testClassName-init-dataset-tc1.yml #dataset/$testClassName-init-dataset-tc2.yml 
        The dataset must be in the folder dataset. Ex: dataset/$testClassName-init-1.yml dataset/$testClassName-init-2.yml
        The count in the final YAML file name must reset in each class, starting at 1.
	    Add all necessary configuration in the class to create the database container with postgres
            """
        val classes = entities.map { it.name }
        prompt += entitiesDeclarations
        prompt += "\nConsider type Date as LocalDate. Consider type Time as LocalTime.\n"

        prompt += """Structure each test case using the Arrange-Act-Assert pattern: 
                        first set up the necessary objects and data (Arrange), 
                        then execute the operation being tested (Act), 
                        and finally verify the result (Assert).
                        Ensure all model classes are imported from ${OutputFolder.packageName}.model."
                        Ensure all exception classes are imported from ${OutputFolder.packageName}.exception."
                        Ensure all usecase classes are imported from ${OutputFolder.packageName}.usecase."
                        Add all necessary imports (e.g. Asserts and verify from JUnit5, LocalDate)."
                        """

        prompt + "Prefer using a single assert per test. Use multiple asserts only when strictly necessary.\n"
        prompt += "\nCreate only the $testClassName class as output from this prompt. \n\nThe classes $classes already exist. Do not create them.\n\n"

        prompt = prompt.replace("\n", "\\n")

        val aiRequest: () -> String = {
            AIAdapter.getAnswerFromQuestion(prompt)
        }

        var result = RequestController.controlledRequest(aiRequest)

        return result
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


    fun getClassName(javaClassText: String): String? {
        val regex = Regex("""\bclass\s+(\w+)""")
        return regex.find(javaClassText)?.groupValues?.get(1)
    }

    override fun compute(input: Any): Any? {
        val prompts = ArrayList<String>()
        val scripts = input as List<Script>
        try {
            entities = data["ListEntities"] as List<Entity>
        } catch (ex: Exception) {
            logs.add(Log.createCriticalError("AutomationEntitiesIntegrationTCFilter", "List Entities not found"))

        }
        scripts.forEach {script ->

            println("SCRIPT: " + script.testCasesAttributeGenerated)
            if(script.testCasesAttributeGenerated.size > 0) {
                val result = generateAutomatedEntityTestClass(script, entities)
                val classFile = extractJava(result)

                val yamls = extractYamlMaps(result)
                yamls.forEach {it ->
                    TestPlanGenerator.createFile(it.value, OutputFolder.basePath + "output/integration/entity/", "${it.key}")
                }
                val className = getClassName(classFile)
                script.automatedTestCases.add(classFile)
                TestPlanGenerator.createFile(classFile, OutputFolder.basePath + "output/integration/entity/", "${className}.java")
            }
        }

        throwsCriticalErrors()

        return scripts
    }
}