<script>

    import {dataTypeStore} from "../../../../svelte-stores/data-type-store";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import _ from "lodash";
    import {reduceToSelectedNodesOnly} from "../../../../common/hierarchy-utils";
    import LogicalFlowTable from "./LogicalFlowTable.svelte";
    import {filters, selectedLogicalFlow, selectedPhysicalFlow} from "./flow-details-store";
    import PhysicalFlowTable from "./PhysicalFlowTable.svelte";
    import {mkAssessmentFilters, mkFlowDetails, mkLogicalFromFlowDetails} from "./flow-detail-utils";
    import SelectedFlowDetail from "./SelectedFlowDetail.svelte";
    import AssessmentFilters from "./AssessmentFilters.svelte";
    import DataTypeFilters from "./DataTypeFilters.svelte";
    import InboundOutboundFilters from "./InboundOutboundFilters.svelte";

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
            .filter(d => _.every(filters, f => f.test(d)))
            .value();
    }

    $: filteredFlows = filterFlows(allFlows, $filters);

    $: logicalFlows = _
        .chain(filteredFlows)
        .map(d => mkLogicalFromFlowDetails(d))
        .uniqBy(d => d.logicalFlow.id)
        .value();

    $: logicalFlowPrimaryAssessments = _.get(flowView, "primaryAssessmentDefinitions", []);

    $: console.log({filters: $filters});

</script>



<div class="flow-detail-panel">
    <div class="flow-detail-table">

        {#if !_.isEmpty(assessmentFilters)}
            <details>
                <summary>
                    Filters
                    {#if !_.isEmpty($filters)}
                        <button class="btn btn-skinny">
                            Clear All
                        </button>
                    {/if}
                </summary>

                <InboundOutboundFilters/>

                <AssessmentFilters {assessmentFilters}/>

                <DataTypeFilters {dataTypes}/>

            </details>
        {/if}

        <h4>Logical Flows</h4>
        <LogicalFlowTable {logicalFlows}
                          assessments={logicalFlowPrimaryAssessments}/>

        <h4 style="margin-top: 2em">Physical Flows</h4>
        <PhysicalFlowTable physicalFlows={filteredFlows}/>

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