package org.provim.nylon.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * The purpose of this deserializer is to reuse matching strings, so that we can use reference equality.
 */
public class ReferenceStringDeserializer implements JsonDeserializer<String> {
    @Override
    public String deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        return element.getAsString().intern();
    }
}