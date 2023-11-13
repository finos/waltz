<script>

    import {selectedLogicalFlow, selectedPhysicalFlow} from "./flow-details-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {entity} from "../../../../common/services/enums/entity";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import LastEdited from "../../../../common/svelte/LastEdited.svelte";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import {flowClassificationStore} from "../../../../svelte-stores/flow-classification-store";
    import _ from "lodash";
    import {
        toCriticalityName,
        toDataFormatKindName,
        toFrequencyKindName,
        toTransportKindName
    } from "../../../../physical-flows/svelte/physical-flow-registration-utils";
    import {enumValueStore} from "../../../../svelte-stores/enum-value-store";
    import {nestEnums} from "../../../../common/svelte/enum-utils";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import pageInfo from "../../../../svelte-stores/page-navigation-store";
    import {Directions} from "./flow-detail-utils";


    export let assessmentDefinitions = [];
    let enumsCall = enumValueStore.load();
    let flowClassificationCall = flowClassificationStore.findAll();
    $: permissionsCall = logicalFlowStore.findPermissionsForFlow($selectedLogicalFlow?.logicalFlow.id);
    $: permissions = $permissionsCall.data;
    $: hasEditPermission = _.some(permissions, d => _.includes(["ADD", "UPDATE", "REMOVE"], d));

    $: nestedEnums = nestEnums($enumsCall.data);
    $: flowClassifications = $flowClassificationCall?.data;
    $: flowClassificationsByCode = _.keyBy(flowClassifications, d => d.code);
    $: assessmentDefinitionsbyId = _.keyBy(assessmentDefinitions, d => d.id);

    function clearSelected() {
        $selectedPhysicalFlow = null;
        $selectedLogicalFlow = null;
    }

    $: selectedLogicalFlowRef = {
        kind: 'LOGICAL_DATA_FLOW',
        id: $selectedLogicalFlow?.logicalFlow.id,
        name: "Logical Flow"
    }

    $: selectedPhysicalFlowRef = {
        kind: 'PHYSICAL_FLOW',
        id: $selectedPhysicalFlow?.physicalFlow.id,
        name: $selectedPhysicalFlow?.physicalFlow.name || $selectedPhysicalFlow?.specification.name || 'unnamed'
    }

    function goToPhysicalFlowEdit(flow) {
        $pageInfo = {
            state: "main.physical-flow.registration",
            params: {
                targetLogicalFlowId: flow.logicalFlow.id,
                kind: flow.direction === Directions.INBOUND ? flow.logicalFlow.target.kind : flow.logicalFlow.source.kind,
                id: flow.direction === Directions.INBOUND ? flow.logicalFlow.target.id : flow.logicalFlow.source.id
            }
        };
    }

</script>


<div class="waltz-sticky-part">

    <div class="row">
        <div class="col-md-12">
            <button class="btn btn-skinny detail-controls"
                    on:click={() => clearSelected()}>
                <span class="pull-right">
                    <Icon name="times"/>
                    Close Detail View
                </span>
            </button>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            {#if $selectedLogicalFlow}
                <div style="margin-bottom: 1em; margin-top: 0.5em">
                    <h4>
                        <span>
                            Logical Flow
                            <Icon name={_.get(entity, [$selectedLogicalFlow.logicalFlow.source.kind, "icon"])}/>
                            <Icon name="arrow-right"/>
                            <Icon name={_.get(entity, [$selectedLogicalFlow.logicalFlow.target.kind, "icon"])}/>
                        </span>
                    </h4>

                    <table class="table table-condensed small">
                        <tbody>
                        <tr>
                            <td style="width: 20%">Name</td>
                            <td>
                                <EntityLink ref={selectedLogicalFlowRef}>
                                </EntityLink>
                            </td>
                        </tr>
                        <tr>
                            <td>Source</td>
                            <td>
                                <EntityLink ref={$selectedLogicalFlow.logicalFlow.source}
                                            isSecondaryLink={true}/>
                            </td>
                        </tr>
                        <tr>
                            <td>Target</td>
                            <td>
                                <EntityLink ref={$selectedLogicalFlow.logicalFlow.target}
                                            isSecondaryLink={true}/>
                            </td>
                        </tr>
                        </tbody>
                    </table>

                    <div class="small">
                        <LastEdited entity={$selectedLogicalFlow.logicalFlow}/>
                    </div>

                    <div style="padding-top: 2em">
                        <strong>Data Types</strong>
                        <ul class="list-inline small">
                            {#each $selectedLogicalFlow.dataTypesForLogicalFlow as type}
                                <li style="padding-bottom: 2px">
                                    <div style={`display: inline-block; height: 1em; width: 1em; border:1px solid #ccc; border-radius: 2px; background-color: ${flowClassificationsByCode[type.rating]?.color}`}>
                                    </div>
                                    <EntityLink ref={type.decoratorEntity}
                                                showIcon={false}>
                                    </EntityLink>
                                </li>
                            {/each}
                        </ul>
                    </div>

                    {#if _.size($selectedLogicalFlow.ratingsByDefId) > 0}
                        <div>
                            <strong>Assessments</strong>
                            <table class="table table-condensed small">
                                <thead>
                                <tr>
                                    <td>Definition</td>
                                    <td>Ratings</td>
                                </tr>
                                </thead>
                                <tbody>
                                {#each _.keys($selectedLogicalFlow.ratingsByDefId) as defnId}
                                    {@const ratings = _.get($selectedLogicalFlow.ratingsByDefId, defnId, [])}
                                    <tr>
                                        <td>
                                            {_.get(assessmentDefinitionsbyId, [defnId, "name"], "Unknown")}
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
                        </div>
                    {/if}

                    {#if hasEditPermission}
                        <div>
                            <ul class="list-unstyled">
                                <li>
                                    <button class="btn btn-skinny"
                                            on:click={() => goToPhysicalFlowEdit($selectedLogicalFlow)}>
                                        <Icon name="plus"/> Add physical flow
                                    </button>
                                </li>
                            </ul>
                        </div>
                    {/if}
                </div>
            {/if}
            {#if $selectedPhysicalFlow}
                <hr>
                <h4>
                    <span>
                        Physical Flow
                        <Icon name={_.get(entity, [$selectedPhysicalFlow.logicalFlow.source.kind, "icon"])}/>
                        <Icon name="arrow-right"/>
                        <Icon name={_.get(entity, [$selectedPhysicalFlow.logicalFlow.target.kind, "icon"])}/>
                    </span>
                </h4>

                <table class="table table-condensed small">
                    <tbody>
                    <tr>
                        <td style="width: 20%">Name</td>
                        <td>
                            <EntityLink ref={selectedPhysicalFlowRef}/>
                        </td>
                    </tr>
                    <tr>
                        <td>External Id</td>
                        <td>{$selectedPhysicalFlow.physicalFlow.externalId || "-"}</td>
                    </tr>
                    <tr>
                        <td>Specification</td>
                        <td>
                            <EntityLink ref={$selectedPhysicalFlow.specification}
                                        isSecondaryLink={true}/>
                        </td>
                    </tr>
                    <tr>
                        <td>Specification Format</td>
                        <td>
                            {toDataFormatKindName(nestedEnums, $selectedPhysicalFlow.specification.format)}
                        </td>
                    </tr>
                    <tr>
                    <tr>
                        <td>Criticality</td>
                        <td>
                            {toCriticalityName(nestedEnums, $selectedPhysicalFlow.physicalFlow.criticality)}
                        </td>
                    </tr>
                    <tr>
                        <td>Frequency</td>
                        <td>
                            {toFrequencyKindName(nestedEnums, $selectedPhysicalFlow.physicalFlow.frequency)}
                        </td>
                    </tr>
                    <tr>
                        <td>Transport Kind</td>
                        <td>
                            {toTransportKindName(nestedEnums, $selectedPhysicalFlow.physicalFlow.transport)}
                        </td>
                    </tr>
                    <tr>
                        <td>Basis Offset</td>
                        <td>
                            {$selectedPhysicalFlow.physicalFlow.basisOffset}
                        </td>
                    </tr>
                    {#if $selectedPhysicalFlow.physicalFlow.description}
                        <tr>
                            <td>Description</td>
                            <td>
                                {$selectedPhysicalFlow.physicalFlow.description}
                            </td>
                        </tr>
                    {/if}
                    </tbody>
                </table>

                <div class="small">
                    <LastEdited entity={$selectedPhysicalFlow.physicalFlow}/>
                </div>
            {/if}
        </div>
    </div>
</div>

<style>

    .detail-controls {
        padding: 0.5rem;
        width: 100%;
        background-color: #eee;
        border: 1px solid #eee;
    }

</style>