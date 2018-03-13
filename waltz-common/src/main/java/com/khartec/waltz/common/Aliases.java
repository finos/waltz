package com.khartec.waltz.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;


public class Aliases<T> {

    private final HashMap<String, T> mappings = new HashMap<>();

    public Aliases register(T val, String... aliases) {
        mappings.put(sanitise(val.toString()), val);
        Arrays.stream(aliases)
                .map(this::sanitise)
                .forEach(s -> mappings.put(s, val));

        return this;
    }


    public Optional<T> lookup(String alias) {
        return Optional.ofNullable(mappings.get(sanitise(alias)));
    }


    private String sanitise(String s) {
        final char[] fillerChars = new char[]{'-', '_', '(', ')', '{', '}', '/', '\\', ',', '.'};

        String valueNormalised = s.trim();

        for (char c : fillerChars) {
            valueNormalised = valueNormalised.replace(c, ' ').replaceAll(" ", "");
        }

        return valueNormalised
                .toLowerCase()
                .trim();
    }

}
