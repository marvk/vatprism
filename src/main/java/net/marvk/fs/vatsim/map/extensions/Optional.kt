package net.marvk.fs.vatsim.map.extensions

import java.util.Optional

fun <T> Optional<T>.getOrNull() = orElse(null)
