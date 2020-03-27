import kotlinx.serialization.json.*
import java.io.File
import kotlin.collections.*


data class OperatorsCheckResult(
    val hasForbiddenElements: Boolean,
    val forbiddenElements: Collection<String>?
)

private data class LanguageInfo(
    val name: String,
    val extension: String,
    val singleLineComment: Regex = Regex("""//.*${'$'}"""),
    val multiLineComment: Regex = Regex("""/\*(?:.|\n)*(?:\*/)?""")
)

enum class Language(val languageInfo: LanguageInfo) {
    C(LanguageInfo("C", ".c")),
    CPP(LanguageInfo("C++", ".cpp")),
    CSHARP(LanguageInfo("C#", ".cs"))
}

class OperatorsChecker(
    opersKwsFilePath: String
) {
    private val opersKwsObject: JsonObject

    init {
        val json = Json(JsonConfiguration.Stable)
        val opersKwsJsonString = File(opersKwsFilePath).readText()
        opersKwsObject = json.parseJson(opersKwsJsonString).jsonObject
    }

    fun check(
        sourceCode: String,
        lang: Language,
        forbiddenElements: Collection<String>
    ): OperatorsCheckResult {
        val opersKws = opersKwsObject[lang.name] as JsonObject
        val operators = opersKws["operators"] as JsonArray
        val keywords = opersKws["keywords"] as JsonArray

        val uncommentedCode = sourceCode.let {
            val tmp = lang.languageInfo.singleLineComment.replace(it, "")
            lang.languageInfo.multiLineComment.replace(tmp, "")
        }

        TODO()
    }

}

