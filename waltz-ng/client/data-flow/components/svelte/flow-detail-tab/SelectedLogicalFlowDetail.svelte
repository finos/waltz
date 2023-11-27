<script>
    import _ from "lodash";
    import {entity} from "../../../../common/services/enums/entity";
    import {selectedLogicalFlow} from "./flow-details-store";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import pageInfo from "../../../../svelte-stores/page-navigation-store";
    import {Directions} from "./flow-detail-utils";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import DataTypeMiniTable from "./DataTypeMiniTable.svelte";
    import AssessmentsTable from "./widgets/AssessmentsTable.svelte";

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

    export let flowClassifications = [];
    export let assessmentDefinitions = [];

    let permissionsCall = null;
    let hasEditPermission = false;
    let assessmentDefinitionsById = {};
    let flowClassificationsByCode = {};

    $: permissionsCall = logicalFlowStore.findPermissionsForFlow($selectedLogicalFlow?.logicalFlow.id);
    $: permissions = $permissionsCall?.data;
    $: hasEditPermission = _.some(permissions, d => _.includes(["ADD", "UPDATE", "REMOVE"], d));
    $: flowClassificationsByCode = _.keyBy(flowClassifications, d => d.code);
    $: assessmentDefinitionsById = _.keyBy(assessmentDefinitions, d => d.id);
    $: flow = $selectedLogicalFlow.logicalFlow;
    $: dataTypes = $selectedLogicalFlow.dataTypesForLogicalFlow;
    $: logicalFlowRatingsByDefId = $selectedLogicalFlow.logicalFlowRatingsByDefId;
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
            <DataTypeMiniTable decorators={dataTypes}
                               {flowClassifications}/>
        </td>
    </tr>
    </tbody>
</table>


{#if !_.isEmpty(logicalFlowRatingsByDefId)}
    <AssessmentsTable ratingsByDefId={logicalFlowRatingsByDefId}
                      {assessmentDefinitionsById}/>
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
    menu {
        padding-left: 1em;
    }

    menu li {
        list-style: none;
    }
</style>