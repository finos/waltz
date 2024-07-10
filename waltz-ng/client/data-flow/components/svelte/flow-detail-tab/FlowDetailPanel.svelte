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
    import {getAssessmentFilters} from "./filters/filter-utils";
    import SelectedFlowDetailPanel from "./SelectedFlowDetailPanel.svelte";
    import {onMount} from "svelte";
    import DataExtractLink from "../../../../common/svelte/DataExtractLink.svelte";
    import {flowClassificationStore} from "../../../../svelte-stores/flow-classification-store";
    import FlowDetailFilters from "./filters/FlowDetailFilters.svelte";

    export let parentEntityRef;

    function filterFlows(allFlows, filters) {
        return _
            .chain(allFlows)
            .map(d => Object.assign(d, {visible: _.every(filters, f => f.test(d))}))
            .value();
    }

    function onReload() {
        logicalFlowStore.getViewForSelector(selectionOptions, true);
    }

    let selectionOptions;

    let flowViewCall = null;
    let flowClassificationCall = null;
    let dataTypesCall = null;

    let flowView;
    let mappedDataTypes = [];
    let assessmentFilters = [];
    let dataTypes = [];
    let allDataTypes = [];
    let flowClassifications = [];
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
        .groupBy(d => d.logicalFlow.id)
        .map((v, k) => {
            const lf = _.first(v);
            const physicalCount = lf.physicalFlow ? v.length : 0;
            return Object.assign(lf, { physicalCount });
        })
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
            assessmentFilters = getAssessmentFilters(flowView);
            allFlows = mkFlowDetails(flowView, parentEntityRef);
        }
    }

    $: {
        if (parentEntityRef && ! flowViewCall) {
            selectionOptions = mkSelectionOptions(parentEntityRef);
            flowViewCall = logicalFlowStore.getViewForSelector(selectionOptions);
        }
    }
</script>


<div class="flow-detail-panel">
    {#if $flowViewCall === null || $flowViewCall?.status === 'loading'}
        <div>
            <h3>Loading...</h3>
            <div class="help-block">
                This may take a few seconds on larger groups
            </div>
        </div>
    {:else}
        <div class="flow-detail-table">
            <FlowDetailFilters {dataTypes}
                               {assessmentFilters}
                               {flowClassifications}
                               {physicalFlows}/>

            <LogicalFlowTable {logicalFlows}
                              {flowClassifications}
                              assessmentDefinitions={allAssessmentDefinitions}/>
            <br>
            <PhysicalFlowTable {physicalFlows}
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
                                 extractUrl="logical-flow-view/physical-flows"
                                 method="POST"
                                 requestBody={selectionOptions}
                                 styling="link"/>
            </span>
            </div>

        </div>
        {#if $selectedLogicalFlow || $selectedPhysicalFlow}
            <div class="flow-detail-context-panel">
                <SelectedFlowDetailPanel on:reload={onReload}
                                         flowClassifications={flowClassifications}
                                         assessmentDefinitions={allAssessmentDefinitions}/>
            </div>
        {/if}
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