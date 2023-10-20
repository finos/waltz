<script>
    import CostKindPicker from "../../../../../common/svelte/entity-pickers/CostKindPicker.svelte";
    import _ from "lodash";
    import {diagramService} from "../../entity-diagram-store";
    import {entity} from "../../../../../common/services/enums/entity";

    let selectedDefinition;

    let selectedCostKinds = [];

    const {updateOverlayParameters} = diagramService;

    const Modes = {
        COST_KIND_PICKER: "COST_KIND_PICKER",
        SUMMARY: "SUMMARY"
    }

    let activeMode = Modes.COST_KIND_PICKER;

    function onSelect() {

        const selectedCostKindIds = _.map(selectedCostKinds, d => d.id);

        const params = {
            costKindIds: selectedCostKindIds,
        }

        updateOverlayParameters(params);

        activeMode = Modes.SUMMARY;
    }


    function onSelectCostKind(costKind) {
        selectedCostKinds = _.concat(selectedCostKinds, costKind)
    }

    $: selectedCostKindIds = _.map(selectedCostKinds, d => d.id);

    $: incompleteSelection = _.isEmpty(selectedCostKinds);

</script>


{#if activeMode === Modes.COST_KIND_PICKER}
    <div class="help-block">
        Select one or more the cost kinds to apply the allocations to.
    </div>
    <CostKindPicker onSelect={onSelectCostKind}
                    subjectKind={entity.MEASURABLE_RATING.key}
                    selectionFilter={ck => !_.includes(selectedCostKindIds, ck.id)}/>
    {#if !_.isEmpty(selectedCostKindIds)}
        <div>
            Selected Cost Kinds:
            <ul>
                {#each selectedCostKinds as kind}
                    <li>
                        {kind.name}
                    </li>
                {/each}
            </ul>
        </div>
    {/if}
    <span>
         <button class="btn btn-success"
                 on:click={onSelect}
                 disabled={incompleteSelection}>
            Load data
        </button>
    </span>
{:else if activeMode === Modes.SUMMARY}
    <div>
        Selected Cost Kinds:
        <ul>
            {#each selectedCostKinds as kind}
                <li>
                    {kind.name}
                </li>
            {/each}
        </ul>
    </div>
{/if}
