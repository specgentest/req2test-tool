package req2test.tool.approach.filter.userstory

import req2test.tool.approach.core.Filter
import req2test.tool.approach.core.Log
import req2test.tool.approach.entity.UserStory
import req2test.tool.approach.processor.UserStoryProcessor

class ValidateAndConvertListUSFilter(data: MutableMap<String, Any?>, processor: Any): Filter(data, processor) {
    override fun filterKeyInput(): String { return "ListRawUserStory" }
    override fun filterName(): String { return "ValidateAndConvertListUSFilter" }
    override fun filterKeyOutput(): String { return "ListUserStory" }
    private val userStoryProcessor = processor as UserStoryProcessor

    fun validateConflictFree(listRawUserStory: MutableList<String>): Boolean{
        val listConflictFreeDTO = userStoryProcessor.isUSConflictFree(listRawUserStory)
        if(listConflictFreeDTO.isNotEmpty()) {
            logs.add(Log.createError("Conflict-Free - Possibible logical conflict: ", listConflictFreeDTO.toString()))
            return false
        }
        return true
    }

    override fun compute(input: Any): Any {
        val listRawUserStory = input as MutableList<String>
        val listUS = ArrayList<UserStory>()
        for(rawUS in listRawUserStory){
            //Change the input rawUS to ValidateAndConvertUSFilter
            data["RawUserStory"] = rawUS
            val usFilter = UserStoryFilter(data, userStoryProcessor)
            if(usFilter.execute()){
                //All US are put in the list, even if validated is false
                //For other filters it is needed to filter validated US
                listUS.add(data["UserStory"] as UserStory)
            }
        }

        //Filtrar antes as US que foram validadas?
        validateConflictFree(listRawUserStory)

        return listUS
    }
}
