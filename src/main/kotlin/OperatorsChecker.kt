package checker.operators

import kotlinx.serialization.json.*
import java.io.File
import kotlin.collections.*


data class OperatorsCheckResult(
    val hasForbiddenElements: Boolean,
    val foundElements: Collection<String>?
)

private data class LanguageInfo(
    val name: String,
    val extension: String,
    val singleLineComment: Regex = Regex("""//.*?\n"""),
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
        val opersKwsEntry = opersKwsObject[lang.languageInfo.name] as JsonObject
        val operators = opersKwsEntry["operators"] as JsonArray
        val keywords = opersKwsEntry["keywords"] as JsonArray

        val uncommentedCode = uncommentCode(sourceCode, lang)

        val foundElements = mutableListOf<String>()

        forbiddenElements.forEach { element ->
            val elementRegex: Regex = getElementRegex(JsonLiteral(element), operators, keywords)
            if (elementRegex.containsMatchIn(uncommentedCode)) {
                foundElements.add(element)
            }
        }
        return if (foundElements.isNotEmpty())
            OperatorsCheckResult(true, foundElements)
        else
            OperatorsCheckResult(false, null)
    }

    private fun getElementRegex(
        element: JsonLiteral,
        operators: JsonArray,
        keywords: JsonArray
    ): Regex {
        if (element !in operators && element !in keywords)
            throw NoSuchElementException("Такого элемента нет в выбранном языке: $element")
        val elementRegex: Regex
        if (element in keywords)
            elementRegex = Regex("""\b${element.content}\b""")
        else {

            // если в операторе есть символы
            // "+", "*", "/", ".", "^", "?", "(", ")", "$" -- экранируем их

            val escapedElement = escapeOperator(element.content)
            elementRegex = Regex("""(?<=['\"\w\s])${escapedElement}(?=['\";\w\s\n])""")
        }
        return elementRegex
    }
}
    private fun uncommentCode(sourceCode: String, lang: Language): String =
        sourceCode.let {
            it
                .replace(lang.languageInfo.singleLineComment, "")
                .replace(lang.languageInfo.multiLineComment, "")
        }

    private fun escapeOperator(element: String): String {
        val escapeRegex = Regex("""[+*/.^?()${'$'}]""")
        val escapedElement = escapeRegex.replace(element) { matchResult ->
            """\${matchResult.value}"""
        }
        return escapedElement
    }

