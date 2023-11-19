<script>

    import {dataTypeStore} from "../../../../svelte-stores/data-type-store";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import _ from "lodash";
    import {reduceToSelectedNodesOnly} from "../../../../common/hierarchy-utils";
    import LogicalFlowTable from "./LogicalFlowTable.svelte";
    import {filters, resetFlowDetailsStore, selectedLogicalFlow, selectedPhysicalFlow} from "./flow-details-store";
    import PhysicalFlowTable from "./PhysicalFlowTable.svelte";
    import {
        Directions,
        FilterKinds,
        mkAssessmentFilters,
        mkFlowDetails,
        mkLogicalFromFlowDetails
    } from "./flow-detail-utils";
    import SelectedFlowDetail from "./SelectedFlowDetailPanel.svelte";
    import AssessmentFilters from "./AssessmentFilters.svelte";
    import DataTypeFilters from "./DataTypeFilters.svelte";
    import InboundOutboundFilters from "./InboundOutboundFilters.svelte";
    import {onMount} from "svelte";
    import PhysicalFlowAttributeFilters from "./PhysicalFlowAttributeFilters.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import DataExtractLink from "../../../../common/svelte/DataExtractLink.svelte";

    export let parentEntityRef;

    let selectionOptions;
    let flowViewCall;
    let flowView;
    let mappedDataTypes = [];
    let assessmentFilters = [];
    let dataTypes = [];
    let disableNodeFn = () => false;
    let allFlows = [];
    let physicalFlows = [];
    let logicalFlows = [];

    $: {
        if (parentEntityRef) {
            selectionOptions = mkSelectionOptions(parentEntityRef);
            flowViewCall = logicalFlowStore.getViewForSelector(selectionOptions);
        }
    }

    onMount(() => resetFlowDetailsStore());

    let dataTypesCall = dataTypeStore.findAll();
    $: allDataTypes = $dataTypesCall?.data;

    $: flowView = $flowViewCall?.data;


    $: {
        if (flowView && allDataTypes) {

            mappedDataTypes = _
                .chain(flowView.dataTypeDecorators, d => d.decoratorEntity)
                .uniq()
                .orderBy(d => d.name)
                .value();

            const mappedDataTypeIds = _.map(mappedDataTypes, d => d.dataTypeId);

            dataTypes = reduceToSelectedNodesOnly(allDataTypes, mappedDataTypeIds);

            disableNodeFn = (node) => !_.includes(_.map(dataTypes, dt => dt.dataTypeId), node.id);

            assessmentFilters = mkAssessmentFilters(flowView);

            allFlows = mkFlowDetails(flowView, parentEntityRef);
        }
    }

    function filterFlows(allFlows, filters) {
        return _
            .chain(allFlows)
            .map(d => Object.assign(d, {visible: _.every(filters, f => f.test(d))}))
            .value();
    }

    $: filteredFlows = filterFlows(allFlows, $filters);

    $: physicalFlows = _.filter(filteredFlows, d => !_.isEmpty(d.physicalFlow));

    $: logicalFlows = _
        .chain(filteredFlows)
        .map(d => mkLogicalFromFlowDetails(d))
        .uniqBy(d => d.logicalFlow.id)
        .value();

    $: logicalFlowPrimaryAssessments = _.get(flowView, "primaryAssessmentDefinitions", []);

</script>



<div class="flow-detail-panel">
    <div class="flow-detail-table">
        <details>

            <summary>
                Filters
                {#if !_.isEmpty($filters)}
                    <button class="btn btn-skinny"
                            on:click={() => $filters = []}>
                        Clear All
                    </button>
                {/if}
            </summary>

            <details class="filter-set" style="margin-top: 1em">
                <summary>
                    <Icon name="random"/> Flow Direction
                    {#if _.some($filters, d => d.kind === FilterKinds.DIRECTION) && _.find($filters, d => d.kind === FilterKinds.DIRECTION).direction !== Directions.ALL}
                        <span style="color: darkorange"
                              title="Flows have been filtered by direction">
                            <Icon name="exclamation-circle"/>
                        </span>
                    {/if}
                </summary>
                <InboundOutboundFilters/>
            </details>

            <details class="filter-set">
                <summary>
                    <Icon name="qrcode"/> Data Types
                    {#if _.some($filters, d => d.kind === FilterKinds.DATA_TYPE)}
                        <span style="color: darkorange"
                              title="Data type filters have been applied">
                            <Icon name="exclamation-circle"/>
                        </span>
                    {/if}
                </summary>
                <DataTypeFilters {dataTypes}/>
            </details>

            <details class="filter-set">
                <summary>
                    <Icon name="puzzle-piece"/> Assessments
                    {#if _.some($filters, d => d.kind === FilterKinds.ASSESSMENT)}
                        <span style="color: darkorange"
                              title="Assessment filters have been applied">
                            <Icon name="exclamation-circle"/>
                        </span>
                    {/if}
                </summary>
                <AssessmentFilters {assessmentFilters}/>
            </details>

            <details class="filter-set">
                <summary>
                    <Icon name="asterisk"/> Physical Flow
                    {#if _.some($filters, d => d.kind === FilterKinds.PHYSICAL_FLOW_ATTRIBUTE)}
                        <span style="color: darkorange"
                              title="Physical flow attribute filters have been applied">
                            <Icon name="exclamation-circle"/>
                        </span>
                    {/if}
                </summary>
                <PhysicalFlowAttributeFilters flows={physicalFlows}/>
            </details>
        </details>

        <LogicalFlowTable {logicalFlows}
                          assessments={logicalFlowPrimaryAssessments}/>
        <br>
        <PhysicalFlowTable {physicalFlows}/>

        <div style="padding-top: 1em" class="pull-right">
            <span>
                <DataExtractLink name="Export Logical Flow Details"
                                 filename="Logical Flows"
                                 extractUrl="logical-flow-view"
                                 method="POST"
                                 requestBody={selectionOptions}
                                 styling="link"/>
                |
                <DataExtractLink name="Export Physical Flow Details"
                                 filename="Physical Flows"
                                 extractUrl={`physical-flows/all/${parentEntityRef.kind}/${parentEntityRef.id}`}
                                 method="POST"
                                 styling="link"/>
            </span>
        </div>

    </div>
    {#if $selectedLogicalFlow || $selectedPhysicalFlow}
        <div class="flow-detail-context-panel">
            <SelectedFlowDetail assessmentDefinitions={logicalFlowPrimaryAssessments}/>
        </div>
    {/if}
</div>

<style>

    .flow-detail-context-panel {
        width: 30%;
        padding-left: 1em;
        border-left: 1px solid #eee;
    }

    .flow-detail-panel {
        display: flex;
        gap: 10px;
    }

    .flow-detail-table {
        width: 70%;
        flex: 1 1 50%
    }

    .filter-set {
        background-color: #fafafa;
    }

</style>