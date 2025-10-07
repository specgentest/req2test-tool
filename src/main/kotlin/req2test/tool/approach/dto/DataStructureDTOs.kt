package req2test.tool.approach.dto

import req2test.tool.approach.entity.DataStructure
import kotlinx.serialization.Serializable

@Serializable
data class DataStructuresDTO(val dataStructures: List<DataStructure>)
