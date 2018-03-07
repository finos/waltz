package com.khartec.waltz.common;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;

/**
 * Parses a string into the enum type and also takes account of friendly names and aliases of the enum values
 * @param <T>
 */
public class EnumParser<T extends Enum<T>> {

    private Aliases<T> aliases;
    private Class<T> enumClass;


    public EnumParser(Aliases<T> aliases, Class<T> enumClass) {
        checkNotNull(aliases, "aliases cannot be null");
        checkNotNull(enumClass, "enumClass cannot be null");

        this.aliases = aliases;
        this.enumClass = enumClass;
    }


    public T parse(String value, T defaultValue) {
        String valueNormalised = trimString(value)
                .toUpperCase()
                .replace(' ', '_')
                .replace('-', '_');


        // try to parse the simple way first
        EnumSet<T> set = EnumSet.allOf(enumClass);
        for (T t : set) {
            if (t.name().equals(valueNormalised)) {
                return t;
            }
        }

        // now doing it with fallback
        T fallbackEnum = fallbackParseEnum(value);
        return fallbackEnum != null ? fallbackEnum : defaultValue;
    }


    public T parse(String value) {
        T parsed = parse(value, null);
        if(parsed == null) {
            throw new IllegalArgumentException(String.format("Could not parse value: %s to enum: %s", value, enumClass.getName()));
        }
        return parsed;
    }


    private T fallbackParseEnum(String value) {
        checkNotNull(value, "value cannot be null");

        String normalisedValue = trimString(value).toUpperCase();
        Optional<T> lookup = aliases.lookup(normalisedValue);
        if (lookup.isPresent()) {
            return lookup.get();
        }

        List<String> tokens = tokeniseValue(normalisedValue);
        for (String token : tokens) {
            Optional<T> tokenLookup = aliases.lookup(token);
            if (tokenLookup.isPresent()) {
                return tokenLookup.get();
            }
        }
        return null;
    }


    private List<String> tokeniseValue(String value) {
        checkNotNull(value, "value cannot be null");

        String normalised = stripChars(value).toUpperCase();

        String[] split = normalised.split(" ");
        List<String> cleansed = ListUtilities.newArrayList(split)
                .stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        return cleansed;
    }


    private String trimString(String value) {
        return value
                .replace((char) 160, ' ') // 160 is a non breaking space
                .trim();
    }


    private String stripChars(String value) {
        checkNotNull(value, "value cannot be null");

        final char[] fillerChars = new char[]{'-', '_', '(', ')', '{', '}', '/', '\\', ',', '.'};

        String valueNormalised = trimString(value);

        for (char c : fillerChars) {
            valueNormalised = valueNormalised.replace(c, ' ');
        }

        return valueNormalised.trim();
    }

}