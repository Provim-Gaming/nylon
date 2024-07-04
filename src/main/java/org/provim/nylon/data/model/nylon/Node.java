package org.provim.nylon.data.model.nylon;

import org.apache.commons.lang3.Validate;

import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class Node {
    public final NodeType type;
    public final String name;
    public final UUID uuid;
    public final int customModelData;

    public Node(
            NodeType type,
            String name,
            UUID uuid,
            int customModelData
    ) {
        Validate.notNull(type, "Type cannot be null");
        Validate.notNull(name, "Name cannot be null");
        Validate.notNull(uuid, "UUID cannot be null");

        this.type = type;
        this.name = name;
        this.uuid = uuid;
        this.customModelData = customModelData;
    }

    public enum NodeType {
        BONE,
        LOCATOR
    }
}