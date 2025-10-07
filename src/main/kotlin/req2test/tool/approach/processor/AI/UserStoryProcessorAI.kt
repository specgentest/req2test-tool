package req2test.tool.approach.processor.AI

import req2test.tool.approach.processor.UserStoryProcessor
import req2test.tool.approach.processor.adapter.AIAdapter
import req2test.tool.approach.processor.adapter.OllamaAdapter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import req2test.tool.approach.dto.*

class UserStoryProcessorAI(val AIAdapter: AIAdapter): UserStoryProcessor {

    fun extractJson(text: String): String {
        var regex = Regex("```json(.*?)```", RegexOption.DOT_MATCHES_ALL)
        var matchResult = regex.find(text)
        var res = matchResult?.groups?.get(1)?.value?.trim() ?: text.trim()

        return res
    }

    override fun isUSWellFormed(rawUserStory: String): WellFormedDTO {

        val question = "Respond by presenting only the JSON. A user story is wellFormed when totalActors is 1, totalActions is 1 action and totalPurposes is 1. A purpose is not considered an action. \\n\\nGiven the user story '$rawUserStory'. Extract the actors, actions, and purposes. \\n\\nEvaluate whether the user story is exactly following the template: 'AS actor I WANT action SO THAT purpose'. \\n\\nThe output must be presented as JSON in the exactly following format: { 'actors':,  'actions': [], 'purposes': [],'totalActors': int, 'totalActions': int, 'totalPurposes': int, 'wellFormed': boolean } \\n\\n \\n\\nIf there is no explicit actor mentioned, assume that the list of actors is empty."

        val result = extractJson(AIAdapter.getAnswerFromQuestion(question))
        println(result)
        return Json.decodeFromString(result) as WellFormedDTO
    }

    override fun isUSCorrectTemplate(rawUserStory: String): CorrectTemplateDTO {
        val question = "Response in the following JSON format: {'correctTemplate': <boolean>}. \\nPerform a morphological analysis in the user story. \\nVerify whether the user story is in the correct template: 'AS ... I WANT ... SO THAT ...'. Verify whether the user story has the word 'AS'. Verify whether the user story has the words 'I WANT'. The user story in correct template must have the words 'SO THAT'. \\n\\nEvaluate the user story exactly as it is written. \\nGiven the user story '$rawUserStory'. please return: \\n\\n {'correctTemplate': <boolean>} \\n\\n Please do not present explanation.} "
        val result = extractJson(AIAdapter.getAnswerFromQuestion(question))

        val jsonDecode = Json { ignoreUnknownKeys = true }
        return jsonDecode.decodeFromString(result) as CorrectTemplateDTO
    }

    override fun isUSAtomicAndMinimal(rawUserStory: String): AtomicAndMinimaDTO {
        val question = "Respond by presenting only the JSON. A user story is atomic when it expresses a requirement for exactly one feature. A user story is minimal when it contains nothing more than actor, action, and purpose (It does not allow anything like comments or extra details). It is allowed to have an action in purpose. \\nGiven the user story '$rawUserStory'. \\nIf the user story is not atomic present the errors in the json field atomicErrors. If the user story is not minimal present the errors in the json field minimalErrors. \\nPresents the JSON output exactly (the only output desired for the question) in the following format: \\n{ 'atomic': boolean, 'minimal': boolean, 'atomicErrors': [String], 'minimalErrors': [String]}"
        val result = extractJson(AIAdapter.getAnswerFromQuestion(question))
        return Json.decodeFromString(result) as AtomicAndMinimaDTO
    }

    override fun isUSSemantic(rawUserStory: String): SemanticDTO {
        val question = "Respond by presenting only the JSON.\\nA user story is conceptually sound when the means express a feature and the ends express a rationale Individual. A user story is problem-oriented: when it only specifies the problem, not the solution to it Individual. A user story is unambiguous when it avoids terms or abstractions that lead to multiple interpretations Individual. A user story is estimatable when it does not denote a coarse-grained requirement that is difficult to plan and prioritize. Given the user story '$rawUserStory'. \\nIf the user story is not conceptually sound present the errors in the json field conceptuallySoundErrors or an empty array. If the user story is not problem-oriented present the errors in the json field problemOrientedErrors or an empty array. If the user story is not unambiguous present the errors in the json field unambiguousErrors or an empty array. If the user story is not estimatable present the errors in the json field estimatableErrors or an empty array. \\nPresents the JSON output exactly (the only output desired for the question) in the following format:\\n{\'conceptuallySound\': boolean, \'problemOriented\': boolean, \'unambiguous\': boolean, \'estimatable\': boolean, \'conceptuallySoundErrors\': [String], \'problemOrientedErrors\': [String], \'unambiguousErrors\': [String], \'estimatableErrors\': [String]}"
        val result = extractJson(AIAdapter.getAnswerFromQuestion(question))
        return Json.decodeFromString(result) as SemanticDTO
    }

    override fun isUSConflictFree(rawUserStories: MutableList<String>): MutableList<ConflictFreeDTO> {
        val conflictList = ArrayList<ConflictFreeDTO>()
        for (i in 0 until rawUserStories.size) {
            for (j in i + 1 until rawUserStories.size) {
                val us1 = rawUserStories[i]
                val us2 = rawUserStories[j]
                val dto = isUSConflictFree(us1, us2)
                if(!dto.conflictFree)
                    conflictList.add(dto)
            }
        }
        return conflictList
    }

    private fun isUSConflictFree(rawUserStory1: String, rawUserStory2: String): ConflictFreeDTO {
        val question = "Create only the JSON and present no explanation.\\nPlease evaluate the following two user stories and determine whether they logical conflict, complement, or can coexist harmoniously within the project context: \\n US1: '$rawUserStory1'\\n US2: '$rawUserStory2'. \\nConsider the following points during your analysis: \\nConsider only the information in the user story. \\nIdentify possible conflicts between user stories. \\nEvaluate whether user stories can be complementary. Presents the JSON output ((the only output desired for the question)) exactly in the following format: \\n{ 'evaluation': String }. Evaluation can be: logical-conflict, complement, can-coexist-harmoniously."
        val result = extractJson(AIAdapter.getAnswerFromQuestion(question))

        val dto = Json.decodeFromString(result) as ConflictEvaluationDTO
        if(dto.evaluation == "logical-conflict"){

            val conflictUS =  ArrayList<String>()
            conflictUS.add(rawUserStory1)
            conflictUS.add(rawUserStory2)
            return ConflictFreeDTO(false, conflictUS)
        }
        else {
            return ConflictFreeDTO(true, ArrayList<String>())
        }
    }
}