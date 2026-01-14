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
    import Markdown from "../../../../common/svelte/Markdown.svelte";
    import ARCTable from "./ARCTable.svelte";

    export let question = undefined;
    export let instanceId = undefined;
    export let currentResponse = undefined;
    export let mode = "VIEW";
    export let linkedEntityKind = null;
    export let linkedEntityId = null;
</script>

<div class="col-md-12 help-block">
    <div>
        {question?.questionText}
        {#if question?.isMandatory}
            <span class="mandatory"
                  title="This question is mandatory">*</span>
        {/if}
    </div>
    {#if question?.externalId}
        <div class="text-muted small"
             style="word-break: break-all">
            ({question?.externalId})
        </div>
    {/if}
    {#if question?.helpText}
        <div class="text-muted small"
             style="padding-top: 1em">
            <Markdown text={question?.helpText}/>
        </div>
    {/if}
</div>
<div class="col-md-12">
    <ARCTable {question}
              {instanceId}
              {currentResponse}
              {mode}
              {linkedEntityKind}
              {linkedEntityId}/>
</div>