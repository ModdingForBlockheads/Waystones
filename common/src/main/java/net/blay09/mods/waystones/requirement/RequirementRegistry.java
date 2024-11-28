package net.blay09.mods.waystones.requirement;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.TeleportFlags;
import net.blay09.mods.waystones.api.WaystoneTypes;
import net.blay09.mods.waystones.api.WaystoneVisibility;
import net.blay09.mods.waystones.api.requirement.*;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.core.WaystoneTeleportManager;
import net.blay09.mods.waystones.tag.ModItemTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequirementRegistry {

    private static final Map<ResourceLocation, RequirementType<?>> requirementTypes = new HashMap<>();
    private static final Map<ResourceLocation, RequirementFunction<?, ?>> requirementFunctions = new HashMap<>();
    private static final Map<Class<?>, ParameterSerializer<?>> parameterSerializers = new HashMap<>();
    private static final Map<ResourceLocation, VariableResolver> variableResolvers = new HashMap<>();
    private static final Map<ResourceLocation, ConditionResolver<?>> conditionResolvers = new HashMap<>();

    public record NoParameter() {
        public static final NoParameter INSTANCE = new NoParameter();
    }

    public record IntParameter(int value) {
    }

    public record FloatParameter(float value) {
    }

    public record IdParameter(ResourceLocation value) {
    }

    public record WaystonesIdParameter(ResourceLocation value) {
    }

    public record ComponentParameter(Component value) {
    }

    public record VariableScaledParameter(WaystonesIdParameter id, FloatParameter scale) {
    }

    public record CooldownParameter(WaystonesIdParameter id, FloatParameter seconds) {
    }

    public record VariableScaledCooldownParameter(WaystonesIdParameter variable, WaystonesIdParameter cooldown, FloatParameter seconds) {
    }

    public record ItemParameter(IdParameter item, FloatParameter count) {
    }

    public record VariableScaledItemParameter(WaystonesIdParameter variable, IdParameter item, FloatParameter count) {
    }

    public static void registerDefaults() {
        final var experiencePointRequirements = new ExperiencePointsRequirementType();
        final var experienceLevelRequirements = new ExperienceLevelRequirementType();
        final var cooldownRequirements = new CooldownRequirementType();
        final var itemRequirements = new ItemRequirementType();

        register(experiencePointRequirements);
        register(experienceLevelRequirements);
        register(cooldownRequirements);
        register(itemRequirements);

        registerModifier("add_level_cost", experienceLevelRequirements, FloatParameter.class, (cost, context, parameters) -> {
            cost.setLevels((int) (cost.getLevels() + parameters.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("multiply_level_cost", experienceLevelRequirements, FloatParameter.class, (cost, context, parameters) -> {
            cost.setLevels((int) (cost.getLevels() * parameters.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("scaled_add_level_cost", experienceLevelRequirements, VariableScaledParameter.class, (cost, context, parameters) -> {
            final var sourceValue = context.getContextValue(parameters.id.value);
            cost.setLevels((int) (cost.getLevels() + sourceValue * parameters.scale.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("scaled_multiply_level_cost", experienceLevelRequirements, VariableScaledParameter.class, (cost, context, parameters) -> {
            final var sourceValue = context.getContextValue(parameters.id.value);
            cost.setLevels((int) (cost.getLevels() * sourceValue * parameters.scale.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("min_level_cost", experienceLevelRequirements, IntParameter.class, (cost, context, parameters) -> {
            cost.setLevels(Math.max(cost.getLevels(), parameters.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("max_level_cost", experienceLevelRequirements, IntParameter.class, (cost, context, parameters) -> {
            cost.setLevels(Math.min(cost.getLevels(), parameters.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);

        registerModifier("add_xp_cost", experiencePointRequirements, IntParameter.class, (cost, context, parameters) -> {
            cost.setPoints(cost.getPoints() + parameters.value);
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("multiply_xp_cost", experiencePointRequirements, FloatParameter.class, (cost, context, parameters) -> {
            cost.setPoints((int) (cost.getPoints() * parameters.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("scaled_add_xp_cost", experiencePointRequirements, VariableScaledParameter.class, (cost, context, parameters) -> {
            final var sourceValue = context.getContextValue(parameters.id.value);
            cost.setPoints((int) (cost.getPoints() + sourceValue * parameters.scale.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("scaled_multiply_level_cost", experiencePointRequirements, VariableScaledParameter.class, (cost, context, parameters) -> {
            final var sourceValue = context.getContextValue(parameters.id.value);
            cost.setPoints((int) (cost.getPoints() * sourceValue * parameters.scale.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("min_xp_cost", experiencePointRequirements, IntParameter.class, (cost, context, parameters) -> {
            cost.setPoints(Math.max(cost.getPoints(), parameters.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("max_xp_cost", experiencePointRequirements, IntParameter.class, (cost, context, parameters) -> {
            cost.setPoints(Math.min(cost.getPoints(), parameters.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);

        registerModifier("add_cooldown", cooldownRequirements, CooldownParameter.class, (cost, context, parameters) -> {
            cost.setCooldown(parameters.id.value, (int) ((float) cost.getCooldownSeconds() + parameters.seconds.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCooldowns);
        registerModifier("multiply_cooldown", cooldownRequirements, CooldownParameter.class, (cost, context, parameters) -> {
            cost.setCooldown(parameters.id.value, (int) ((float) cost.getCooldownSeconds() * parameters.seconds.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCooldowns);
        registerModifier("scaled_add_cooldown", cooldownRequirements, VariableScaledCooldownParameter.class, (cost, context, parameters) -> {
            final var sourceValue = context.getContextValue(parameters.variable.value);
            cost.setCooldown(parameters.cooldown.value, (int) ((float) cost.getCooldownSeconds() + sourceValue * parameters.seconds.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCooldowns);
        registerModifier("scaled_multiply_cooldown", cooldownRequirements, VariableScaledCooldownParameter.class, (cost, context, parameters) -> {
            final var sourceValue = context.getContextValue(parameters.variable.value);
            cost.setCooldown(parameters.cooldown.value, (int) ((float) cost.getCooldownSeconds() * sourceValue * parameters.seconds.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCooldowns);
        registerModifier("min_cooldown", cooldownRequirements, CooldownParameter.class, (cost, context, parameters) -> {
            cost.setCooldown(parameters.id.value, (int) Math.max(cost.getCooldownSeconds(), parameters.seconds.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCooldowns);
        registerModifier("max_cooldown", cooldownRequirements, CooldownParameter.class, (cost, context, parameters) -> {
            cost.setCooldown(parameters.id.value, (int) Math.min(cost.getCooldownSeconds(), parameters.seconds.value));
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCooldowns);

        registerModifier("add_item_cost", itemRequirements, ItemParameter.class, (cost, context, parameters) -> {
            final var item = BuiltInRegistries.ITEM.getValue(parameters.item.value);
            if (cost.getItemStack().getItem() != item) {
                cost.setItemStack(new ItemStack(item));
                cost.setCount((int) parameters.count.value);
            } else {
                cost.setCount((int) (cost.getCount() + parameters.count.value));
            }
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("multiply_item_cost", itemRequirements, ItemParameter.class, (cost, context, parameters) -> {
            final var item = BuiltInRegistries.ITEM.getValue(parameters.item.value);
            if (cost.getItemStack().getItem() != item) {
                cost.setItemStack(new ItemStack(item));
                cost.setCount((int) parameters.count.value);
            } else {
                cost.setCount((int) (cost.getCount() * parameters.count.value));
            }
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("scaled_add_item_cost", itemRequirements, VariableScaledItemParameter.class, (cost, context, parameters) -> {
            final var item = BuiltInRegistries.ITEM.getValue(parameters.item.value);
            if (cost.getItemStack().getItem() != item) {
                cost.setItemStack(new ItemStack(item));
                cost.setCount((int) (context.getContextValue(parameters.variable.value) * parameters.count.value));
            } else {
                cost.setCount((int) (cost.getCount() + context.getContextValue(parameters.variable.value) * parameters.count.value));
            }
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("scaled_multiply_item_cost", itemRequirements, VariableScaledItemParameter.class, (cost, context, parameters) -> {
            final var item = BuiltInRegistries.ITEM.getValue(parameters.item.value);
            if (cost.getItemStack().getItem() != item) {
                cost.setItemStack(new ItemStack(item));
                cost.setCount((int) (context.getContextValue(parameters.variable.value) * parameters.count.value));
            } else {
                cost.setCount((int) (cost.getCount() * context.getContextValue(parameters.variable.value) * parameters.count.value));
            }
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("min_item_cost", itemRequirements, ItemParameter.class, (cost, context, parameters) -> {
            final var item = BuiltInRegistries.ITEM.getValue(parameters.item.value);
            if (cost.getItemStack().getItem() != item) {
                cost.setItemStack(new ItemStack(item));
                cost.setCount((int) parameters.count.value);
            } else {
                cost.setCount(Math.max(cost.getCount(), (int) parameters.count.value));
            }
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);
        registerModifier("max_item_cost", itemRequirements, ItemParameter.class, (cost, context, parameters) -> {
            final var item = BuiltInRegistries.ITEM.getValue(parameters.item.value);
            if (cost.getItemStack().getItem() != item) {
                cost.setItemStack(new ItemStack(item));
                cost.setCount((int) parameters.count.value);
            } else {
                cost.setCount(Math.min(cost.getCount(), (int) parameters.count.value));
            }
            return cost;
        }, () -> WaystonesConfig.getActive().teleports.enableCosts);

        registerModifier("refuse", createDefaultType("refuse", RefuseRequirement.class), ComponentParameter.class, (cost, context, parameters) -> {
            cost.setMessage(parameters.value);
            return cost;
        }, () -> true);

        registerSerializer(NoParameter.class, it -> NoParameter.INSTANCE);
        registerSerializer(IntParameter.class, it -> new IntParameter(Integer.parseInt(it)));
        registerSerializer(FloatParameter.class, it -> new FloatParameter(Float.parseFloat(it)));
        registerSerializer(IdParameter.class, it -> new IdParameter(ResourceLocation.parse(it)));
        registerSerializer(WaystonesIdParameter.class, it -> new WaystonesIdParameter(RequirementModifierParser.waystonesResourceLocation(it)));
        registerSerializer(ComponentParameter.class, it -> new ComponentParameter(it.startsWith("$") ? Component.translatable(it.substring(1)) : Component.literal(it)));
        registerDefaultSerializer(VariableScaledParameter.class);
        registerDefaultSerializer(CooldownParameter.class);
        registerDefaultSerializer(VariableScaledCooldownParameter.class);
        registerDefaultSerializer(ItemParameter.class);
        registerDefaultSerializer(VariableScaledItemParameter.class);

        registerConditionResolver("is_interdimensional", NoParameter.class, (context, parameters) -> context.isDimensionalTeleport());
        registerConditionResolver("source_is_warp_plate", NoParameter.class,
                (context, parameters) -> context.getFromWaystone().map(waystone -> waystone.getWaystoneType().equals(WaystoneTypes.WARP_PLATE)).orElse(false));
        registerConditionResolver("source_is_portstone", NoParameter.class,
                (context, parameters) -> context.getFromWaystone().map(waystone -> waystone.getWaystoneType().equals(WaystoneTypes.PORTSTONE)).orElse(false));
        registerConditionResolver("source_is_waystone", NoParameter.class,
                (context, parameters) -> context.getFromWaystone().map(waystone -> waystone.getWaystoneType().equals(WaystoneTypes.WAYSTONE)).orElse(false));
        registerConditionResolver("source_is_sharestone", NoParameter.class,
                (context, parameters) -> context.getFromWaystone().map(waystone -> WaystoneTypes.isSharestone(waystone.getWaystoneType())).orElse(false));
        registerConditionResolver("source_is_inventory_button",
                NoParameter.class,
                (context, parameters) -> context.getFlags().contains(TeleportFlags.INVENTORY_BUTTON));
        registerConditionResolver("source_is_scroll", NoParameter.class, (context, parameters) -> context.getWarpItem().is(ModItemTags.SCROLLS));
        registerConditionResolver("source_is_bound_scroll", NoParameter.class, (context, parameters) -> context.getWarpItem().is(ModItemTags.BOUND_SCROLLS));
        registerConditionResolver("source_is_return_scroll", NoParameter.class, (context, parameters) -> context.getWarpItem().is(ModItemTags.RETURN_SCROLLS));
        registerConditionResolver("source_is_warp_scroll", NoParameter.class, (context, parameters) -> context.getWarpItem().is(ModItemTags.WARP_SCROLLS));
        registerConditionResolver("source_is_warp_stone", NoParameter.class, (context, parameters) -> context.getWarpItem().is(ModItemTags.WARP_STONES));
        registerConditionResolver("target_is_warp_plate",
                NoParameter.class,
                (context, parameters) -> context.getTargetWaystone().getWaystoneType().equals(WaystoneTypes.WARP_PLATE));
        registerConditionResolver("target_is_global",
                NoParameter.class,
                (context, parameters) -> context.getTargetWaystone().getVisibility() == WaystoneVisibility.GLOBAL);
        registerConditionResolver("target_is_sharestone",
                NoParameter.class,
                (context, parameters) -> WaystoneTypes.isSharestone(context.getTargetWaystone().getWaystoneType()));
        registerConditionResolver("target_is_waystone",
                NoParameter.class,
                (context, parameters) -> context.getTargetWaystone().getWaystoneType().equals(WaystoneTypes.WAYSTONE));
        registerConditionResolver("is_with_pets", NoParameter.class, (context, parameters) -> !WaystoneTeleportManager.findPets(context.getEntity()).isEmpty());
        registerConditionResolver("is_with_leashed",
                NoParameter.class,
                (context, parameters) -> !WaystoneTeleportManager.findLeashedAnimals(context.getEntity()).isEmpty());
        registerConditionResolver("source_is_dimension",
                IdParameter.class,
                (context, parameters) -> context.getFromWaystone()
                        .map(waystone -> waystone.getDimension().location())
                        .orElseGet(() -> context.getEntity().level().dimension().location())
                        .equals(parameters.value));
        registerConditionResolver("target_is_dimension",
                IdParameter.class,
                (context, parameters) -> context.getTargetWaystone().getDimension().location().equals(parameters.value));
        registerConditionResolver("involves_dimension",
                IdParameter.class,
                (context, parameters) -> context.getTargetWaystone().getDimension().location().equals(parameters.value) || context.getFromWaystone()
                        .map(waystone -> waystone.getDimension().location())
                        .orElseGet(() -> context.getEntity().level().dimension().location())
                        .equals(parameters.value));
        registerConditionResolver("is_within_distance",
                FloatParameter.class,
                (context, parameters) -> (float) Math.sqrt(context.getEntity()
                        .distanceToSqr(context.getTargetWaystone().getPos().getCenter())) <= parameters.value);

        registerVariableResolver("distance", it -> (float) Math.sqrt(it.getEntity().distanceToSqr(it.getTargetWaystone().getPos().getCenter())));
        registerVariableResolver("leashed", it -> (float) WaystoneTeleportManager.findLeashedAnimals(it.getEntity()).size());
        registerVariableResolver("pets", it -> (float) WaystoneTeleportManager.findPets(it.getEntity()).size());
    }

    private static <T extends WarpRequirement> RequirementType<T> createDefaultType(String name, Class<T> requirementClass) {
        final var requirementType = new RequirementType<T>() {
            @Override
            public ResourceLocation getId() {
                return ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, name);
            }

            @Override
            public T createInstance() {
                try {
                    return requirementClass.getConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        register(requirementType);
        return requirementType;
    }

    public static void register(RequirementType<?> requirementType) {
        requirementTypes.put(requirementType.getId(), requirementType);
    }

    public static void register(RequirementFunction<?, ?> requirementFunction) {
        requirementFunctions.put(requirementFunction.getId(), requirementFunction);
    }

    public static void register(ParameterSerializer<?> parameterSerializer) {
        parameterSerializers.put(parameterSerializer.getType(), parameterSerializer);
    }

    public static void register(VariableResolver variableResolver) {
        variableResolvers.put(variableResolver.getId(), variableResolver);
    }

    public static void register(ConditionResolver<?> conditionResolver) {
        conditionResolvers.put(conditionResolver.getId(), conditionResolver);
    }

    public static void registerVariableResolver(String name, Function<WaystoneTeleportContext, Float> resolver) {
        register(new VariableResolver() {
            @Override
            public ResourceLocation getId() {
                return ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, name);
            }

            @Override
            public float resolve(WaystoneTeleportContext context) {
                return resolver.apply(context);
            }
        });
    }

    public static <P> void registerConditionResolver(String name, Class<P> parameterType, BiFunction<WaystoneTeleportContext, P, Boolean> resolver) {
        register(new ConditionResolver<P>() {
            @Override
            public ResourceLocation getId() {
                return ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, name);
            }

            @Override
            public Class<P> getParameterType() {
                return parameterType;
            }

            @Override
            public boolean matches(WaystoneTeleportContext context, P parameters) {
                return resolver.apply(context, parameters);
            }
        });

        final var index = name.indexOf("is_");
        final var notName = index != -1 ? name.substring(0, index + 3) + "not_" + name.substring(index + 3) : "not_" + name;
        register(new ConditionResolver<P>() {
            @Override
            public ResourceLocation getId() {
                return ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, notName);
            }

            @Override
            public Class<P> getParameterType() {
                return parameterType;
            }

            @Override
            public boolean matches(WaystoneTeleportContext context, P parameters) {
                return !resolver.apply(context, parameters);
            }
        });
    }

    public static <T> void registerDefaultSerializer(Class<T> type) {
        registerSerializer(type, it -> RequirementModifierParser.deserializeParameterList(type, it));
    }

    public static <T> void registerSerializer(Class<T> type, Function<String, T> deserializer) {
        register(new ParameterSerializer<T>() {
            @Override
            public Class<T> getType() {
                return type;
            }

            @Override
            public T deserialize(String value) {
                return deserializer.apply(value);
            }
        });
    }

    private static <T extends WarpRequirement, P> void registerModifier(String name, RequirementType<T> requirementType, Class<P> parameterType, WarpRequirementModifierFunction<T, P> function, Supplier<Boolean> predicate) {
        register(new RequirementFunction<T, P>() {
            @Override
            public ResourceLocation getId() {
                return ResourceLocation.fromNamespaceAndPath(Waystones.MOD_ID, name);
            }

            @Override
            public ResourceLocation getRequirementType() {
                return requirementType.getId();
            }

            @Override
            public Class<P> getParameterType() {
                return parameterType;
            }

            @Override
            public T apply(T requirement, WarpRequirementsContext context, P parameters) {
                return function.apply(requirement, context, parameters);
            }

            @Override
            public boolean isEnabled() {
                return predicate.get();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends WarpRequirement> RequirementType<T> getRequirementType(ResourceLocation id) {
        return (RequirementType<T>) requirementTypes.get(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends WarpRequirement, P> RequirementFunction<T, P> getRequirementFunction(ResourceLocation id) {
        return (RequirementFunction<T, P>) requirementFunctions.get(id);
    }

    public static VariableResolver getVariableResolver(ResourceLocation id) {
        return variableResolvers.get(id);
    }

    public static ConditionResolver<?> getConditionResolver(ResourceLocation id) {
        return conditionResolvers.get(id);
    }

    @SuppressWarnings("unchecked")
    public static <T> ParameterSerializer<T> getParameterSerializer(Class<T> type) {
        return (ParameterSerializer<T>) parameterSerializers.get(type);
    }
}
