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
    import {arcSurveyState} from "./ARCSurveyState";
    import {settingsStore} from "../../../../svelte-stores/settings-store";
    import {ARC_DROPDOWN_DEFINITION, ARC_EXTERNAL_URL} from "../../../../common/constants";
    import {architectureRequiredChangeStore} from "../../../../svelte-stores/architecture-required-changes";
    import {mkRef} from "../../../../common/entity-utils";
    import ARCTableEdit from "./ARCTableEdit.svelte";
    import ARCTableView from "./ARCTableView.svelte";
    import {mkResponseObject, parseJSON} from "./arc-survey-utils";

    export let instanceId;
    export let question;
    export let currentResponse = "";
    export let mode;
    export let linkedEntityKind = null;
    export let linkedEntityId = null;

    const DEFAULT_DROPDOWN_DEFINITION = {
        label: "Do you want to select ARCs from the tree?",
        options: ["Y", "N"],
        inclusionOption: null
    };

    const MODES = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    $: settingsCall = settingsStore.loadAll();

    $: settings = $settingsCall?.data;

    $: dropdownSettingValue = settings?.find(t => t.name === ARC_DROPDOWN_DEFINITION)?.value;

    $: parsedDropdownSettingValue = dropdownSettingValue && parseJSON(dropdownSettingValue);

    $: dropdownDefinition = parsedDropdownSettingValue || DEFAULT_DROPDOWN_DEFINITION;

    $: dropdownItems = dropdownDefinition?.options.map(t => ({name: t}));

    $: urlSetting = settings?.find(t => t.name === ARC_EXTERNAL_URL)?.value;

    $: url = urlSetting ?? null;

    $: mode = (mode === MODES.VIEW || mode === MODES.EDIT) ? mode : MODES.VIEW;

    $: arcHierarchyCall = linkedEntityKind && linkedEntityId
        && architectureRequiredChangeStore
        .findForLinkedEntityHierarchy(mkRef(linkedEntityKind, linkedEntityId));

    // a parent becomes the main arc for each row
    $: arcs = $arcHierarchyCall?.data.filter(t => !t.externalParentId);

    // all children become a part of the selection tree
    $: arcHierarchy = $arcHierarchyCall?.data.filter(t => t.externalParentId);


    $: tableHeadings = [
        "ARC",
        "Milestones",
        dropdownDefinition?.label ?? DEFAULT_DROPDOWN_DEFINITION.label,
        question?.label ?? "Applicable ARCs",
    ];

    $: parsedCurrentResponse = (() => {
        try {
            if(!currentResponse) return null;
            const {responseType, ...rest} = JSON.parse(currentResponse);
            return rest.responses;
        } catch (e) {
            return null;
        }
    })();

    $: $arcSurveyState = parsedCurrentResponse ?? arcs
        .map(arc => mkResponseObject(arc));

</script>

<br/>
{#if mode === MODES.EDIT}
    <ARCTableEdit {arcs}
                  {arcHierarchy}
                  {tableHeadings}
                  {url}
                  {dropdownDefinition}
                  {dropdownItems}
                  currentResponse={parsedCurrentResponse ?? undefined}
                  {instanceId}
                  {question}/>
{:else}
    <ARCTableView arcs={parsedCurrentResponse?.map(t => t.entityRef)}
                  arcHierarchy={parsedCurrentResponse?.flatMap(t => t.response)}
                  {tableHeadings}
                  {url}
                  {dropdownDefinition}/>
{/if}

<br/>
