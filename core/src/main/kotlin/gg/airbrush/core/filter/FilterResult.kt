package gg.airbrush.core.filter

data class FilterResult(val action: FilterAction, val failedChecks: List<FilterRule>)