package com.khartec.waltz.data;

import com.khartec.waltz.common.StringUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class SearchUtilities
{
    public static List<String> mkTerms(String query) {
        if(query.length() < 3) return new ArrayList<>();

        String safeQuery = query
                .replace("[", " ")
                .replace("]", " ")
                .replace("'", " ")
                .replace("\"", " ")
                .replace("|", " ")
                .replace("!", " ")
                .replace("%", " ")
                .replace("(", " ")
                .replace(")", " ")
                .replace(",", " ")
                .replace("~", " ");

        String[] terms = safeQuery.split(" ");

        // ensure the first term is at least 3 characters
        if(terms.length == 1 && terms[0].length() < 3) return new ArrayList<>();

        return Stream.of(terms)
                .filter(StringUtilities::notEmpty)
                .collect(toList());
    }
}
