package req2test.tool.approach.processor.stub

import req2test.tool.approach.processor.UserStoryProcessor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import req2test.tool.approach.dto.*


class ValidateUSAIStub: UserStoryProcessor {

    override fun isUSWellFormed(rawUserStory: String): WellFormedDTO {
        var json = "{ \"actors\": [\"Customer\"]," +
                "\"actions\": [\"want to reserve a vehicle\"], " +
                "\"purposes\": [\"so that I can rent it later\"], " +
                "\"totalActors\": 1, " +
                "\"totalActions\": 1, " +
                "\"totalPurposes\": 1, " +
                "\"wellFormed\": true }"

        if(System.getenv("INPUT_WITH_ERRORS") == "TRUE")
            json = "{ \"actors\": [\"Customer\", \"Professor\"]," +
                "\"actions\": [\"want to reserve a vehicle\", \"buy\"], " +
                "\"purposes\": [\"so that I can rent it later\"], " +
                "\"totalActors\": 2, " +
                "\"totalActions\": 2, " +
                "\"totalPurposes\": 1, " +
                "\"wellFormed\": false }"
        return Json.decodeFromString(json) as WellFormedDTO
    }

    override fun isUSCorrectTemplate(rawUserStory: String): CorrectTemplateDTO {
        TODO("Not yet implemented")
    }

    override fun isUSAtomicAndMinimal(rawUserStory: String): AtomicAndMinimaDTO {
        /*
        A user story can be atomic, minimal and full sentence. Atomic: A user story expresses a requirement for exactly one feature, Minimal: A user story contains nothing more than role, means, and ends, Full sentence: A user story is a well-formed full sentence. Given the user story 'as a Customer I want to reserve a vehicle so that I can rent it later'. Is the user story atomic, minimal and full sentence? The output must be presented as JSON, in the following format: "{ "atomic": { "atomic_restrictions": [], "validated": true }, "minimal": { "minimal_restrictions": [], "validated": true }, "full_sentence": { "full_sentence_restrictions": [], "validated": true } }" Present only JSON in the output.
         */
        var json = "{\"atomic\": true, " +
                "\"minimal\": true, " +
                "\"atomicErrors\": [], " +
                "\"minimalErrors\": [] }"

        if(System.getenv("INPUT_WITH_ERRORS") == "TRUE")
            json = "{\"atomic\": false, " +
                "\"minimal\": false, " +
                "\"atomicErrors\": [\"Multiple features described in the user story: reserving a vehicle and buying it.\"], " +
                "\"minimalErrors\": [\"Extra detail provided: 'so that I can rent it later'\"] }"


        return Json.decodeFromString(json) as AtomicAndMinimaDTO
    }

    override fun isUSSemantic(rawUserStory: String): SemanticDTO {
        var json = "{ \"conceptuallySound\": true, " +
                "\"problemOriented\": true, " +
                "\"unambiguous\": true, " +
                "\"estimatable\": true, " +
                "\"conceptuallySoundErrors\": [], " +
                "\"problemOrientedErrors\": [], " +
                "\"unambiguousErrors\": [], " +
                "\"estimatableErrors\": [] }"

        if(System.getenv("INPUT_WITH_ERRORS") == "TRUE")
            json = "{ \"conceptuallySound\": false, " +
                "\"problemOriented\": false, " +
                "\"unambiguous\": false, " +
                "\"estimatable\": true, " +
                "\"conceptuallySoundErrors\": [\"The user story describes both reserving a vehicle and buying it, which may imply a solution instead of just stating the problem.a\"], " +
                "\"problemOrientedErrors\": [\"The user story includes the solution ('buying it') rather than focusing solely on the problem\"], " +
                "\"unambiguousErrors\": [\"The phrase 'I can rent it later' introduces ambiguity about the intended usage of the reserved/bought vehicle\"], " +
                "\"estimatableErrors\": [] }"


        return Json.decodeFromString(json) as SemanticDTO
    }

    override fun isUSConflictFree(rawUserStories: MutableList<String>): MutableList<ConflictFreeDTO> {
        val listConflict = ArrayList<ConflictFreeDTO>()
        var json = "{ \"conflictFree\": true, \"conflictUS\": []}"

        if(System.getenv("INPUT_WITH_ERRORS") == "TRUE")
            json = "{ \"conflictFree\": false, \"conflictUS\": [ \"US1\", \"US2\"]}"
        val dto = Json.decodeFromString(json) as ConflictFreeDTO
        if(!dto.conflictFree)
            listConflict.add(dto)
        return listConflict
    }
}
