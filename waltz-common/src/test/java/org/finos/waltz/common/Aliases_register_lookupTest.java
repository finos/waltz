/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Aliases_register_lookupTest {

    @Test
    public void selfRegistersValues() {
        Aliases<String> aliases = new Aliases<>();
        aliases.register("BOB");
        assertEquals(aliases.lookup("bob"), Optional.of("BOB"));
    }


    @Test
    public void canProvideMultipleAliasesForOneValue() {
        Aliases<String> aliases = new Aliases<>();
        aliases.register("BOB", "robert", "bobby");
        assertEquals(aliases.lookup("bobby"), Optional.of("BOB"));
        assertEquals(aliases.lookup("robert"), Optional.of("BOB"));
        assertEquals(aliases.lookup("nope"), Optional.empty());
    }


    @Test
    public void canResolveAliasesCaseInsensitively() {
        Aliases<String> aliases = new Aliases<>();
        aliases.register("BOB", "robert", "bobby");
        assertEquals(aliases.lookup("Bobby"), Optional.of("BOB"));
        assertEquals(aliases.lookup("Robert"), Optional.of("BOB"));
    }


    @Test
    public void canResolveAliasesWithSpuriousWhitespace() {
        Aliases<String> aliases = new Aliases<>();
        aliases.register("BOB", "robert", "bobby");
        assertEquals(aliases.lookup(" Bobby "), Optional.of("BOB"));
        assertEquals(aliases.lookup("Robert"), Optional.of("BOB"));
    }


    @Test
    public void ignoresUnderscores() {
        Aliases<String> aliases = new Aliases<>();
        aliases.register("SUPERMAN", "clark KENT");
        assertEquals(aliases.lookup("Clark_Kent"), Optional.of("SUPERMAN"));
        assertEquals(aliases.lookup("Clark-Kent"), Optional.of("SUPERMAN"));
    }

}
