<script>

    import _ from "lodash";
    import {FilterKinds} from "../../../../data-flow/components/svelte/flow-detail-tab/filters/filter-utils";
    import {filters} from "../data-type-decorator-section-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {getAssessmentViewFilters} from "../data-type-decorator-view-grid-utils";
    import AssessmentFilters from "./AssessmentFilters.svelte"
    import FlowClassificationFilters from "./FlowClassificationFilters.svelte";

    export let assessmentsView;
    export let classifications;

    $: assessmentFilters = getAssessmentViewFilters(assessmentsView);

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
            <Icon name="shield"/> Flow Classification
            {#if !_.isEmpty(_.get(classificationFilter, ["classifications"], []))}
                    <span style="color: darkorange"
                          title="Flows have been filtered by classification">
                        <Icon name="exclamation-circle"/>
                    </span>
            {/if}
        </summary>
        <FlowClassificationFilters {classifications} filters={filters}/>
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
        <AssessmentFilters {assessmentFilters} filters={filters}/>
    </details>
</details>


<style>
    .filter-set {
        background-color: #fafafa;
    }
</style>