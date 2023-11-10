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
        toDataFormatKindName, toFrequencyKindName, toTransportKindName
    } from "../../../../physical-flows/svelte/physical-flow-registration-utils";
    import {enumValueStore} from "../../../../svelte-stores/enum-value-store";
    import {nestEnums} from "../../../../common/svelte/enum-utils";


    export let assessmentDefinitions = [];

    let enumsCall = enumValueStore.load();
    let flowClassificationCall = flowClassificationStore.findAll();

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
        id: $selectedLogicalFlow?.id,
        name: "Logical Flow"
    }

    $: selectedPhysicalFlowRef = {
        kind: 'PHYSICAL_FLOW',
        id: $selectedPhysicalFlow?.id,
        name: $selectedPhysicalFlow?.name || $selectedPhysicalFlow?.specification.name || 'unnamed'
    }

</script>


<div class="waltz-sticky-part">
    {#if $selectedLogicalFlow}
        <div style="margin-bottom: 4em">

            <h4>
                <span>
                    Logical Flow
                    <Icon name={_.get(entity, [$selectedLogicalFlow.logicalFlow.source.kind, "icon"])}/>
                    <Icon name="arrow-right"/>
                    <Icon name={_.get(entity, [$selectedLogicalFlow.logicalFlow.target.kind, "icon"])}/>
                </span>
                <button class="btn btn-skinny btn-xs"
                        on:click={() => clearSelected()}>
                    <Icon name="times"/>
                    Clear
                </button>
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
        </div>
    {/if}

    {#if $selectedPhysicalFlow}
        <h4>
            <span>
                Physical Flow
                <Icon name={_.get(entity, [$selectedPhysicalFlow.logicalFlow.source.kind, "icon"])}/>
                <Icon name="arrow-right"/>
                <Icon name={_.get(entity, [$selectedPhysicalFlow.logicalFlow.target.kind, "icon"])}/>
            </span>
            <button class="btn btn-skinny btn-xs"
                    on:click={() => clearSelected()}>
                <Icon name="times"/>
                Clear
            </button>
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
                <td>{$selectedPhysicalFlow.physicalFlow.externalId}</td>
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