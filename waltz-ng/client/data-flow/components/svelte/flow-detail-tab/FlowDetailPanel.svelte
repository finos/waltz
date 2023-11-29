<script>
    import {dataTypeStore} from "../../../../svelte-stores/data-type-store";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import _ from "lodash";
    import {reduceToSelectedNodesOnly} from "../../../../common/hierarchy-utils";
    import LogicalFlowTable from "./LogicalFlowTable.svelte";
    import {filters, resetFlowDetailsStore, selectedLogicalFlow, selectedPhysicalFlow} from "./flow-details-store";
    import PhysicalFlowTable from "./PhysicalFlowTable.svelte";
    import {mkFlowDetails} from "./flow-detail-utils";
    import {mkAssessmentFilters} from "./filters/filter-utils";
    import SelectedFlowDetailPanel from "./SelectedFlowDetailPanel.svelte";
    import {onMount} from "svelte";
    import DataExtractLink from "../../../../common/svelte/DataExtractLink.svelte";
    import {flowClassificationStore} from "../../../../svelte-stores/flow-classification-store";
    import FlowDetailFilters from "./filters/FlowDetailFilters.svelte";

    export let parentEntityRef;

    function filterFlows(allFlows, filters) {
        console.log("ff", {allFlows, filters})
        return _
            .chain(allFlows)
            .map(d => Object.assign(d, {visible: _.every(filters, f => f.test(d))}))
            .value();
    }

    let selectionOptions;

    let flowViewCall = null;
    let flowClassificationCall = null;
    let dataTypesCall = null;

    let flowView;
    let mappedDataTypes = [];
    let assessmentFilters = [];
    let dataTypes = [];
    let allFlows = [];
    let physicalFlows = [];
    let logicalFlows = [];

    onMount(() => {
        resetFlowDetailsStore();
        flowClassificationCall = flowClassificationStore.findAll();
        dataTypesCall = dataTypeStore.findAll();
    });

    $: allDataTypes = $dataTypesCall?.data;
    $: flowClassifications = $flowClassificationCall?.data;
    $: flowView = $flowViewCall?.data;

    $: filteredFlows = filterFlows(allFlows, $filters);
    $: physicalFlows = _.filter(filteredFlows, d => !_.isEmpty(d.physicalFlow));
    $: allAssessmentDefinitions = _.concat(
        flowView?.logicalFlowAssessmentDefinitions || [],
        flowView?.physicalFlowAssessmentDefinitions || [],
        flowView?.physicalSpecificationAssessmentDefinitions || []);

    $: logicalFlows = _
        .chain(filteredFlows)
        .uniqBy(d => d.logicalFlow.id)
        .value();

    $: {
        if (flowView && allDataTypes) {
            mappedDataTypes = _
                .chain(flowView.dataTypeDecorators, d => d.decoratorEntity)
                .uniq()
                .orderBy(d => d.name)
                .value();

            const mappedDataTypeIds = _.map(mappedDataTypes, d => d.dataTypeId);
            dataTypes = reduceToSelectedNodesOnly(allDataTypes, mappedDataTypeIds);
            assessmentFilters = mkAssessmentFilters(flowView);
            allFlows = mkFlowDetails(flowView, parentEntityRef);
        }
    }

    $: {
        if (parentEntityRef) {
            selectionOptions = mkSelectionOptions(parentEntityRef);
            flowViewCall = logicalFlowStore.getViewForSelector(selectionOptions);
        }
    }
</script>


<div class="flow-detail-panel">
    <div class="flow-detail-table">
        <FlowDetailFilters {dataTypes}
                           {assessmentFilters}
                           {physicalFlows}/>

        <LogicalFlowTable {logicalFlows}
                          {flowClassifications}
                          assessmentDefinitions={allAssessmentDefinitions}/>
        <br>
        <PhysicalFlowTable {physicalFlows}
                           {flowClassifications}
                           assessmentDefinitions={allAssessmentDefinitions}/>

        <div style="padding-top: 1em"
             class="pull-right">
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
            <SelectedFlowDetailPanel flowClassifications={flowClassifications}
                                     assessmentDefinitions={allAssessmentDefinitions}/>
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
</style>