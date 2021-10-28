<script>

    import {filterApplied} from "./flow-decorator-store";
    import AssessmentFilters from "./AssessmentFilters.svelte";
    import DefaultFilters from "./DefaultFilters.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    const Modes = {
        DEFAULT: "DEFAULT",
        ASSESSMENT: "ASSESSMENT"
    }

    let activeMode = Modes.DEFAULT;

</script>

{#if activeMode === Modes.DEFAULT}
    <div class="row row-no-gutters">
        <DefaultFilters style="display: inline-block" on:submit={() => activeMode = Modes.ASSESSMENT}/>
    </div>
{:else if activeMode === Modes.ASSESSMENT}
    <div class="row row-no-gutters">
        <AssessmentFilters on:submit={() => activeMode = Modes.DEFAULT}/>
    </div>
{/if}

{#if $filterApplied}
    <br>
    <div class="col-sm-12">
        <div class="warning-block text-muted">
        <span>
            <Icon class="warning-icon"
                  name="exclamation-triangle"/>
            There are filters applied to the flow diagram
        </span>
        </div>
    </div>
{/if}

<style type="text/scss">

  @import "../../../../style/_variables";

    .warning-block {
        background: #fffdfa;
        background: linear-gradient(90deg, #fffdfa 0%, rgba(255,255,255,1) 100%);
        padding: 15px;
        border-radius: 3px;
        box-shadow: 0 0 2px 0 hsla(0, 0%, 0%, 0.2);
        border-left: 4px solid $waltz-amber;
    }


</style>