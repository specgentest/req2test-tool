package req2test.tool.approach.filter.datatest.genericrule

import req2test.tool.approach.core.Filter
import req2test.tool.approach.filter.ac.rule.type.RuleVariable
import req2test.tool.approach.processor.adapter.AIAdapter
import req2test.tool.approach.processor.adapter.ChatGPTAdapter
import req2test.tool.approach.processor.adapter.ChatGPTMessage
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.LocalDate
import org.json.JSONObject
import org.json.JSONArray
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.ListDataTest
import req2test.tool.approach.entity.Rule
import java.util.regex.Matcher

class GenericRuleDataTestFilter (data: MutableMap<String, Any?>, processor: Any) : Filter(data, processor) {
    private val AIAdapter = processor as AIAdapter
    private var entities: MutableList<Entity> = ArrayList()
    private var countEntities = 1
    private var countAttributes = 1
    private val entitiesVariables = LinkedHashMap<String, Map<String, String>>()
    private val entitiesAllVariables = HashMap<String, Map<String, String>>()
    private val varDictionaryAttributes = HashMap<String, String>()
    private val varDictionaryEntities = HashMap<String, String>()

    override fun filterKeyInput(): String {
        return "ACRule"
    }

    override fun filterName(): String {
        return "GenericRuleDataTestFilter"
    }

    override fun filterKeyOutput(): String {
        return "GenericRuleDataTest"
    }

    private fun attributesDeclaration(genericRule: Rule): String {
        var variables = HashMap<String, String>()
        var allVariables = HashMap<String, String>()
        val varRule = "Rule$countEntities"
        val ruleID: String = genericRule.attributes["id"] as String
        varDictionaryEntities["%$ruleID"] = varRule

        var varDictionaryAttributes = HashMap<String, String>()

        val inputRuleVariables: List<RuleVariable> = genericRule.attributes["input"] as List<RuleVariable>
        val outputRuleVariables: List<RuleVariable> = genericRule.attributes["output"] as List<RuleVariable>
        val variablesRuleVariables: List<RuleVariable> = genericRule.attributes["variables"] as List<RuleVariable>
        val validations: List<String> = genericRule.attributes["validations"] as List<String>

        var declaration = "Given an $varRule with the following attributes:\n"

        val ruleVariables = ArrayList<RuleVariable>()
        ruleVariables.addAll(variablesRuleVariables)

        ruleVariables.forEach { rVariable ->
            val varAttribute = "var$countAttributes"
            val varAttribute2 = "Var$countAttributes"
            val type = rVariable.type

            variables[varAttribute] = type as String

            allVariables[varAttribute] = type as String

            this.varDictionaryAttributes[varAttribute] = "${'$'}${rVariable.name}"
            varDictionaryAttributes[varAttribute] = "${'$'}${rVariable.name}"
            //TODO - Replace correctly
            this.varDictionaryAttributes[varAttribute2] = "${'$'}${rVariable.name}"
            varDictionaryAttributes[varAttribute2] = "${'$'}${rVariable.name}"

            declaration += "- $varAttribute as $type and with the following validation: ["
            rVariable.validations?.forEach { it2 -> declaration += it2 + "; " }
            if(rVariable.generated)
                declaration += "generated: ${rVariable.generatedFormula}  ; "
            declaration += "]\n"
            countAttributes++
        }

        entitiesVariables[ruleID] = variables
        entitiesAllVariables[ruleID] = allVariables
        //Adding Rule validations
        declaration += "\nConsider the following validations for all attributes: \n"


       validations.forEach { validation ->
            declaration += "$validation\n"
        }

        declaration = revertVariables(declaration, varDictionaryAttributes)

        declaration += "\n"
        return declaration
    }

    fun revertVariables(text: String, varDictionaryAttributes: Map<String, String>): String {
        var result = text

        // Cria um mapa invertido: de "$items" -> "$var13"
        val reversed = varDictionaryAttributes.entries
            .associate { entry -> entry.value to "\$${entry.key.lowercase()}" }

        // Ordena para evitar substituições parciais
        val sorted = reversed.entries.sortedByDescending { it.key.length }

        for ((original, replacement) in sorted) {
            val regex = Regex("""(?<!\w)${Regex.escape(original)}(?!\w)""")
            result = regex.replace(result, Matcher.quoteReplacement(replacement))
        }

        return result
    }

    fun replaceEntities(text: String, varDictionaryEntities: Map<String, String>): String {
        var result = text

        // Ordena do maior para o menor para evitar colisões como "Cart" em "%Cart"
        val sorted = varDictionaryEntities.entries.sortedByDescending { it.key.length }

        for ((original, replacement) in sorted) {
            val pattern = Regex("""(?<!\w)${Regex.escape(original)}(?=\b|[.\[])""")
            result = pattern.replace(result, Matcher.quoteReplacement(replacement))
        }

        return result
    }

    fun substituteVariables(text: String, replacements: Map<String, String>): String {
        var result = text

        // Ordena por tamanho da chave para evitar substituições parciais
        val sorted = replacements.entries.sortedByDescending { it.key.length }

        for ((key, value) in sorted) {
            // Cria um padrão que busca a variável isolada ou em notações como Entity3.Var9
            val pattern = Regex("""(?<!\w)\Q$key\E(?!\w)""")
            result = pattern.replace(result, Matcher.quoteReplacement(value))
        }

        return result
    }

    private fun entityDeclaration(entity: Entity): String {
        val attributes = entity.attributes
        var variables = HashMap<String, String>()
        var allVariables = HashMap<String, String>()
        val varEntity = "Entity$countEntities"
        varDictionaryEntities["%"+entity.name] = varEntity
        varDictionaryEntities[entity.name] = varEntity

        var declaration = "Given an $varEntity with the following attributes:\n"
        //Insert Entity attributes that are not generated
        attributes.forEach {
            //if(!it.generatedDB and !it.generatedConstructor) {
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
    private fun createReturnDataTest(rule: Rule): String {
        var declaration = ""
        val ruleID: String = rule.attributes["id"] as String
        val variables = entitiesVariables[ruleID]?.entries?.reversed()?.associate { it.toPair() }
        //val reversedMap = originalMap.entries.reversed().associate { it.toPair() }

        var input = "{"
        variables?.forEach {it1, it2 ->
            if(input != "{")
                input += ", "

            input += "'$it1': $it2"
        }
        input += "}"

        println(input)

        var varEntity = varDictionaryEntities["%$ruleID"]

        varDictionaryEntities.forEach { it1, it2 ->
            input = input.replace(it1, it2)
        }

        //Identify as few classes as possible and generate a test case for each class
        val limitValueAnalysis = "\nBoundary Value Analysis is a software testing technique used to exercise the boundaries of the input domain.\n"

        val date = LocalDate.now()
        //Identify as few classes as possible and generate a test case for each class. Create at least 1 data test with valid input. Create data test to validate all attributes.
        declaration += "The Equivalence Partition technique aims to generate a test case for each identified class, avoiding redundancy. $limitValueAnalysis" +
                "\nUsing the Equivalence Partition, identify and generate as many test cases as possible using Equivalence Partition for each attribute. " +
                "\nFor each equivalence class, identify the limits. These limits include the minimum and maximum value within the class, as well as values just below and just above these limits." +
                "\nCreate as many as possible new test cases with limits identified using Boundary Value Analysis black box technique. **For each validation that contain strictly 'error message' create at least 1 additional test case**. Do not create invalid test case for the other validations." +
                "\n\nCreate a json with datatest only to $varEntity. It must follow the format: \n\n{ 'dataTest': [ { 'className': String, 'isValidInput': Boolean, 'input': $input, 'errorMessage': String }]}" +

                "\n When generating data test, only the main entity ($varEntity) should have invalid inputs for negative test cases. \n" +
                "All referenced entities (e.g., objects used inside attributes like lists or nested objects) **must always be valid and consistent**. \n" +
                "Do not introduce any invalid or inconsistent data inside referenced entities.\n" +

                "\nthe input field in json must have be a map presenting key and value for all attributes. Generate all auxiliary values to calculate the dataset and presents the calculated result. Validations marked as generated must have generate data as described."+
                "\nGenerate data for all attributes. Do not let any attribute empty. If necessary validation a data test can be 'null'." +
                "\n\nConsider only the validation mentioned in the attribute and  validations for all attributes.\n" +
                "\nAll dates have to present the calculation formula description instead of a date, " +
                "for example: current date + 2 months, current date + 2 years, current date + 10 days." +
                "\nIn case of inputs that lead to an error, fill the errorMessage with detailed expected error type and mark isValidInput as false. errorMessage must be detailed. The class name must be unique and descriptive." +
                "\n\nCreate only one JSON as output from this prompt no further details. Do not add any comments.".trimIndent()
        declaration = declaration.replace("generated:", "")
        println(declaration)
        return declaration
    }

    private fun entitiesDeclaration(entities: List<Entity>): String {
        var declaration = ""
        entities.forEach { declaration += entityDeclaration(it) }
        return declaration
    }

    private fun createEntityPrompt(rule: Rule, entities: List<Entity>): ListDataTest {
        var prompt = entitiesDeclaration(entities)
        prompt += attributesDeclaration(rule)

        prompt = replaceEntities(prompt, varDictionaryEntities)

        prompt = replaceValidationsVar(prompt)

        prompt += createReturnDataTest(rule)

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
        val rule = input as Rule
        val ruleID: String = rule.attributes["id"] as String
        entities = data["ListEntities"] as MutableList<Entity>
        val entityDataTest =  createEntityPrompt(rule, entities)
        TestPlanGenerator.createFile(entityDataTest.convertToJson(), "outputArtifacts/", "${ruleID}DataTest.json")

        processor as ChatGPTAdapter
        //Recuperando o histórico do processamento dos dados da entidade
        entityDataTest.llmMessageHistory = processor.messageHistory
        entityDataTest.varDictionaryAttributes = varDictionaryAttributes
        entityDataTest.entitiesAllVariables = entitiesAllVariables
        //limpando o histórico do processor para evitar lixo para o próximo processamento
        processor.messageHistory = mutableListOf<ChatGPTMessage>()

        println(entityDataTest.llmMessageHistory)
        return entityDataTest
    }
}