package req2test.tool.approach.filter.datatest

import req2test.tool.approach.core.Filter
import req2test.tool.approach.processor.adapter.AIAdapter
import req2test.tool.approach.processor.adapter.ChatGPTAdapter
import req2test.tool.approach.processor.adapter.ChatGPTMessage
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONObject
import org.json.JSONArray
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.ListDataTest
import java.util.regex.Matcher

class EntityDataTestFilter (data: MutableMap<String, Any?>, processor: Any) : Filter(data, processor) {
    private val AIAdapter = processor as AIAdapter
    private var entities: MutableList<Entity> = ArrayList()
    private var countEntities = 1
    private var countAttributes = 1
    private val entitiesVariables = LinkedHashMap<String, Map<String, String>>()
    private val entitiesAllVariables = LinkedHashMap<String, Map<String, String>>()
    private val varDictionaryAttributes = LinkedHashMap<String, String>()
    private val varDictionaryEntities = LinkedHashMap<String, String>()

    override fun filterKeyInput(): String {
        return "Entity"
    }

    override fun filterName(): String {
        return ""
    }

    override fun filterKeyOutput(): String {
        return "EntityDataTest"
    }

    private fun entityDeclaration(entity: Entity): String {
        val attributes = entity.attributes
        var variables = HashMap<String, String>()
        var allVariables = HashMap<String, String>()
        val varEntity = "Entity$countEntities"
        varDictionaryEntities["%"+entity.name] = varEntity
        varDictionaryEntities[entity.name] = varEntity //Adicionei dia 31/03

        var declaration = "Given an $varEntity with the following attributes:\n"
        //Insert Entity attributes that are not generated
        attributes.forEach {
                val varAttribute = "var$countAttributes"
                val varAttribute2 = "Var$countAttributes"
                val type = it.type

                //Adiconei essa validação para impactar apenas o input que é solicitado
                if(!it.generatedDB and !it.generatedConstructor)
                    variables[varAttribute] = type

                allVariables[varAttribute] = type

                varDictionaryAttributes[varAttribute] = "${'$'}${it.name}"
                //TODO - Replace correctly
                varDictionaryAttributes[varAttribute2] = "${'$'}${it.name}"
                declaration += "- $varAttribute as $type and with the following validation: ["
                it.validations.forEach { it2 -> declaration += it2 + "; " }
                if(it.generatedDB or it.generatedConstructor)
                    declaration += "generated: ${it.generatedFormula}  ; "
                declaration += "]\n"
                countAttributes++
            //}
        }
        entitiesVariables[entity.name] = variables
        entitiesAllVariables[entity.name] = allVariables
        //Adding Entity validations
        declaration += "\nConsider the following validations for all attributes in $varEntity: \n"
        entity.entityValidations?.forEach {
            declaration += "- ${it.detailedRule}\n"
        }

        declaration += "\n"
        countEntities++
        return declaration
    }

    fun replaceValidationsVar(prompt: String): String {
        var texto = prompt

        // Inverte o mapa para que possamos substituir variáveis pelos nomes abreviados
        val invertedReplacements = varDictionaryAttributes.entries.associate { (key, value) -> value.drop(1) to key }

        // Ordena as chaves do mapa invertido por comprimento decrescente para evitar substituições parciais
        val sortedKeys = invertedReplacements.keys.sortedByDescending { it.length }

        // Substitui cada variável pela sua chave no mapa
        sortedKeys.forEach { key ->
            texto = texto.replace(".$key", ".${invertedReplacements[key]}" ?: key)
        }
        return texto
    }
    private fun createReturnDataTest(entity: Entity): String {
        var declaration = ""
        //val variables = entitiesVariables[entity.name]
        val variables = entitiesVariables[entity.name]?.entries?.reversed()?.associate { it.toPair() }
        var input = "{"
        variables?.forEach {it1, it2 ->
            if(input != "{")
                input += ", "

            input += "'$it1': $it2"
        }
        input += "}"

        println(input)

        var varEntity = varDictionaryEntities["%"+entity.name]


        var varyInput = "{"

        var alwayValidinput = "{"
        variables?.forEach {it1, it2 ->
            if(alwayValidinput != "{")
                alwayValidinput += ", "
            if(varDictionaryEntities.contains(it2))
                alwayValidinput += "'$it1': $it2"
            else
                varyInput += "'$it1': $it2"

        }
        alwayValidinput += "}"
        varyInput += "}"

        varDictionaryEntities.forEach { it1, it2 ->
            input = input.replace(it1, it2)
            alwayValidinput = alwayValidinput.replace(it1, it2)
            varyInput = varyInput.replace(it1, it2)
        }


        //Identify as few classes as possible and generate a test case for each class
         val limitValueAnalysis = "\nBoundary Value Analysis is a software testing technique used to exercise the boundaries of the input domain.\n"

        //Identify as few classes as possible and generate a test case for each class. Create at least 1 data test with valid input. Create data test to validate all attributes.
        declaration += "The Equivalence Partition technique aims to generate a test case for each identified class, avoiding redundancy. $limitValueAnalysis" +
                "\n**To apply The Equivalence Partition and Boundary Value Analysis technique, consider only the following attributes: $varyInput **" +
                "\nUse the Equivalence Partition to identify and generate as many test cases as possible using Equivalence Partition for each attribute. " +
                "\nCreate as many as possible new test cases with limits identified using Boundary Value Analysis technique. For each Equivalence Partition class create at least 1 additional test case. " +
                "\n**Create additional test cases to validate each attribute in $alwayValidinput with both `null` and non-`null` values, but only if the attribute contains an explicit `not null` validation. When not null, the value must be valid and complete. When null, the test case must be marked as invalid with a detailed error message.**" +
            "\n\nWhen generating data tests:" +
            "\n- Only the main entity ($varEntity) may contain invalid or edge-case data." +
                "\n- Respect strictly and exactly the validations explicitly described for each attribute. Do not add, assume, infer, extend, or create any additional validations beyond what is explicitly provided." +
            "\n- **All referenced entities (e.g., objects inside attributes like lists or nested objects) MUST always contain only valid and consistent data.**" +
            "\n- **You must NOT include any invalid, empty, inconsistent, or incomplete attributes inside referenced entities.**" +
            "\n **The attributes $alwayValidinput, for each attribute must contain either a valid value or be set to null only if explicitly allowed by its validation rule (e.g., not null)**. Do not create any other validations." +
                "\n **- Do not use any programming language functions for values (e.g., .repeat(), repeat(255), .substring(), .toString()). All values must be fully expanded and explicitly written.**\n"+
                "\n\nCreate a json with data test only to $varEntity. **It must follow the format: \n\n{ 'dataTest': [ { 'className': String, 'isValidInput': Boolean, 'input': $input, 'errorMessage': String }]}**" +
                "\nthe input field in json must have be a map presenting key and value for all attributes."+
                "\nGenerate data for all attributes. Do not let any attribute empty. If necessary validation a data test can be 'null'." +
                "\n\nConsider only the validation mentioned in the attribute and  validations for all attributes.\n" +
                "\nAll dates have to present the calculation formula description instead of a date, " +
                "for example: current date + 2 months, current date + 2 years, current date + 10 days." +
                "\nIn case of inputs that lead to an error, fill the errorMessage with detailed expected error type and mark isValidInput as false. errorMessage must be detailed. The class name must be unique and descriptive." +
                "\n\nCreate only one JSON as output from this prompt no further details. Do not add any comments.".trimIndent()

        println(declaration)
        return declaration
    }

    private fun entitiesDeclaration(entities: List<Entity>): String {
        var declaration = ""
        entities.forEach { declaration += entityDeclaration(it) }
        return declaration
    }

    private fun createEntityPrompt(entity: Entity, entities: List<Entity>): ListDataTest {
        var prompt = entitiesDeclaration(entities)

        varDictionaryEntities.forEach { (it1, it2) ->
            prompt = prompt.replace(it1, it2)
        }

        varDictionaryAttributes.forEach { (it1, it2) ->
            println(it2 + ": " + it1)
            prompt = prompt.replace(it2, it1)
        }

        prompt = replaceValidationsVar(prompt)

        prompt += createReturnDataTest(entity)

        println("prompt: " + prompt)

        prompt = prompt.replace("\n", "\\n")

        var result = AIAdapter.getAnswerFromQuestion(prompt)

        result = varDictionaryAttributes.entries
            .sortedByDescending { it.key.length }
            .fold(result) { acc, (from, to) ->
                acc.replace(Regex("\\b$from\\b"), Matcher.quoteReplacement(to))
            }

        varDictionaryEntities.forEach { (it1, it2) ->
            val value = it1.replace("%", "")
            result = result.replace(it2, value)
        }

        result = varDictionaryAttributes.entries
            .sortedByDescending { it.key.length }
            .fold(result) { acc, (from, to) ->
                acc.replace(Regex(from), Matcher.quoteReplacement(to))
            }
        result = result.replace("$", "")
        result = extractJson(result)
        result = result.replace("'", "\"")

        val mapper = jacksonObjectMapper()

        val convertedResult = mapper.readValue<ListDataTest>(result)
        return convertedResult
    }


    fun extractJson(text: String): String {
        val regex = Regex("```json\\s*(.*?)```", RegexOption.DOT_MATCHES_ALL)
        val matches = regex.findAll(text).mapNotNull { it.groups[1]?.value?.trim() }.toList()

        for (match in matches) {
            if (isValidJson(match)) {
                return match
            }
        }

        return ""
    }

    fun isValidJson(json: String): Boolean {
        return try {
            JSONObject(json) // Tenta interpretar como JSON Objeto
            true
        } catch (e: Exception) {
            try {
                JSONArray(json) // Tenta interpretar como JSON Array
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    override fun compute(input: Any): Any? {
        val entity = input as Entity
        entities = data["ListEntities"] as MutableList<Entity>
        val entityDataTest =  createEntityPrompt(entity, entities)
        TestPlanGenerator.createFile(entityDataTest.convertToJson(), "outputArtifacts/", "${entity.name}DataTest.json")

        processor as ChatGPTAdapter
        //Recuperando o histórico do processamento dos dados da entidade
        entityDataTest.llmMessageHistory = processor.messageHistory
        entityDataTest.varDictionaryAttributes = varDictionaryAttributes
        entityDataTest.varDictionaryEntities = varDictionaryEntities
        entityDataTest.entitiesAllVariables = entitiesAllVariables
        //limpando o histórico do processor para evitar lixo para o próximo processamento
        processor.messageHistory = mutableListOf<ChatGPTMessage>()

        println(entityDataTest.llmMessageHistory)
        return entityDataTest
    }
}