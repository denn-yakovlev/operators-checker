import checker.operators.*
fun main(args: Array<String>) {

    // usage example

    val checker = OperatorsChecker("src/main/resources/OpersKwsList.json")
    val code = """
        //some comment
        for (int i = 0; i < 42; i++) {
            x += 1    
        }
    """.trimIndent()
    val forbidden = listOf("+=")
    val result = checker.check(code, Language.C, forbidden)
    assert(result.hasForbiddenElements == true)
    assert(result.foundElements?.size == 1 ?: false)
    assert(result.foundElements?.contains("+=") ?: false)
}