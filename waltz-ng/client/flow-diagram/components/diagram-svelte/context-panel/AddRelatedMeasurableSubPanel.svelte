<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import {createEventDispatcher} from "svelte";

    export let diagramId;
    export let measurables;

    let showSuggestedMeasurables = true;

    let dispatch = createEventDispatcher();

    function selectEntity(e) {
        dispatch("select", e);
    }

    function cancel(){
        dispatch("cancel");
    }

</script>


{#if showSuggestedMeasurables}
    <p class="help-block">
        The following viewpoints are suggestions based upon actual usage
        by applications in this diagram.  Select one to add an explicit
        association or
        <button class="btn btn-skinny" on:click={() => showSuggestedMeasurables = false}>
            search for a different measurable
        </button>.
    </p>
    <div class="waltz-scroll-region"
         style="height: 200px">
        <table class="table table-condensed small">
            <!-- IF NONE -->
            {#if _.isEmpty(measurables)}
                <tr>
                    <td colspan="2">No suggested viewpoints</td>
                </tr>
            {:else}
                <!-- LIST -->
                <tbody>
                {#each measurables as measurable}
                    <tr>
                        <td>
                            <div >{measurable.name}</div>
                            <div class="small text-muted">
                                {_.get(measurable.category, "name", "unknown")}
                            </div>
                        </td>
                        <td>
                            <button on:click={() => selectEntity(measurable)}
                                    class="clickable">
                                <Icon name="trash"/>Add
                            </button>
                        </td>
                    </tr>
                {/each}
                </tbody>
            {/if}
        </table>
    </div>
    {:else }
    <div class="form-group">
        <label for="measurable">Measurable:</label>
        <div id="measurable">
            <EntitySearchSelector on:select={e => console.log({e}) ||selectEntity(e.detail)}
                                  placeholder="Search for measurables"
                                  entityKinds={['MEASURABLE']}>
            </EntitySearchSelector>
        </div>
        <p class="help-block">
            Select the measurable by typing or
            <button class="btn btn-skinny"
                    on:click={() => showSuggestedMeasurables = true}>
                show suggested measurables
            </button>
        </p>
    </div>
{/if}

<button class="btn btn-skinny"
        on:click={cancel}>
    Cancel
</button>