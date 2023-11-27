<script>
    import {filters, selectedLogicalFlow, selectedPhysicalFlow} from "./flow-details-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import SelectedLogicalFlowDetail from "./SelectedLogicalFlowDetail.svelte";
    import SelectedPhysicalFlowDetail from "./SelectedPhysicalFlowDetail.svelte";
    import _ from "lodash";

    export let assessmentDefinitions = [];
    export let flowClassifications = [];

    function clearSelected() {
        $selectedPhysicalFlow = null;
        $selectedLogicalFlow = null;
        $filters = _.reject($filters, d => d.id === "SELECTED_LOGICAL")
    }

</script>


<div class="waltz-sticky-part">

    <div class="row">
        <div class="col-md-12">
            <button class="btn btn-skinny pull-right"
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
                <SelectedLogicalFlowDetail {flowClassifications}
                                           {assessmentDefinitions}/>
            {/if}

            {#if $selectedPhysicalFlow}
                <hr>
                <SelectedPhysicalFlowDetail {assessmentDefinitions}/>
            {/if}
        </div>
    </div>
</div>

<style>

</style>