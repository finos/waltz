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
    import ARCDropdown from "./ARCDropdown.svelte";
    import ARCText from "./ARCText.svelte";
    import ARCTextArea from "./ARCTextArea.svelte";

    export let question;
    export let selectResponse = (r) => console.log("selecting response: ", r);
    export let response;
    export let mode;

    const MODES = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    const subQuestionFieldTypes = {
        DROPDOWN: "DROPDOWN",
        TEXT: "TEXT",
        TEXTAREA: "TEXTAREA",
        ARCTREE: "ARCTREE"
    }
</script>

<div>
    {#if question.fieldType === subQuestionFieldTypes.DROPDOWN}
        <ARCDropdown question={question}
                     selectResponse={(r) => selectResponse(r.name)}
                     mode={mode}
        response={response}/>
    {:else if question.fieldType === subQuestionFieldTypes.TEXT}
        <ARCText selectResponse={(r) => selectResponse(r)}/>
    {:else if question.fieldType === subQuestionFieldTypes.TEXTAREA}
        <ARCTextArea selectResponse={(r) => selectResponse(r)}/>
    {/if}
</div>