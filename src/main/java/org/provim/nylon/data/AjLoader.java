/*
 * Nylon
 * Copyright (C) 2023, 2024 Provim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.provim.nylon.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.provim.nylon.data.deserialization.*;
import org.provim.nylon.data.model.animated_java.AjModel;
import org.provim.nylon.data.model.converter.AjModelConverter;
import org.provim.nylon.data.model.nylon.NylonModel;

import java.io.*;
import java.util.UUID;

public class AjLoader {
    private static final Gson GSON = new GsonBuilder()
            // Reference equality
            .registerTypeAdapter(UUID.class, new ReferenceUuidDeserializer())

            // Custom deserializers
            .registerTypeAdapter(Vector3f.class, new Vector3fDeserializer())
            .registerTypeAdapter(Quaternionf.class, new QuaternionfDeserializer())
            .registerTypeAdapter(Identifier.class, new CodecDeserializer<>(Identifier.CODEC))
            .registerTypeAdapter(Item.class, new RegistryDeserializer<>(BuiltInRegistries.ITEM))
            .registerTypeAdapter(SoundEvent.class, new RegistryDeserializer<>(BuiltInRegistries.SOUND_EVENT))
            .create();

    public static NylonModel require(Identifier id) throws IllegalArgumentException, JsonParseException {
        String path = String.format("/ajmodels/%s/%s.json", id.getNamespace(), id.getPath());
        InputStream input = AjLoader.class.getResourceAsStream(path);
        if (input == null) {
            throw new IllegalArgumentException("Model doesn't exist: " + path);
        }

        return AjLoader.load(path, input);
    }

    public static NylonModel require(String path) throws IllegalArgumentException, JsonParseException {
        try (InputStream input = new FileInputStream(path)) {
            return AjLoader.load(path, input);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Model doesn't exist: " + path);
        }
    }

    public static NylonModel load(String path, InputStream input) throws JsonParseException {
        try (Reader reader = new InputStreamReader(input)) {
            AjModel model = GSON.fromJson(reader, AjModel.class);
            return AjModelConverter.convert(model);
        } catch (Throwable throwable) {
            throw new JsonParseException("Failed to parse model: " + path, throwable);
        }
    }
}
