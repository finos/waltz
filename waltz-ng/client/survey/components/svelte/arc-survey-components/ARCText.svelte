<!--
  ~ Waltz - Enterprise Architecture
  ~ Copyright (C) 2016 - 2026 Waltz open source project
  ~ See README.md for more information
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific
  ~
  -->

<script>
    import {debounce} from "lodash"
    import SurveyResponseInput from "../../input/survey-response-input";

    export let selectResponse = (r) => console.log("Selected response: ", r);

    const debouncedSelectResponse = debounce(selectResponse, 600);

    let text = "";

    $: debouncedSelectResponse(text);
</script>

<div>
    <input type="text" bind:value={text} class="arc-text-input"/>
</div>

<style>
    input {
    /* takes width of parent */
        width: 100%;
        height: 4rem;
        padding: 0.5rem;
    }
</style>