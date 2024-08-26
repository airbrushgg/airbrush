package gg.airbrush.core.lib

import java.text.DecimalFormat

fun Int.format(): String {
    return DecimalFormat("#,###,###").format(this)
}

fun Long.format(): String {
	return DecimalFormat("#,###,###").format(this)
}