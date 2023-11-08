<script>

    import {flowClassificationStore} from "../../../../svelte-stores/flow-classification-store";
    import {dataTypeStore} from "../../../../svelte-stores/data-type-store";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import _ from "lodash";
    import {reduceToSelectedNodesOnly} from "../../../../common/hierarchy-utils";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import LogicalFlowTable from "./LogicalFlowTable.svelte";
    import {filteredAssessments, selectedLogicalFlow, selectedPhysicalFlow} from "./flow-details-store";
    import PhysicalFlowTable from "./PhysicalFlowTable.svelte";
    import {mkAssessmentFilters, mkFlowDetails} from "./flow-detail-utils";
    import SelectedFlowDetail from "./SelectedFlowDetail.svelte";

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

            allFlows = mkFlowDetails(flowView);
        }
    }


    const assessmentFilter = function(assessments, flowDetail) {
        return _.isEmpty(assessments)
            || _.some(assessments, r => _.some(flowDetail.assessmentRatings, d => _.isEqual(r, d)));
    }

    const logicalFlowFilter = function(selectedLogicalFlow, flowDetail) {
        console.log({selectedLogicalFlow, flowDetail})
        return _.isEmpty(selectedLogicalFlow)
            || flowDetail.logicalFlow.id === flowDetail.logicalFlow.id;
    }

    const physicalFlowFilter = function(selectedPhysicalFlow, flowDetail) {
        console.log({selectedPhysicalFlow, flowDetail})
        return _.isEmpty(selectedPhysicalFlow)
            || flowDetail.physicalFlow.id === flowDetail.physicalFlow.id;
    }

    function filterFlows(allFlows, filteredAssessments, selectedLogicalFlow, selectedPhysicalFlow) {
        return _
            .chain(allFlows)
            .filter(d => logicalFlowFilter(selectedLogicalFlow, d))
            .filter(d => physicalFlowFilter(selectedPhysicalFlow, d))
            .filter(d => assessmentFilter(filteredAssessments, d))
            .value();
    }

    $: filteredFlows = filterFlows(allFlows, $filteredAssessments, $selectedLogicalFlow, $selectedPhysicalFlow);

    $: logicalFlows = _
        .chain(filteredFlows)
        .map(d => _.pick(d, ["logicalFlow", "ratingsByDefId", "dataTypesForLogicalFlow", "assessmentRatings"]))
        .uniqBy(d => d.logicalFlow.id)
        .value();

    $: logicalFlowPrimaryAssessments = _.get(flowView, "primaryAssessmentDefinitions", []);

    $: console.log({filteredFlows, allFlows, logicalFlows});

    function selectRating(definitionId, ratingId) {

        const ratingInfo = {
            definitionId,
            ratingId
        };

        if (_.some($filteredAssessments, r => _.isEqual(r, ratingInfo))) {
            $filteredAssessments = _.filter($filteredAssessments, d => !_.isEqual(d, ratingInfo));
        } else {
            $filteredAssessments = _.concat($filteredAssessments, ratingInfo);
        }
    }

    function clearFiltersForDefinition(defnId) {
        $filteredAssessments = _.filter($filteredAssessments, d => d.definitionId !== defnId);
    }

    $: console.log({slf: $selectedLogicalFlow, spf: $selectedPhysicalFlow});

</script>





<div class="flow-detail-panel">
    <div class="flow-detail-table">

        {#if !_.isEmpty(assessmentFilters)}
            <details>
                <summary>
                    Filters
                </summary>

                <div class="help-block"
                     style="padding-top: 1em">Use the assessment ratings to filter the logical flows. Only ratings aligned to a flow can be filtered upon</div>
                <div style="display: flex; gap: 1em">
                    <div style="flex: 1 1 30%">
                        {#each assessmentFilters as assessment}
                            <table class="table table-condensed">
                                <thead>
                                <tr>
                                    <th>{assessment?.definition?.name}
                                        <span>
                                    <button class="btn btn-skinny"
                                            disabled={!_.some($filteredAssessments, d => d.definitionId === assessment?.definition.id)}
                                            on:click={() => clearFiltersForDefinition(assessment?.definition.id)}>
                                        Clear
                                    </button>
                                </span>
                                    </th>
                                </tr>
                                </thead>
                                <tbody>
                                {#each assessment?.ratings as rating}
                                    <tr class="clickable"
                                        class:selected={_.some($filteredAssessments, r => _.isEqual(r, { definitionId: assessment.definition.id, ratingId: rating.id}))}
                                        on:click={() => selectRating(assessment.definition.id, rating.id)}>
                                        <td>
                                            <RatingIndicatorCell {...rating}/>
                                        </td>
                                    </tr>
                                {/each}
                                </tbody>
                            </table>
                        {:else}
                            <NoData type="info">No flows have been given a rating for a primary assessment</NoData>
                        {/each}
                    </div>
                </div>
            </details>
        {/if}

        <LogicalFlowTable {logicalFlows}
                          assessments={logicalFlowPrimaryAssessments}/>

        <PhysicalFlowTable physicalFlows={filteredFlows}/>

    </div>
    {#if $selectedLogicalFlow || $selectedPhysicalFlow}
        <div class="flow-detail-context-panel">
            <SelectedFlowDetail/>
        </div>
    {/if}
</div>

<style>

    .selected {
        background-color: #fff;
    }

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