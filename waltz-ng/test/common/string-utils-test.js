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

import {assert} from "chai";
import * as su from "../../client/common/string-utils";

describe("StringUtils", () => {
    describe("toDomainAll", () => {
        it ("returns just the domain part of a url",
            () => assert.equal("www.reddit.com", su.toDomain("https://www.reddit.com/r/programming")));
        it ("happy with any protocol",
            () => assert.equal("www.reddit.com", su.toDomain("ftp://www.reddit.com/r/programming")));
        it ("happy without protocol",
            () => assert.equal("www.reddit.com", su.toDomain("www.reddit.com/r/programming")));
        it ("happy without additional path",
            () => {
                assert.equal("www.reddit.com", su.toDomain("www.reddit.com"))
                assert.equal("www.reddit.com", su.toDomain("www.reddit.com/"))
            });
    });

    describe("truncateMiddle", () => {
        it("chops the middle out of strings", () =>
            assert.equal("12 ... 12", su.truncateMiddle("123456789101112", 9)));
        it("returns the original string if it's below the max length", () =>
            assert.equal("hello", su.truncateMiddle("hello", 9)));
    });

    describe("numberFormatter", () => {
        const simplify = true;
        const doNotSimplify = false;

        it("simplifies numbers", () => {
            assert.equal("123.5k", su.numberFormatter(123456.78, 1, simplify));
            assert.equal("123k", su.numberFormatter(123456.78, 0, simplify));
            assert.equal("124k", su.numberFormatter(123999.78, 0, simplify));
        });

        it("reduces precision on numbers", () => {
            assert.equal(1.1, su.numberFormatter(1.123, 1, doNotSimplify));
            assert.equal(1.2, su.numberFormatter(1.17, 1, doNotSimplify));
            assert.equal(1.6, su.numberFormatter(1.567, 1, doNotSimplify));

        });
    });

    describe("escapeRegexCharacters", () => {
        it("escapes", () => {
            assert.equal("\\+\\.\\?\\\\", su.escapeRegexCharacters("+.?\\"));
            assert.equal("\\$\\^\\{\\}\\(\\)", su.escapeRegexCharacters("$^{}()"));
            assert.equal("\\[\\]", su.escapeRegexCharacters("[]"));
        })
    });
});



