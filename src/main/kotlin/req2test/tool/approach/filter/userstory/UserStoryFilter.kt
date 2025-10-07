package req2test.tool.approach.filter.userstory

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.dto.WellFormedDTO
import req2test.tool.approach.entity.UserStory
import req2test.tool.approach.processor.UserStoryProcessor
import req2test.tool.approach.processor.generator.file.TestPlanGenerator

class UserStoryFilter(data: MutableMap<String, Any?>, processor: Any): Filter(data, processor) {
    override fun filterKeyInput(): String { return "RawUserStory" }
    override fun filterName(): String {

        return "UserStoryFilter"
    }

    override fun filterKeyOutput(): String {
        return "UserStory"
    }
    private val userStoryProcessor = processor as UserStoryProcessor
    private var userStory: UserStory = UserStory("")

    private fun extractRole(wellFormedDTO: WellFormedDTO): String?{
        if(wellFormedDTO.totalActors == 1)
            return wellFormedDTO.actors[0]
        else if(wellFormedDTO.totalActors > 1)
            logs.add(Log.createError("Well-Formed", "There are more than 1 actor" + wellFormedDTO.actors.toString()))
        else
            logs.add(Log.createError("Well-Formed", "No actor found"))
        return null
    }

    private fun extractTask(wellFormedDTO: WellFormedDTO): String?{
        if(wellFormedDTO.totalActions == 1)
            return wellFormedDTO.actions[0]
        else if(wellFormedDTO.totalActions > 1)
            logs.add(Log.createError("Well-Formed", "There are more than 1 action" + wellFormedDTO.actions.toString()))
        else
            logs.add(Log.createError("Well-Formed", "No action found"))
        return null
    }

    private fun extractPurpose(wellFormedDTO: WellFormedDTO): String? {
        if(wellFormedDTO.totalPurposes == 1)
            return wellFormedDTO.purposes[0]
        else if(wellFormedDTO.totalPurposes > 1)
            logs.add(
                Log.createError(
                    "Well-Formed",
                    "There are more than 1 purpose" + wellFormedDTO.purposes.toString()
                )
            )
        else
        logs.add(Log.createError("Well-Formed", "No purpose found"))
        return null
    }

    private fun validateAtomicAndMinimal(rawUserStory: String): Boolean {
        //TODO: try catch nos parses
        val atomicAndMinimaDTO = userStoryProcessor.isUSAtomicAndMinimal(rawUserStory)
        userStory.isAtomic = atomicAndMinimaDTO.atomic
        userStory.isMinimal = atomicAndMinimaDTO.minimal
        if(!atomicAndMinimaDTO.atomic) {
            logs.addAll(Log.createError("Atomic", atomicAndMinimaDTO.atomicErrors))
            return false
        }
        if(!atomicAndMinimaDTO.minimal) {
            logs.addAll(Log.createError("Minimal", atomicAndMinimaDTO.minimalErrors))
            return false
        }
        return true
    }

    private fun validateSemantic(rawUserStory: String): Boolean {
        val semanticDTO = userStoryProcessor.isUSSemantic(rawUserStory)
        userStory.isConcepttuallySound = semanticDTO.conceptuallySound
        userStory.isProblemOriented = semanticDTO.problemOriented
        userStory.isUnambiguous = semanticDTO.unambiguous
        userStory.isEstimatable = semanticDTO.estimatable
        if(!semanticDTO.conceptuallySound) {
            logs.addAll(Log.createWarning("Conceptually Sound", semanticDTO.conceptuallySoundErrors))
        }
        if(!semanticDTO.problemOriented) {
            logs.addAll(Log.createWarning("Problem Oriented", semanticDTO.problemOrientedErrors))
        }
        if(!semanticDTO.unambiguous) {
            logs.addAll(Log.createWarning("Unambiguous", semanticDTO.unambiguousErrors))
        }
        if(!semanticDTO.estimatable) {
            logs.addAll(Log.createWarning("Estimatable", semanticDTO.estimatableErrors))
        }
        return true
    }

    private fun updateRoleTaskPurpose(wellFormedDTO: WellFormedDTO) {
        userStory.role = extractRole(wellFormedDTO)
        userStory.task = extractTask(wellFormedDTO)
        userStory.purpose = extractPurpose(wellFormedDTO)
    }

    private fun validateWellFormed(wellFormedDTO: WellFormedDTO): Boolean {
        if((wellFormedDTO.totalActors==1) and (wellFormedDTO.totalActions==1))
            return true
        else
            logs.add(Log.createError("Well-Formed", "The user story is not well-formed"))
        return false
    }

    //It needs to be well-formed, and it needs to have at least one purpose
    private fun validateFullSentence(wellFormedDTO: WellFormedDTO): Boolean {
        if((wellFormedDTO.totalActors==1) and (wellFormedDTO.totalActions==1) and (wellFormedDTO.totalPurposes>0))
            return true
        else
            logs.add(Log.createWarning("Full Sentence", "The user story is not full sentence"))
        return false
    }

    private fun isUSWellFormed(rawUserStory: String): WellFormedDTO {
        return userStoryProcessor.isUSWellFormed(rawUserStory)
    }

    private fun isUSCorrectTemplate(rawUserStory: String): Boolean {
        val us = rawUserStory.lowercase()
        val AS = "as"
        val I_WANT = "i want"
        val SO_THAT = "so that"
        if(us.contains(AS) and us.contains(I_WANT) and us.contains(SO_THAT)) {
            val indexAS = us.indexOf(AS)
            val indexI_WANT = us.indexOf(I_WANT)
            val indexSO_THAT = us.indexOf(SO_THAT)

            if((indexAS < indexI_WANT) and (indexI_WANT < indexSO_THAT)){
                println("Template Válido")
                return true
            }
            else {
                println("Template Inválido: Ordem incorreta")
                logs.add(Log.createError("Well-Formed", "Template incorreto"))
                return false
            }
        }
        else {
            logs.add(Log.createError("Well-Formed", "Template incorreto"))
            return false
        }
    }

    override fun compute(input: Any): Any {
        val rawUserStory = input as String
        userStory = UserStory(rawUserStory)
        isUSCorrectTemplate(rawUserStory)
        val wellFormedDTO = isUSWellFormed(rawUserStory)
        userStory.isWellFormed = validateWellFormed(wellFormedDTO)
        userStory.isFullSentence = validateFullSentence(wellFormedDTO)
        validateAtomicAndMinimal(rawUserStory)
        updateRoleTaskPurpose(wellFormedDTO)
        validateSemantic(rawUserStory)
        userStory.logs.addAll(logs)
        if(errors().isEmpty())
            userStory.validated = true

        TestPlanGenerator.createFile(userStory.toString(), "outputArtifacts/", "userStory.txt")
        return userStory
    }
}


