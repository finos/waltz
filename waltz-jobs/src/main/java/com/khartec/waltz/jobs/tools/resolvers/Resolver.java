package com.khartec.waltz.jobs.tools.resolvers;

import java.util.Optional;

public interface Resolver<T> {

    Optional<T> resolve(String name);

    default String normalize(String name) {
        return name.trim().toLowerCase();
    }

}
