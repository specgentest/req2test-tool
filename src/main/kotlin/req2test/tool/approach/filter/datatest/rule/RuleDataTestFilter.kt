package req2test.tool.approach.filter.datatest.rule

import req2test.tool.approach.core.Filter
import req2test.tool.approach.processor.adapter.AIAdapter
import req2test.tool.approach.processor.adapter.ChatGPTAdapter
import req2test.tool.approach.processor.adapter.ChatGPTMessage
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.entity.ListDataTest
import req2test.tool.approach.entity.Rule
import java.util.regex.Matcher

class RuleDataTestFilter (data: MutableMap<String, Any?>, processor: Any) : Filter(data, processor) {
    private val AIAdapter = processor as AIAdapter
    private var varDictionaryAttributes = HashMap<String, String>()
    private var varDictionaryEntities = HashMap<String, String>()

    override fun filterKeyInput(): String {
        return "EntityDataTest"
    }

    override fun filterName(): String {
        return "RuleDataTestFilter"
    }

    override fun filterKeyOutput(): String {
        return "RuleDataTest"
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


//    private fun createEntityPrompt(inputs: List<String>): ListDataTest {
    private fun createEntityPrompt(inputs: Map<String, String>?): ListDataTest {
        var idVar = ""
        varDictionaryAttributes.forEach { (it1, it2) ->
            if(it2.startsWith("\$id"))
                idVar = it1
        }

//        var prompt = "Generate three new valid data, and one valid data with only invalid type of $idVar with the error message 'invalid id'. Consider all attributes, \n\n{ 'dataTest': [ { 'className': String, 'isValidInput': Boolean, 'input': $inputs, 'errorMessage': String }]}. \n Do not ignore any attribute." +
//                "\nthe input field in json must have be a map presenting key and value for all attributes."

    var prompt = "Generate three new valid data, and one valid data with only invalid type of $idVar with the error message 'invalid id'. Consider all attributes. \n Do not ignore any attribute." +
            "\nthe input field in json must have be a map presenting key and value for all attributes."

        println(prompt)
        var result = AIAdapter.getAnswerFromQuestion(prompt)

        result = varDictionaryAttributes.entries
            .sortedByDescending { it.key.length }
            .fold(result) { acc, (from, to) ->
                acc.replace(Regex("\\b$from\\b"), Matcher.quoteReplacement(to))
            }

//Adicionado para substituir nome da Entidade "Entity3.price must be at least 10.0",
        //Pode dar problema só se tiver mais de 10 entidades. Nao vou tratar isso no momento
        varDictionaryEntities.forEach { (it1, it2) ->
            val value = it1.replace("%", "")
            result = result.replace(it2, value)
        }

        //Substituindo o restante de Var no meio de palavras
        result = varDictionaryAttributes.entries
            .sortedByDescending { it.key.length }
            .fold(result) { acc, (from, to) ->
                acc.replace(Regex(from), Matcher.quoteReplacement(to))
            }

        result = result.replace("$", "")
        result = extractJson(result)
        result = result.replace("'", "\"")

        println(result)

        val mapper = jacksonObjectMapper()

        val convertedResult = mapper.readValue<ListDataTest>(result)
        return convertedResult
    }

    fun extractJson(text: String): String {
        var regex = Regex("```json(.*?)```", RegexOption.DOT_MATCHES_ALL)
        var matchResult = regex.find(text)
        var res = matchResult?.groups?.get(1)?.value?.trim()

        if (res.isNullOrEmpty()) {
            regex = Regex("```(.*?)```", RegexOption.DOT_MATCHES_ALL)
            matchResult = regex.find(text)
            res = matchResult?.groups?.get(1)?.value?.trim()
        }

        // Retorne uma string vazia se nenhum JSON válido foi extraído
        return res ?: ""
    }



    override fun compute(input: Any): Any? {

        val listACRules = data["ListACRules"] as List<Rule>




        val entityDataTest = input as ListDataTest

        processor as ChatGPTAdapter
        processor.messageHistory =  entityDataTest.llmMessageHistory

        val entity = entityDataTest.reference as Entity
        val entityAllVariables = entityDataTest.entitiesAllVariables[entity.name] as Map<String, String>
//        val inputs = entityAllVariables.keys.filter { it.first().isLowerCase() }

        val inputs = entityAllVariables?.entries?.reversed()?.associate { it.toPair() }

        //val inputs = entityDataTest.varDictionaryAttributes.keys.filter { it.first().isLowerCase() }

        //Inicializando o dicionário para substituir as varíavéis com nomes genéricos
        varDictionaryAttributes = entityDataTest.varDictionaryAttributes
        varDictionaryEntities = entityDataTest.varDictionaryEntities
        val newEntityDataTest =  createEntityPrompt(inputs)

        TestPlanGenerator.createFile(newEntityDataTest.convertToJson(), "outputArtifacts/", "${entity.name}RuleDataTest.json")

        //Convertendo para ChatGPTAdapter para registrar o histórico
        //TODO generalizar para nao perder compatibilidade com outros LLMs. Problema de ter que implementar nova estratégia para outro LLM.

        //Recuperando o histórico do processamento dos dados da entidade
        entityDataTest.llmMessageHistory = processor.messageHistory
        //limpando o histórico do processor para evitar lixo para o próximo processamento
        processor.messageHistory = mutableListOf<ChatGPTMessage>()

        println(newEntityDataTest)

        return newEntityDataTest
    }
}