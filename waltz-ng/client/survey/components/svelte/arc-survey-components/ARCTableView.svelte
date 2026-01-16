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
    import ARCTree from "./ARCTree.svelte";
    import DateTime from "../../../../common/svelte/DateTime.svelte";
    import {arcSurveyState} from "./ARCSurveyState";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let arcs;
    export let arcHierarchy;
    export let tableHeadings;
    export let url;
    export let dropdownDefinition;

    const getArcRow = (arcId) => {
        return $arcSurveyState.find(t => t.entityRef.id === arcId);
    }

</script>

{#if arcs}
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
                <tr>
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
                        <p>{getArcRow(arc.id)?.dropdownResponse ?? "-"}</p>
                    </td>

                    <!-- Tree -->
                    <td>
                        {#if arcHierarchy && getArcRow(arc.id)?.dropdownResponse === dropdownDefinition?.inclusionOption}
                            <ARCTree items={arcHierarchy.filter(t => t.externalParentId === arc.externalId)}
                                     selectedItems={getArcRow(arc.id)?.response ?? []}
                                     mode={"VIEW"}
                                     {url}/>
                        {/if}
                    </td>
                </tr>
            {/each}
        {/if}
        </tbody>
    </table>
</div>
{:else}
    <NoData type="info">
        The response for this table has not yet been filled.
    </NoData>
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
</style>
