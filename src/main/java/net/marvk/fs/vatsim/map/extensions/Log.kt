package net.marvk.fs.vatsim.map.extensions

import org.apache.logging.log4j.LogManager

inline fun <reified T> T.createLogger() = LogManager.getLogger(T::class.java)
