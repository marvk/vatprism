package net.marvk.fs.vatsim.map.data;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

interface Adapter<T> extends JsonSerializer<T>, JsonDeserializer<T> {
}
