package req2test.tool.approach.processor.adapter

import req2test.tool.approach.dto.OllamaMessageDTO
import req2test.tool.approach.entity.Entity
import com.github.kittinunf.fuel.Fuel
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class OllamaAdapter(val url: String): AIAdapter {

    private fun request(jsonBody: String): String {
        val (request, response, result) = Fuel.post("http://localhost:11434/api/chat")
            .timeout(900000)
            .timeoutRead(900000)
            .body(jsonBody)
            .responseString()

        val (bytes, error) = result

        if(response.statusCode == 200)
            return bytes.toString().replace("'", "\\\"")

        throw Exception("Requesting error: $error")

    }
    override fun getAnswerFromQuestion(question: String, context: String?): String {

        val body = """
        {
          "model": "llama3",
          "messages": [
            {
              "role": "user",
              "content": "$question"
            }
          ],
          "stream": false
        }
    """.trim()
        println(body)
        val result = request(body)
        println(result)
        val jsonDecode = Json { ignoreUnknownKeys = true }
        val dto = jsonDecode.decodeFromString(result) as OllamaMessageDTO
        return dto.message.content
    }
}

fun main(){
    val jsonDecode = Json { ignoreUnknownKeys = true }
    val content = """{"model":"mistral","created_at":"2024-05-05T04:51:48.459655408Z","message":{"role":"assistant","content":" {"conceptuallySound": true, "problemOriented": false, "unambiguous": true, "estimatable": true, "conceptuallySoundErrors": [], "problemOrientedErrors": [], "unambiguousErrors": [], "estimatableErrors": []}"},"done":true,"total_duration":81610623743,"load_duration":301226,"prompt_eval_count":327,"prompt_eval_duration":65667993000,"eval_count":64,"eval_duration":15939831000}
"""
    val dto = jsonDecode.decodeFromString(content) as OllamaMessageDTO
    println(dto.message.content)

    val prompt = """
        "Respond by presenting only the JSON. A user story is well-formed when it has exactly 1 actor, 1 action and 1 purpose. Given the user story 'AS a Customer and a Professor I want to reserve a vehicle and buy it so that I can rent it later'. Extract the actors, actions and purposes.  The output must be presented as JSON, in the following format: { 'actors':,  'actions': [], 'purposes': [],'totalActors': int, 'totalActions': int, 'totalPurposes': int,'wellFormed': boolean }"
    """.trimIndent()

}



@Serializable
data class OllamaRequest(
    val model: String,
    val messages: List<ChatGPTMessage>,
    val stream: Boolean = false  // <-- adiciona esse campo
)

@Serializable
data class OllamaResponse(
    val message: ChatGPTMessage
)



class OllamaAdapter2 : ChatGPTAdapter("") {

    companion object {
        val entityHistory = HashMap<Entity, ArrayList<ChatGPTMessage>>()
    }

    override fun request(jsonBody: String): String {
        val (request, response, result) = Fuel.post("http://localhost:11434/api/chat")
            .timeout(900000)
            .timeoutRead(900000)
            .body(jsonBody)
            .responseString()

        val (bytes, error) = result

        if (response.statusCode == 200)
            return bytes.toString()

        throw Exception("Requesting error: $error")
    }

    override fun getAnswerFromQuestion(question: String, context: String?): String {
        if (messageHistory.isEmpty()) {
            val dataContext = """
                You are a JSON dataset generator with a strict focus on accuracy, valid syntax, and data consistency.
                All output must be valid and strictly conform to JSON syntax:
                - Use true and false (lowercase) for boolean values.
                - Use null (lowercase) for missing or undefined values.
                - Do not use Python-style values like `True`, `False`, or `None`.

                Always respect dependencies between attributes. Perform all operations precisely (sum, multiply, divide, etc.).
                Never make estimations or placeholders like “sum of…”. Always calculate exact results based on actual data.
                
                **Format the JSON response within a Markdown code block, delimited by ```json and ```.**
                Generate the JSON **on a single line**, with **no** line breaks, indentation, or extra whitespace. The JSON output should be as dense as possible.

                Return example:
                ```json{"key1":"value1","key2":"value2"}```
                """.trimIndent()

            var llmContext = dataContext
            if (context != null) {
                llmContext = context
            }
            messageHistory.add(ChatGPTMessage(role = "system", content = llmContext))
        }

        messageHistory.add(ChatGPTMessage(role = "user", content = question))

        val messagesJson = messageHistory.joinToString(separator = ",", prefix = "[", postfix = "]") {
            """{"role": "${it.role}", "content": "${it.content.replace("\"", "\\\"").replace("\n", "\\\\n").replace(" ", " ")}" }"""
        }

        val jsonBody = """
{
  "model": "deepseek-coder-v2:latest",
  "messages": $messagesJson,
  "stream": false
}
""".trimIndent()
        println("Request JSON: $jsonBody")

        val result = request(jsonBody)
        println("Raw Response: $result")

        val jsonDecode = Json { ignoreUnknownKeys = true }
        val response = jsonDecode.decodeFromString<OllamaResponse>(result)

        val answer = response.message
        messageHistory.add(answer)

        return answer.content
    }
}
