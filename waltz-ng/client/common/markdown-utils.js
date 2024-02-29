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

import _ from "lodash";
import showdown from "showdown";

showdown.extension("bootstrap-tables", () => {
    return [{
        type: "output",
        regex: /<table>/g,
        replace: "<table class='table table-condensed table-striped small'>",
    }]
});

const converter = new showdown.Converter({extensions: ["bootstrap-tables"]});
converter.setFlavor("github");
converter.setOption("ghCodeBlocks", true);
converter.setOption("simplifiedAutoLink", true);
converter.setOption("simpleLineBreaks", true);
converter.setOption("strikethrough", true);
converter.setOption("tasklists", true);


export function markdownToHtml(markdownText) {
    return _.isEmpty(markdownText)
        ? null
        : converter.makeHtml(markdownText);
}
