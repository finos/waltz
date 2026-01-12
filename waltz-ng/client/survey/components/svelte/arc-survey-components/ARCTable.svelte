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
    import Tooltip from "../../../../common/svelte/Tooltip.svelte";
    import ArcTooltip from "./ARCTooltip.svelte";
    import ARCSubQuestions from "./ARCSubQuestions.svelte";
    import {arcSurveyState, invalidRows} from "./ARCSurveyState";
    import {surveyInstanceStore} from "../../../../svelte-stores/survey-instance-store";
    import ARCTree from "./ARCTree.svelte";
    import DropdownPicker from "../../../../common/svelte/DropdownPicker.svelte";
    import {displayError} from "../../../../common/error-utils";
    import {settingsStore} from "../../../../svelte-stores/settings-store";
    import {onDestroy} from "svelte";
    import toastStore from "../../../../svelte-stores/toast-store";
    import {ARC_DROPDOWN_LABEL, ARC_EXTERNAL_URL} from "../../../../common/constants";
    export let instanceId;
    export let question;
    export let currentResponse = "";
    export let mode;

    let savingResponse = false;

    const DEFAULT_DROPDOWN_DEFINITION = {
        label: "Do you want to select ARCs from the tree?",
        options: ["Y", "N"],
        inclusionOption: null
    };

    const MODES = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    const requiredQuestionFields = (question) => (
        {
            id: question.id,
            externalId: question.externalId,
            fieldType: question.fieldType
        }
    );

    const getArcRow = (arcId) => {
        return $arcSurveyState.find(t => t.entityRef.id === arcId);
    }

    const getSelectedItems = (arcId) => {
        return getArcRow(arcId)?.response ?? [];
    }

    const parseJSON = (string) => {
        try {
            return JSON.parse(string);
        } catch (e) {
            return undefined;
        }
    }

    $: settingsCall = settingsStore.loadAll();

    $: settings = $settingsCall?.data;

    $: dropdownSettingValue = settings?.find(t => t.name === ARC_DROPDOWN_LABEL)?.value;

    $: parsedDropdownSettingValue = dropdownSettingValue && parseJSON(dropdownSettingValue);

    $: dropdownDefinition = parsedDropdownSettingValue || DEFAULT_DROPDOWN_DEFINITION;

    $: dropdownItems = dropdownDefinition?.options.map(t => ({name: t}));

    $: urlSetting = settings?.find(t => t.name === ARC_EXTERNAL_URL)?.value;

    $: url = urlSetting ?? null;

    $: mode = (mode === MODES.VIEW || mode === MODES.EDIT) ? mode : MODES.VIEW;

    const arcExamples = [
        {
            id: 1,
            externalId: "ARC-4913",
            title: "ARC-4913",
            description: "This is an architecturally required change.",
            status: "Green",
            milestoneRag: "A",
            milestoneForecastDate: "11/02/2026 14:22:34",
            parentExternalId: "ARC-4407",
            linkedEntityId: 12,
            linkedEntityKind: "CHANGE_INITIATIVE"
        },
        {
            id: 2,
            externalId: "ARC-4407",
            title: "ARC-4407",
            description: "This is an architecturally required change.",
            status: "Green",
            milestoneRag: "A",
            milestoneForecastDate: "11/02/2026 14:22:34",
            parentExternalId: null,
            linkedEntityId: 12,
            linkedEntityKind: "CHANGE_INITIATIVE"
        },
        {
            id: 3,
            externalId: "ARC-4408",
            title: "ARC-4408",
            description: "This is an architecturally required change.",
            status: "Green",
            milestoneRag: "A",
            milestoneForecastDate: "11/02/2026 14:22:34",
            parentExternalId: "ARC-4407",
            linkedEntityId: 12,
            linkedEntityKind: "CHANGE_INITIATIVE"
        },
        {
            id: 4,
            externalId: "ARC-4402",
            title: "ARC-4402",
            description: "This is an architecturally required change.",
            status: "Green",
            milestoneRag: "A",
            milestoneForecastDate: "11/02/2026 14:22:34",
            parentExternalId: "ARC-4408",
            linkedEntityId: 12,
            linkedEntityKind: "CHANGE_INITIATIVE"
        }
    ]

    const arcs = [...arcExamples];


    $: tableHeadings = [
        "ARC",
        "Milestones",
        dropdownDefinition?.label ?? DEFAULT_DROPDOWN_DEFINITION.label,
        "Applicable ARCs",
        ...(question?.subQuestions || []).map(q => q?.label),
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
        .map(arc => (
            {
                entityRef: arc,

                questions: question?.subQuestions && [...question?.subQuestions?.map((t) => (
                    {
                        ...requiredQuestionFields(t),
                        response: undefined
                    }
                ))],

                response: [],

                // by default the dropdown should be toggled to not show the tree
                dropdownResponse: null
            }
        ));


    // current row arc Id, question Id for which we are updating the response, the actual response
    const updateResponse = (arcId, questionId, response) => {
        console.log(response);

        $arcSurveyState = $arcSurveyState.map(t => {
            if(t.entityRef.id === arcId) {
                t.questions = t?.questions?.map(q => {
                    if(q.id === questionId) {
                        q.response = response;
                    }
                    return q;
                })
            }
            return t;
        });
    }

    const updateDropdownResponse = (arcId, response) => {
        $arcSurveyState = $arcSurveyState
            .map(t => {
            if(t.entityRef.id === arcId) {
                t = {
                    ...t,
                    dropdownResponse: response
                }
            }
            return t;
        })
    }

    const selectTreeItem = (arcId, item) => {
        $arcSurveyState = $arcSurveyState.map(t => {
            if(t.entityRef.id === arcId) {
                t.response.push(item);
            }
            return t;
        })
    }

    const deselectTreeItem = (arcId, item) => {
        $arcSurveyState = $arcSurveyState.map(t => {
            if(t.entityRef.id === arcId) {
                t = {
                    ...t,
                    response: t.response.filter(it => it.id !== item.id)
                }
            }
            return t;
        })
    }

    // response is $arcSurveyState object
    const validateSubmission = () => {
        if(!$arcSurveyState.length) {
            displayError("Cannot save an empty response");
            throw Error("Cannot save an empty response");
        }

        $arcSurveyState = $arcSurveyState.map(t => {
            // if items from the tree are to be selected
            if(t?.dropdownResponse === dropdownDefinition?.inclusionOption) {
                // if there is not a selected item
                if(!t?.response?.length) {
                    $invalidRows = [...$invalidRows.filter(x => x?.id !== t?.entityRef?.id),
                        {id: t?.entityRef?.id, message: `You have selected ${t?.dropdownResponse}, but did not select an ARC`}];
                    displayError(`You have selected ${t?.dropdownResponse}, but did not select an ARC`);
                }
            }

            else if(t?.dropdownResponse !== null) {
                t.response = [];

                // if this row was previously a part of the invalid rows
                $invalidRows = $invalidRows?.filter(x => x?.id !== t?.entityRef?.id);
            }

            // dropdown is mandatory
            else if(t.dropdownResponse === null){
                $invalidRows = [...$invalidRows.filter(x => x?.id !== t?.entityRef?.id),
                    {id: t?.entityRef?.id, message: `You have not selected an option from the dropdown for ${t?.entityRef?.title}`}];
                displayError(`You have not selected an option from the dropdown for ${t?.entityRef?.title}`);
            }

            return t;
        });

        if($invalidRows?.length) {
            throw new Error("Error saving response");
        }
    }

    $: saveJsonResponseCall = () => {
        try {
            validateSubmission();

            $invalidRows = [];

            const payload = {
                responseType: "ARC",
                responses: [...$arcSurveyState]
            }

            savingResponse = true;
            surveyInstanceStore
                .saveResponse(instanceId, {questionId: question.id, jsonResponse: JSON.stringify(payload)})
                .then(() => {
                    savingResponse = false;
                    toastStore.success("Table saved successfully.")
                })
                .catch((e) => {
                    savingResponse = false;
                    console.error({erroe: e});
                });

        } catch (e) {
            displayError("Error saving response.");
        }
    }

    onDestroy(() => {
        $arcSurveyState = [];
        $invalidRows = [];
    })

</script>

<br/>
<div class="table-container">
    <table>
        <thead>
        <tr>
            {#each tableHeadings as heading}
                <th>{heading}</th>
            {/each}
        </tr>
        </thead>
        <tbody>
        {#if arcs}
            {#each arcs as arc}
                <tr class:bg-danger={$invalidRows.find(t => t.id === arc?.id)} title={$invalidRows.find(t => t.id === arc?.id)?.message}>
                    <!-- Actual ARC -->
                    <td>
                        <Tooltip content={ArcTooltip}
                                 props={{node: arc, url}}>
                            <span slot="target" class="clickable secondary-link">{arc?.title}</span>
                        </Tooltip>
                    </td>

                    <!-- Milestone RAG -->
                    <td>
                        <h5>Rag Rating: {arc.milestoneRag}</h5>
                        <hr/>
                        <h5>Forecast Date</h5>
                        <h5>{arc.milestoneForecastDate}</h5>
                    </td>

                    <!-- Dropdown -->
                    <td>
                        {#if mode === MODES.EDIT}
                            <DropdownPicker items={dropdownItems}
                                            onSelect={(r) => updateDropdownResponse(arc.id, r.name)}
                                            selectedItem={getArcRow(arc.id)?.dropdownResponse ? {name: getArcRow(arc.id)?.dropdownResponse} : null}/>
                        {/if}
                        {#if mode === MODES.VIEW}
                            <p>{getArcRow(arc.id)?.dropdownResponse ?? "-"}</p>
                        {/if}
                    </td>

                    <!-- Tree -->
                    <td>
                        {#if $arcSurveyState.find(t => t.entityRef.id === arc.id)?.dropdownResponse === dropdownDefinition.inclusionOption}
                            <ARCTree items={arcs}
                                     onSelectItem={(r) => selectTreeItem(arc.id, r)}
                                     onDeselectItem={(r) => deselectTreeItem(arc.id, r)}
                                     selectedItems={getSelectedItems(arc.id)}
                                     {mode}
                                     {url}/>
                        {/if}
                    </td>


                    <!-- Sub Questions -->
                    {#if question?.subQuestions}
                    {#each question.subQuestions as subQuestion}
                        <td>
                        <!-- RENDERER FOR SUBQUESTION -->
                            <ARCSubQuestions question={subQuestion}
                                             selectResponse={(r) => updateResponse(arc?.id, subQuestion?.id, r)}
                                             mode={mode}
                                             response={getArcRow(arc.id)?.questions?.find(q => q?.id === subQuestion?.id)?.response ?? "-"}/>
                        </td>
                    {/each}
                    {/if}
                </tr>
            {/each}
        {/if}
        </tbody>
    </table>
</div>

<br/>

{#if mode === MODES.EDIT}
    <div class="save-response">
        <button class="btn btn-success"
                disabled={savingResponse}
                on:click={saveJsonResponseCall}>
            {#if savingResponse}
                <i class="fa fa-spin fa-spinner"></i> Saving...
            {:else}
                Save
            {/if}
        </button>
        <p class="help-block">Please periodically save the changes you have made.</p>
    </div>
{/if}


<style>
    .table-container {
        overflow-x: auto;
        width: 100%;
    }

    table {
        width: 100%;
        table-layout: fixed;
        border-collapse: collapse;
    }

    th {
        width: 250px;
        padding: 8px;
        border: 1px solid #ddd;
        background-color: #f2f2f2;
        text-align: left;
        white-space: normal;
        vertical-align: top;
        font-weight: bold;
    }

    td {
        padding: 8px;
        border: 1px solid #ddd;
        white-space: normal;
        vertical-align: top;
    }

    .save-response {
        display: inline-flex;
        gap: 0.5rem;
        align-items: center;
    }

    .save-response .btn {
        padding-top: 0.25rem;
        padding-bottom: 0.25rem;
    }
</style>
