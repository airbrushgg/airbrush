package gg.airbrush.core.filter

data class FilterConfig(
    val root: Root,
    val rules: List<RuleDefinition>?
) {
    data class Root(val blockMessage: String)
    data class RuleDefinition(
        val blockMessage: String?,
        val pattern: String?,
        val file: String?,
        val action: FilterAction?)
}
