package req2test.tool.approach.core

enum class LogType(val value: String) {
    WARNING("WARNING"),
    ERROR("ERROR"),
    CRITICAL_ERROR("CRITICAL ERROR")
}
class Log(private val tag: String, private val description: String, val logType: LogType){
    companion object {
        fun createWarning(tag: String, description: String): Log {
            return Log(tag, description, LogType.WARNING)
        }

        fun createWarning(tag: String, descriptions: List<String>): List<Log> {
            val logs = ArrayList<Log>()
            for(description in descriptions)
                logs.add(Log(tag, description, LogType.WARNING))
            return logs
        }

        fun createError(tag: String, description: String): Log {
            return Log(tag, description, LogType.ERROR)
        }

        fun createError(tag: String, descriptions: List<String>): List<Log> {
            val logs = ArrayList<Log>()
            for(description in descriptions)
                logs.add(Log(tag, description, LogType.ERROR))
            return logs
        }

        fun createCriticalError(tag: String, description: String): Log {
            return Log(tag, description, LogType.CRITICAL_ERROR)
        }

        fun createCriticalError(tag: String, descriptions: List<String>): List<Log> {
            val logs = ArrayList<Log>()
            for(description in descriptions)
                logs.add(Log(tag, description, LogType.CRITICAL_ERROR))
            return logs
        }
    }

    override fun toString(): String {
        return "$logType - $tag: $description"
    }
}
