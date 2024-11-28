package net.blay09.mods.waystones.requirement;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.WaystoneStyle;
import net.blay09.mods.waystones.api.requirement.ConditionResolver;
import net.blay09.mods.waystones.api.requirement.RequirementFunction;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class RequirementModifierParser {

    public static Optional<ConfiguredRequirementModifier<?, ?>> parse(String input) {
        try {
            final var conditionsStart = input.indexOf('[');
            final var conditionsEnd = input.indexOf(']');
            final var conditionsPart = conditionsStart != -1 && conditionsEnd != -1 ? input.substring(conditionsStart + 1, conditionsEnd) : "";
            final var functionPart = input.substring(conditionsEnd + 1).trim();
            final var conditions = parseConditions(conditionsPart);
            final var requirement = parseRequirement(functionPart);
            return Optional.of(new ConfiguredRequirementModifier<>(requirement, conditions));
        } catch (Exception e) {
            Waystones.logger.error("Could not parse warp requirement: {}", input, e);
            return Optional.empty();
        }
    }

    private static List<ConfiguredCondition<?>> parseConditions(String conditionsPart) {
        final var conditions = new ArrayList<ConfiguredCondition<?>>();
        final var conditionPattern = Pattern.compile("([\\w:]+)(?:\\((.*?)\\))?");
        final var conditionMatcher = conditionPattern.matcher(conditionsPart);

        while (conditionMatcher.find()) {
            final var conditionId = waystonesResourceLocation(conditionMatcher.group(1));
            final var args = conditionMatcher.group(2);
            final var conditionResolver = RequirementRegistry.getConditionResolver(conditionId);
            conditions.add(parseCondition(conditionResolver, args != null ? args : ""));
        }

        return conditions;
    }

    private static <P> ConfiguredCondition<P> parseCondition(ConditionResolver<P> conditionResolver, String args) {
        final var parameters = deserializeParameter(conditionResolver.getParameterType(), args);
        return new ConfiguredCondition<>(conditionResolver, parameters);
    }

    private static ConfiguredRequirement<?, ?> parseRequirement(String functionPart) {
        final var functionPattern = Pattern.compile("(\\w+)\\((.*?)\\)");
        final var functionMatcher = functionPattern.matcher(functionPart);

        if (functionMatcher.find()) {
            final var requirementId = waystonesResourceLocation(functionMatcher.group(1));
            final var args = functionMatcher.group(2);
            final var requirement = RequirementRegistry.getRequirementFunction(requirementId);
            return parseRequirement(requirement, args != null ? args : "");
        } else {
            throw new IllegalArgumentException("Invalid format for requirement modifier: '" + functionPart + "'");
        }
    }

    private static <T extends WarpRequirement, P> ConfiguredRequirement<T, P> parseRequirement(RequirementFunction<T, P> requirement, String args) {
        final var parameters = deserializeParameter(requirement.getParameterType(), args);
        return new ConfiguredRequirement<>(requirement, parameters);
    }

    public static ResourceLocation waystonesResourceLocation(String value) {
        final var colon = value.indexOf(':');
        final var namespace = colon != -1 ? value.substring(0, colon) : "waystones";
        final var path = colon != -1 ? value.substring(colon + 1) : value;
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static <T> T deserializeParameter(Class<T> type, String value) {
        final var serializer = RequirementRegistry.getParameterSerializer(type);
        if (serializer == null) {
            throw new IllegalArgumentException("No serializer registered for type " + type);
        }
        return serializer.deserialize(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializeParameterList(Class<T> type, String commaSeparatedParameters) {
        final var constructor = type.getConstructors()[0];
        final var parameterTypes = constructor.getParameterTypes();
        final var parameters = commaSeparatedParameters.split(",");
        if (parameters.length != parameterTypes.length) {
            throw new IllegalArgumentException("Parameter count mismatch for type " + type);
        }

        final var parameterValues = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterValues[i] = deserializeParameter(parameterTypes[i], parameters[i].trim());
        }

        try {
            return (T) constructor.newInstance(parameterValues);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
