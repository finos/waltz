<script>
    import {selectedLogicalFlow, selectedPhysicalFlow} from "./flow-details-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import SelectedLogicalFlowDetail from "./SelectedLogicalFlowDetail.svelte";
    import SelectedPhysicalFlowDetail from "./SelectedPhysicalFlowDetail.svelte";

    export let assessmentDefinitions = [];

    function clearSelected() {
        $selectedPhysicalFlow = null;
        $selectedLogicalFlow = null;
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
                <SelectedLogicalFlowDetail assessment-definitions={assessmentDefinitions}/>
            {/if}

            {#if $selectedPhysicalFlow}
                <hr>
                <SelectedPhysicalFlowDetail />
            {/if}
        </div>
    </div>
</div>

<style>

</style>