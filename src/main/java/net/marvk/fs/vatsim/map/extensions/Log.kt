package net.marvk.fs.vatsim.map.extensions

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

inline fun <reified T> T.createLogger(): Logger = LogManager.getLogger(T::class.java)
