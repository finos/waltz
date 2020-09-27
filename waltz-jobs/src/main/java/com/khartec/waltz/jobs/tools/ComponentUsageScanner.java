package com.khartec.waltz.jobs.tools;

import org.jooq.lambda.Unchecked;
import org.jooq.lambda.function.Functions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class ComponentUsageScanner {

    private static final Predicate<Path> isHtml = f -> f.getFileName().toString().endsWith(".html");
    private static final Predicate<Path> isJs = f -> f.getFileName().toString().endsWith(".js");

    private Map<String, Path> componentToPath;

    public static void main(String[] args) throws IOException {
        ComponentUsageScanner cus = new ComponentUsageScanner();
        cus.go("c:/dev/repos/waltz-dev/waltz-ng/client");
    }

    private void go(String basePathStr) throws IOException {
        Path base = Paths.get(basePathStr);
        Map<String, Path> componentToPath = mkComponentToPathMap(base);
        Map<String, Set<Path>> componentPathToUsages = mkComponentToUsagesMap(base);

        componentToPath
                .entrySet()
                .stream()
                .filter(entry -> ! entry.getKey().endsWith("-section"))
                .filter(entry ->! componentPathToUsages.containsKey(entry.getKey()))
                .forEach(e -> System.out.println(e.getKey()));
    }

    private Map<String, Set<Path>> mkComponentToUsagesMap(Path base) throws IOException {
        Pattern pattern = Pattern.compile(".*<(waltz[-\\w]+).*");
        Map<String, Set<Path>> componentPathToUsages = Files
                .walk(base)
                .filter(Files::isRegularFile)
                .filter(Functions.or(isHtml, isJs))
                .filter(p -> ! p.getFileName().toString().contains("demo-"))
                .flatMap(Unchecked.function(p -> Files
                        .lines(p)
                        .map(pattern::matcher)
                        .filter(Matcher::matches)
                        .map(m -> m.group(1))
                        .distinct()
                        .map(componentName -> tuple(componentName, p))))
                .collect(groupingBy(t -> t.v1, mapping(t -> t.v2, toSet())));
        return componentPathToUsages;
    }


    private Map<String, Path> mkComponentToPathMap(Path base) throws IOException {
        Predicate<Path> dirMatches = f -> {
            String pathAsStr = f.toString();
            return pathAsStr.contains("components")
                    || pathAsStr.contains("directives")
                    || pathAsStr.contains("widgets");
        };
        return Files.walk(base)
                .filter(Files::isRegularFile)
                .filter(dirMatches)
                .filter(isHtml)
                .collect(Collectors.toMap(this::toComponentName, Function.identity()));
    }


    private String toComponentName(Path f) {
        String filename = f.getFileName().toString();
        return "waltz-" + filename.replace(".html", "");
    }
}
