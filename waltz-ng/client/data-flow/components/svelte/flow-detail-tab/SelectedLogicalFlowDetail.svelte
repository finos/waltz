<script>
    import _ from "lodash";
    import {entity} from "../../../../common/services/enums/entity";
    import {selectedLogicalFlow} from "./flow-details-store";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import pageInfo from "../../../../svelte-stores/page-navigation-store";
    import {Directions} from "./flow-detail-utils";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import DataTypeMiniTable from "./DataTypeMiniTable.svelte";
    import AssessmentsTable from "./widgets/AssessmentsTable.svelte";
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";
    import {createEventDispatcher} from "svelte";
    import toastStore from "../../../../svelte-stores/toast-store";

    const ActionSectionStates = {
        LIST: "LIST",
        REMOVAL_CONFIRMATION: "REMOVAL_CONFIRMATION"
    };

    const dispatch = createEventDispatcher();

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

    function doRemove(flow) {
        logicalFlowStore
            .removeFlow(flow.id)
            .then(() => {
                toastStore.info("Logical Flow removed, reloading");
                dispatch("reload");
                dispatch("clearSelected");
            });
    }

    export let flowClassifications = [];
    export let assessmentDefinitions = [];

    let actionSectionState = ActionSectionStates.LIST;
    let permissionsCall = null;
    let hasAddPermission = false;
    let hasUpdatePermission = false;
    let hasRemovePermission = false;
    let assessmentDefinitionsById = {};
    let flowClassificationsByCode = {};

    $: permissionsCall = logicalFlowStore.findPermissionsForFlow($selectedLogicalFlow?.logicalFlow.id);
    $: permissions = $permissionsCall?.data;
    $: hasAddPermission = _.includes(permissions, "ADD");
    $: hasUpdatePermission = _.includes(permissions, "ADD");
    $: hasRemovePermission = _.includes(permissions, "ADD");
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
        {#if actionSectionState === ActionSectionStates.LIST }
            {#if hasAddPermission}
                <li>
                    <button class="btn btn-skinny btn-success"
                            on:click={() => goToPhysicalFlowEdit(flow)}>
                        <Icon name="plus"/>
                        Add physical flow
                    </button>
                    <span class="help-block small">
                        This will open the flow registration page
                    </span>
                </li>
            {/if}
            {#if hasRemovePermission}
                <li>
                    <button class="btn btn-skinny btn-danger"
                            on:click={() => actionSectionState = ActionSectionStates.REMOVAL_CONFIRMATION}>
                        <Icon name="trash"/>
                        Remove logical flow
                    </button>
                    <p class="help-block small">
                        This will remove the logical flow
                        {#if $selectedLogicalFlow.physicalCount > 0}
                            and the {$selectedLogicalFlow.physicalCount} associated physical flows
                        {/if}

                        Note: removed flows may be restored via the link in the changelog.
                    </p>
                </li>
            {/if}
            <li>
                <button class="btn btn-skinny"
                        on:click={() => goToLogicalFlowPage(flow)}>
                    <Icon name="random"/>
                    Visit the logical flow page
                </button>
                {#if hasUpdatePermission}
                    <span class="help-block small">
                        To remove the flow or edit it's data types
                    </span>
                {/if}
            </li>
        {/if}
        {#if actionSectionState === ActionSectionStates.REMOVAL_CONFIRMATION}
            <div class="removal-box">
                <h4>Removal Confirmation:</h4>
                <p>
                    Are you sure you want to remove the flow ?
                </p>
                <p>
                    <EntityLabel ref={flow.source}/>
                    <Icon name="arrow-right"/>
                    <EntityLabel ref={flow.target}/>
                </p>
                {#if $selectedLogicalFlow.physicalCount > 0}
                    <p>
                        This will also remove <strong>{$selectedLogicalFlow.physicalCount}</strong>
                        associated physical flows.
                    </p>
                {/if}

                <button class="btn btn-skinny"
                        on:click={() => doRemove(flow)}>
                    <Icon name="trash"/>
                    Remove
                </button>
                <button class="btn btn-skinny"
                        on:click={() => actionSectionState = ActionSectionStates.LIST}>
                    Cancel
                </button>
            </div>
        {/if}
    </menu>
</details>


<style>
    menu {
        padding-left: 1em;
    }

    menu li {
        list-style: none;
    }

    .removal-box{
        border-width: 1px;
        border-style: solid;
        border-color: #d93f44;
        background-color: #fae9ee;
        padding-left: 2em;
        padding-right: 2em;
        padding-bottom: 1.5em;
        padding-top: 1.5em;
        border-radius: 2px;
    }
</style>