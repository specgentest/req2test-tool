package req2test.tool.approach.processor.adapter

import req2test.tool.approach.core.OutputFolder
import req2test.tool.approach.entity.Entity
import req2test.tool.approach.processor.generator.file.TestPlanGenerator
import com.github.kittinunf.fuel.Fuel
import kotlinx.serialization.*
import kotlinx.serialization.json.*


@Serializable
data class ChatGPTMessage(val role: String, val content: String)

@Serializable
data class ChatGPTRequest(val model: String, val messages: List<ChatGPTMessage>)

@Serializable
data class ChatGPTResponse(val choices: List<Choice>) {
    @Serializable
    data class Choice(val message: ChatGPTMessage)
}

open class ChatGPTAdapter(val apiKey: String): AIAdapter {

    var messageHistory: MutableList<ChatGPTMessage> = mutableListOf<ChatGPTMessage>()

    companion object {
        var countPrompt = 1
        val entityHistory = HashMap<Entity, ArrayList<ChatGPTMessage>>()
    }

    open fun request(jsonBody: String): String {
        // Adiciona a propriedade "temperature" ao corpo JSON
        val adjustedJsonBody = jsonBody.replaceFirst("{", "{\"temperature\": 0.2,")

        val (request, response, result) = Fuel.post("https://api.openai.com/v1/chat/completions")
            .header("Authorization" to "Bearer $apiKey")
            .header("Content-Type" to "application/json")
            .timeout(300000)
            .timeoutRead(300000)
            .body(adjustedJsonBody)
            .responseString()

        val (bytes, error) = result

        if (response.statusCode == 200)
            return bytes.toString()

        throw Exception("Requesting error: $error")
    }


    override fun getAnswerFromQuestion(question: String, context: String?): String {
        // Adiciona a nova mensagem ao histórico
        var llmContext = ""
        if(messageHistory.isEmpty()) {

            var dataContext = """
                You are a JSON dataset generator with a strict focus on accuracy, valid syntax, and data consistency.
                **All output must be valid and strictly conform to JSON syntax**:
                - Use true and false (lowercase) for boolean values.
                - Use null (lowercase) for missing or undefined values.
                - Do not use Python-style values like `True`, `False`, or `None`.
                - **Never return values using functions, e.g repeat(255).**
                - Do not use any programming language functions for values (e.g., .repeat(), repeat(255), .substring(), .toString()). All values must be fully expanded and explicitly written.
                - Any use of programming expressions (like .repeat()) will cause the test case to be rejected. You must write literal values explicitly, even if it is long or repetitive.
                
                
                Always respect dependencies between attributes. When one field depends on others (e.g.,  ${'$'}sum depends on  ${'$'}values), compute the exact value based on the actual data present in the JSON.
                You must perform all mathematical operations precisely — no estimations, no assumptions. Do not use placeholders like “sum of…” or “calculated value”. Always return the correct computed result using the actual input data.
                If the prompt requests an operation such as summing, multiplying, dividing, or subtracting, always return the exact result, without rounding or approximation.
                For operations over lists (e.g., sum of all elements), iterate through the list and compute the correct result. Example: if  ${'$'}var = [10, 20, 30].
                Sum all the numeric values inside the array ${'$'}var, then the sum must be 60.
                Example2: if ${'$'}var2 = 2 e ${'$'}var3 = 10  
                ${'$'}var1 = ${'$'}var2 * ${'$'}var3
                then 
                ${'$'}var1 = 20, ${'$'}var2 = 2, ${'$'}var3 = 10.               
                Do not introduce validations, constraints, or business rules unless they are explicitly mentioned in the prompt.
                Do not infer behavior or constraints that were not requested. Always stay within the scope of the input prompt. 
                Never return inconsistent data.
                Always return any JSON output wrapped in a Markdown JSON code block, like this: ```json { "key": "value" } ```

            """.trimIndent()

            llmContext = dataContext

            if(context != null)
                llmContext = context

            //val context = """Você é um gerador de datasets JSON com responsabilidade total sobre a consistência e a exatidão dos dados. Deve calcular corretamente todos os campos derivados, especialmente somatórios. Para listas, você deve iterar por todos os elementos, realizando os cálculos de forma exata. O campo sum, por exemplo, deve refletir a soma de todos os elementos conforme instruções. Nunca invente valores derivados: sempre calcule-os corretamente com base nos dados anteriores do JSON."""
            messageHistory.add(ChatGPTMessage(role = "system", content = llmContext))


        }

        if(context != null)
            savePrompt(messageHistory, context, question)
        else
            savePrompt(messageHistory, llmContext, question)

        messageHistory.add(ChatGPTMessage(role = "user", content = question))

//        val body = ChatGPTRequest(model = "gpt-4o", messages = messageHistory)
        val body = ChatGPTRequest(model = "gpt-4o-2024-05-13", messages = messageHistory)
        val jsonBody = Json.encodeToString(body)
        println(jsonBody)

        val result = request(jsonBody)


        println(result)

        val jsonDecode = Json { ignoreUnknownKeys = true }
        val response = jsonDecode.decodeFromString<ChatGPTResponse>(result)

        // Adiciona a resposta do ChatGPT ao histórico
        val answer = response.choices.first().message
        messageHistory.add(answer)

        return answer.content
    }

    fun savePrompt(messageHistory: List<ChatGPTMessage>, context: String, prompt: String) {

        var finalContext = context.replace("\\n", "\n")
        var finalPrompt = prompt.replace("\\n", "\n")

        var finalHistory = ""
        messageHistory.forEach { message ->
            finalHistory += message.role.replace("\\n", "\n") + ":\n"
            finalHistory += message.content.replace("\\n", "\n") + "\n"
        }

        val textPrompt = """
Context:
        
$finalContext
        

Prompt:
        
$finalPrompt
        """.trimIndent()
        val folderPath = "prompts/"
        TestPlanGenerator.createFile(textPrompt, OutputFolder.basePath + folderPath, "prompt$countPrompt.txt")
        countPrompt++
    }
}