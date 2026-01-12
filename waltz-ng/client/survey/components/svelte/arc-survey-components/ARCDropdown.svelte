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
    import {surveyQuestionStore} from "../../../../svelte-stores/survey-question-store";
    import DropdownPicker from "../../../../common/svelte/DropdownPicker.svelte";
    export let question;
    export let selectResponse = (r) => console.log("selecting response: ", r);
    export let response;

    const MODES = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    $: templateDropdownEntriesCall = surveyQuestionStore.findDropdownEntriesForTemplate(175);
    $: templateDropdownEntries = $templateDropdownEntriesCall?.data || [];

    $: optionList = templateDropdownEntries
        .filter(t => t.questionId === question.id)
        .map(t => ({name: t.value}));
</script>

<div>
    <DropdownPicker items={optionList}
                    onSelect={(r) => selectResponse(r)}
                    defaultMessage={response}/>
</div>