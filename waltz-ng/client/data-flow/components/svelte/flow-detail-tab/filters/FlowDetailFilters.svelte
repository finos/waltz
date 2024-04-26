<script>
import _ from "lodash";
import {filters} from "../flow-details-store";
import {FilterKinds} from "./filter-utils";
import {Directions} from "../flow-detail-utils";
import Icon from "../../../../../common/svelte/Icon.svelte";
import AssessmentFilters from "./AssessmentFilters.svelte";
import InboundOutboundFilters from "./InboundOutboundFilters.svelte";
import PhysicalFlowAttributeFilters from "./PhysicalFlowAttributeFilters.svelte";
import FlowClassificationFilters from "./FlowClassificationFilters.svelte";
import SourceTargetKindFilters from "./SourceTargetKindFilters.svelte";

export let dataTypes = [];
export let assessmentFilters = [];
export let physicalFlows = [];
export let flowClassifications = [];


$: classificationFilter = _.find($filters, d => d.kind === FilterKinds.FLOW_CLASSIFICATION);
$: directionFilter = _.find($filters, d => d.kind === FilterKinds.DIRECTION);

</script>

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
            {#if !_.isEqual(_.get(directionFilter, ["direction"], Directions.ALL), Directions.ALL)}
                <span style="color: darkorange"
                      title="Flows have been filtered by direction">
                    <Icon name="exclamation-circle"/>
                </span>
            {/if}
        </summary>
        <InboundOutboundFilters/>
    </details>

    <details class="filter-set" style="margin-top: 1em">
        <summary>
            <Icon name="shield"/> Flow Classification
            {#if !_.isEmpty(_.get(classificationFilter, ["classifications"], []))}
                <span style="color: darkorange"
                      title="Flows have been filtered by classification">
                    <Icon name="exclamation-circle"/>
                </span>
            {/if}
        </summary>
        <FlowClassificationFilters {flowClassifications}/>
    </details>

    <details class="filter-set">
        <summary>
            <Icon name="qrcode"/> Source / Target Types
            {#if _.some($filters, d => d.kind === FilterKinds.NODE_KIND)}
                <span style="color: darkorange"
                      title="Source and/or Target type filters have been applied">
                    <Icon name="exclamation-circle"/>
                </span>
            {/if}
        </summary>
        <SourceTargetKindFilters {dataTypes}/>
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

<style>
    .filter-set {
        background-color: #fafafa;
    }
</style>