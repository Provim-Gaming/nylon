package org.provim.nylon.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjEntityHolder;
import org.provim.nylon.api.VariantController;
import org.provim.nylon.data.AjLoader;
import org.provim.nylon.extra.ModelEntity;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjVariant;
import org.provim.nylon.util.Utils;

import java.util.Collection;
import java.util.function.Consumer;

public class ModelCommand {
    private static final String TARGETS = "targets";
    private static final String ANIMATION = "animation";

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        var builder = Commands.literal("model").requires(source -> source.hasPermission(2));

        // Create model commands
        builder.then(modelCreator());

        // Manipulate model commands
        builder.then(Commands.argument(TARGETS, EntityArgument.entities())
                .then(scaleManipulator())
                .then(variantManipulator())
                .then(animationManipulator())
        );

        return builder;
    }

    private static int manipulateModels(CommandSourceStack source, Collection<? extends Entity> targets, Consumer<AjEntityHolder> consumer) {
        int count = 0;
        for (Entity target : targets) {
            AjEntityHolder holder = AjEntity.getHolder(target);
            if (holder != null) {
                consumer.accept(holder);
                count++;
            }
        }

        if (count <= 0) {
            source.sendFailure(Component.literal("No models found!"));
        }

        return count;
    }

    private static int spawnModel(CommandSourceStack source, ResourceLocation id) throws CommandSyntaxException {
        try {
            AjModel model = AjLoader.require(id);
            return spawnModel(source, model);
        } catch (Throwable throwable) {
            throw Utils.buildCommandException("Failed to load model!\n" + throwable.getMessage());
        }
    }

    private static int spawnModel(CommandSourceStack source, String path) throws CommandSyntaxException {
        try {
            AjModel model = AjLoader.require(path);
            return spawnModel(source, model);
        } catch (Throwable throwable) {
            throw Utils.buildCommandException("Failed to load model!\n" + throwable.getMessage());
        }
    }

    private static int spawnModel(CommandSourceStack source, AjModel model) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        Vec2 rot = source.getRotation();

        ModelEntity entity = new ModelEntity(level, model);
        entity.moveTo(pos.x, pos.y, pos.z, rot.y, 0F);

        level.addFreshEntity(entity);

        source.sendSuccess(() -> Component.literal("Successfully spawned model!"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> modelCreator() {
        var builder = Commands.literal("create");

        // Create model commands
        builder.then(Commands.literal("id")
                .then(Commands.argument("model", ResourceLocationArgument.id())
                        .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                        .executes(context -> spawnModel(
                                context.getSource(),
                                ResourceLocationArgument.getId(context, "model")
                        ))
                )
        );

        builder.then(Commands.literal("filepath")
                .then(Commands.argument("model", StringArgumentType.greedyString())
                        .executes(context -> spawnModel(
                                context.getSource(),
                                StringArgumentType.getString(context, "model")
                        ))
                )
        );

        return builder;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> scaleManipulator() {
        var builder = Commands.literal("scale");

        builder.then(Commands.argument("scale", FloatArgumentType.floatArg(0.01f))
                .executes(context -> {
                    float scale = FloatArgumentType.getFloat(context, "scale");
                    return manipulateModels(
                            context.getSource(),
                            EntityArgument.getEntities(context, TARGETS),
                            holder -> holder.setScale(scale)
                    );
                })
        );

        return builder;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> variantManipulator() {
        var builder = Commands.literal("variant");

        builder.then(Commands.argument("variant", StringArgumentType.word())
                .suggests(availableVariants())
                .executes(context -> {
                    String variant = StringArgumentType.getString(context, "variant");
                    return manipulateModels(
                            context.getSource(),
                            EntityArgument.getEntities(context, TARGETS),
                            holder -> {
                                VariantController controller = holder.getVariantController();
                                if (variant.equals("default")) {
                                    controller.setDefaultVariant();
                                } else {
                                    controller.setVariant(variant);
                                }
                            }
                    );
                })
        );

        return builder;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> animationManipulator() {
        var builder = Commands.literal("animation");

        builder.then(Commands.argument(ANIMATION, StringArgumentType.word())
                .suggests(availableAnimations())
                .executes(context -> {
                    String animation = StringArgumentType.getString(context, ANIMATION);
                    return manipulateModels(
                            context.getSource(),
                            EntityArgument.getEntities(context, TARGETS),
                            (holder) -> holder.getAnimator().playAnimation(animation)
                    );
                })
                .then(Commands.literal("play")
                        .executes(context -> {
                            String animation = StringArgumentType.getString(context, ANIMATION);
                            return manipulateModels(
                                    context.getSource(),
                                    EntityArgument.getEntities(context, TARGETS),
                                    (holder) -> holder.getAnimator().playAnimation(animation)
                            );
                        })
                        .then(Commands.argument("priority", IntegerArgumentType.integer())
                                .executes(context -> {
                                    String animation = StringArgumentType.getString(context, ANIMATION);
                                    int priority = IntegerArgumentType.getInteger(context, "priority");
                                    return manipulateModels(
                                            context.getSource(),
                                            EntityArgument.getEntities(context, TARGETS),
                                            (holder) -> holder.getAnimator().playAnimation(animation, priority)
                                    );
                                })
                        )
                )
                .then(Commands.literal("pause")
                        .executes(context -> {
                            String animation = StringArgumentType.getString(context, ANIMATION);
                            return manipulateModels(
                                    context.getSource(),
                                    EntityArgument.getEntities(context, TARGETS),
                                    holder -> holder.getAnimator().pauseAnimation(animation)
                            );
                        })
                )
                .then(Commands.literal("stop")
                        .executes(context -> {
                            String animation = StringArgumentType.getString(context, ANIMATION);
                            return manipulateModels(
                                    context.getSource(),
                                    EntityArgument.getEntities(context, TARGETS),
                                    holder -> holder.getAnimator().stopAnimation(animation)
                            );
                        })
                )
        );

        return builder;
    }

    private static SuggestionProvider<CommandSourceStack> availableAnimations() {
        return (ctx, builder) -> {
            forEachModel(ctx, model -> {
                for (String animation : model.animations().keySet()) {
                    builder.suggest(animation);
                }
            });
            return builder.buildFuture();
        };
    }

    private static SuggestionProvider<CommandSourceStack> availableVariants() {
        return (ctx, builder) -> {
            builder.suggest("default");
            forEachModel(ctx, model -> {
                for (AjVariant variant : model.variants().values()) {
                    builder.suggest(variant.name());
                }
            });
            return builder.buildFuture();
        };
    }

    private static void forEachModel(CommandContext<CommandSourceStack> ctx, Consumer<AjModel> consumer) throws CommandSyntaxException {
        // Make sure to only call this when we have the target context already.
        for (Entity entity : EntityArgument.getEntities(ctx, TARGETS)) {
            AjEntityHolder holder = AjEntity.getHolder(entity);
            if (holder != null) {
                consumer.accept(holder.getModel());
            }
        }
    }
}
