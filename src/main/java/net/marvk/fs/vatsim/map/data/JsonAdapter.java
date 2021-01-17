package net.marvk.fs.vatsim.map.data;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

interface JsonAdapter<T> extends JsonSerializer<T>, JsonDeserializer<T> {
}
