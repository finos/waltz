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
    import {arcSurveyState, invalidRows} from "./ARCSurveyState";
    import ARCTree from "./ARCTree.svelte";
    import DropdownPicker from "../../../../common/svelte/DropdownPicker.svelte";
    import DateTime from "../../../../common/svelte/DateTime.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import {surveyInstanceStore} from "../../../../svelte-stores/survey-instance-store";
    import toastStore from "../../../../svelte-stores/toast-store";
    import {displayError} from "../../../../common/error-utils";
    import {onDestroy} from "svelte";
    import ARCTableView from "./ARCTableView.svelte";
    import {mkResponseObject} from "./arc-survey-utils";

    export let arcs;
    export let arcHierarchy;
    export let tableHeadings;
    export let url;
    export let dropdownDefinition;
    export let dropdownItems;
    export let currentResponse;
    export let instanceId;
    export let question;

    let savingResponse = false;
    let startedOver = false;

    const getArcRow = (arcId) => {
        return $arcSurveyState.find(t => t.entityRef.id === arcId);
    }

    const getSelectedItems = (arcId) => {
        return getArcRow(arcId)?.response ?? [];
    }

    const updateDropdownResponse = (arcId, dropdownResponse) => {
        if(getArcRow(arcId)) {
            $arcSurveyState = $arcSurveyState
                .map(t => {
                    if (t.entityRef.id === arcId) {
                        t = {
                            ...t,
                            dropdownResponse: dropdownResponse
                        }
                    }
                    return t;
                })
        } else {
            const arc = arcs.find(t => t.id === arcId);
            const responseObject = {
                entityRef: arc,
                response: [],
                dropdownResponse: dropdownResponse
            };
            $arcSurveyState = [...$arcSurveyState, responseObject];
        }
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

    const validateSubmission = () => {
        if(!$arcSurveyState.length) {

            // if there are no arcs to even begin with
            if(!arcs.length) {
                return;
            }

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
                } else {
                    $invalidRows = $invalidRows?.filter(x => x?.id !== t?.entityRef?.id);
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

            else {
                $invalidRows = $invalidRows?.filter(x => x?.id !== t?.entityRef?.id);
            }

            return t;
        });

        if($invalidRows?.length) {
            throw new Error("Error saving response");
        }
    }

    const onStartOver = () => {
        startedOver = true;
        $arcSurveyState = arcs.map(t => mkResponseObject(t));
    }

    const saveJsonResponse = () => {
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
                    toastStore.success("Table saved successfully.");
                    startedOver = false;
                })
                .catch((e) => {
                    savingResponse = false;
                    console.error({error: e});
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

{#if currentResponse === undefined || startedOver}
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
                                 props={{node: arc, url}} placement="right">
                            <span slot="target" class="clickable secondary-link">{arc?.title}</span>
                        </Tooltip>
                    </td>

                    <!-- Milestone RAG -->
                    <td>
                        <h5>Rag Rating: {arc.milestoneRag}</h5>
                        <hr/>
                        <h5>Forecast Date</h5>
                        <DateTime relative={false} formatStr="YYYY-MM-DD">
                            <small class="text-muted">{arc.milestoneForecastDate}</small>
                        </DateTime>
                    </td>

                    <!-- Dropdown -->
                    <td>
                        <DropdownPicker items={dropdownItems}
                                        onSelect={(r) => updateDropdownResponse(arc.id, r?.name ?? null)}
                                        selectedItem={getArcRow(arc.id)?.dropdownResponse ? {name: getArcRow(arc.id)?.dropdownResponse} : null}/>
                    </td>

                    <!-- Tree -->
                    <td>
                        {#if arcHierarchy && $arcSurveyState.find(t => t.entityRef.id === arc.id)?.dropdownResponse === dropdownDefinition?.inclusionOption}
                            <ARCTree items={arcHierarchy.filter(t => t.externalParentId === arc.externalId)}
                                     onSelectItem={(r) => selectTreeItem(arc.id, r)}
                                     onDeselectItem={(r) => deselectTreeItem(arc.id, r)}
                                     selectedItems={getSelectedItems(arc.id)}
                                     mode={"EDIT"}
                                     {url}/>
                        {/if}
                    </td>
                </tr>
            {/each}
        {/if}
        </tbody>
    </table>
</div>
<br/>
<div class="save-response">
    <button class="btn btn-success"
            disabled={savingResponse}
            on:click={saveJsonResponse}>
        {#if savingResponse}
            <i class="fa fa-spin fa-spinner"></i> Saving...
        {:else}
            Save
        {/if}
    </button>
    <p class="help-block">Please periodically save the changes you have made.</p>
</div>
{:else}
    <NoData type="info">
        You have already filled the table, filling it again would require you to
        <button class="btn btn-skinny" on:click={onStartOver}>start over.</button>
    </NoData>
    <ARCTableView arcs={currentResponse.map(t => t.entityRef)}
                  arcHierarchy={currentResponse?.flatMap(t => t.response)}
                  {tableHeadings}
                  {url}
                  {dropdownDefinition}/>
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