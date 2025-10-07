package req2test.tool.approach.processor

import req2test.tool.approach.dto.*

interface ValidateWellFormedUserStory {
    fun isUSWellFormed(rawUserStory: String): WellFormedDTO
    fun isUSCorrectTemplate(rawUserStory: String): CorrectTemplateDTO

}

interface ValidateAtomicMinimalFullSentenceUserStory {
    fun isUSAtomicAndMinimal(rawUserStory: String): AtomicAndMinimaDTO
}

interface ValidateSemanticUserStory {
    fun isUSSemantic(rawUserStory: String): SemanticDTO
}

interface ValidateConflictFreeUserStory {
    fun isUSConflictFree(rawUserStories: MutableList<String>): MutableList<ConflictFreeDTO>
}
interface UserStoryProcessor: ValidateWellFormedUserStory, ValidateAtomicMinimalFullSentenceUserStory,
    ValidateSemanticUserStory, ValidateConflictFreeUserStory
