<script>
    import _ from "lodash";
    import {entity} from "../../../../common/services/enums/entity";
    import {selectedLogicalFlow} from "./flow-details-store";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import pageInfo from "../../../../svelte-stores/page-navigation-store";
    import {Directions} from "./flow-detail-utils";
    import {flowClassificationStore} from "../../../../svelte-stores/flow-classification-store";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";

    function goToPhysicalFlowEdit(flow) {
        $pageInfo = {
            state: "main.physical-flow.registration",
            params: {
                targetLogicalFlowId: flow.id,
                kind: flow.direction === Directions.INBOUND ? flow.target.kind : flow.source.kind,
                id: flow.direction === Directions.INBOUND ? flow.target.id : flow.source.id
            }
        };
    }

    function goToLogicalFlowPage(flow) {
        $pageInfo = {
            state: "main.logical-flow.view",
            params: {
                id: flow.id
            }
        }
    }

    export let assessmentDefinitions;

    let flowClassificationCall = flowClassificationStore.findAll();
    let permissionsCall = null;
    let hasEditPermission = false;
    let assessmentDefinitionsById = {};
    let flowClassifications = [];
    let flowClassificationsByCode = {};

    $: permissionsCall = logicalFlowStore.findPermissionsForFlow($selectedLogicalFlow?.logicalFlow.id);
    $: permissions = $permissionsCall?.data;
    $: hasEditPermission = _.some(permissions, d => _.includes(["ADD", "UPDATE", "REMOVE"], d));
    $: flowClassifications = $flowClassificationCall?.data;
    $: flowClassificationsByCode = _.keyBy(flowClassifications, d => d.code);
    $: assessmentDefinitionsById = _.keyBy(assessmentDefinitions, d => d.id);
    $: flow = $selectedLogicalFlow.logicalFlow;
    $: dataTypes = $selectedLogicalFlow.dataTypesForLogicalFlow;
    $: ratingsByDefId = $selectedLogicalFlow.ratingsByDefId;
    $: ref = {
        id:flow.id,
        name: "Logical Flow",
        kind: "LOGICAL_DATA_FLOW"
    };
</script>

<h4>
    <span>
        Logical Flow
        <Icon name={_.get(entity, [flow.source.kind, "icon"])}/>
        <Icon name="arrow-right"/>
        <Icon name={_.get(entity, [flow.target.kind, "icon"])}/>
    </span>
</h4>

<table class="table table-condensed small">
    <tbody>
    <tr>
        <td style="width: 20%">Name</td>
        <td>
            <EntityLink ref={ref}>
            </EntityLink>
        </td>
    </tr>
    <tr>
        <td>Source</td>
        <td>
            <EntityLink ref={flow.source}
                        isSecondaryLink={true}/>
        </td>
    </tr>
    <tr>
        <td>Target</td>
        <td>
            <EntityLink ref={flow.target}
                        isSecondaryLink={true}/>
        </td>
    </tr>
    <tr>
        <td>Data Types</td>
        <td>
            <ul class="list-inline">
                {#each _.sortBy(dataTypes, d => d.decoratorEntity.name) as type}
                    <li style="padding-bottom: 2px">
                        <div class="rating-indicator-block"
                             style={`background-color: ${flowClassificationsByCode[type.rating]?.color}`}>
                        </div>
                        <EntityLink ref={type.decoratorEntity}
                                    showIcon={false}>
                        </EntityLink>
                    </li>
                {/each}
            </ul>
        </td>
    </tr>
    </tbody>
</table>


{#if !_.isEmpty(ratingsByDefId)}
    <table class="table table-condensed small">
        <thead>
        <tr>
            <th>Assessment</th>
            <th>Rating</th>
        </tr>
        </thead>
        <tbody>
        {#each _.keys(ratingsByDefId) as defnId}
            {@const ratings = _.get(ratingsByDefId, defnId, [])}
            <tr>
                <td>
                    {_.get(assessmentDefinitionsById, [defnId, "name"], "Unknown")}
                </td>
                <td>
                    <ul class="list-inline">
                        {#each ratings as rating}
                            <li>
                                <RatingIndicatorCell {...rating}/>
                            </li>
                        {/each}
                    </ul>
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}

<details>
    <summary>Actions</summary>
    <menu>
        {#if hasEditPermission}
            <li>
                <button class="btn btn-skinny"
                        on:click={() => goToPhysicalFlowEdit(flow)}>
                    Add physical flow
                </button>
                <span class="help-block">
                    This will open the flow registration page
                </span>
            </li>
        {/if}
        <li>
            <button class="btn btn-skinny"
                    on:click={() => goToLogicalFlowPage(flow)}>
                Visit the logical flow page
            </button>
            {#if hasEditPermission}
                <span class="help-block">
                    To remove the flow or edit it's data types
                </span>
            {/if}
        </li>
    </menu>
</details>


<style>
    .rating-indicator-block {
        display: inline-block;
        width: 1.1em;
        height: 1.1em;
        border: 1px solid #aaa;
        border-radius: 2px;
        position: relative;
        top: 2px;
    }

    menu {
        padding-left: 1em;
    }

    menu li {
        list-style: none;
    }
</style>