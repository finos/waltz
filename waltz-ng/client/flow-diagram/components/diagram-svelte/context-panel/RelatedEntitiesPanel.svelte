<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {mkRef} from "../../../../common/entity-utils";
    import {flowDiagramEntityStore} from "../../../../svelte-stores/flow-diagram-entity-store";
    import {measurableStore} from "../../../../svelte-stores/measurables";
    import {measurableCategoryStore} from "../../../../svelte-stores/measurable-category-store";

    export let diagramId;

    const Modes = {
        VIEW: "VIEW",
        ADD_MEASURABLE: "ADD_MEASURABLE",
        ADD_CHANGE_INITIATIVE: "ADD_CHANGE_INITIATITVE",
    };

    $: entitiesCall = flowDiagramEntityStore
        .findByDiagramId(diagramId);

    $: relatedEntities = $entitiesCall.data;

    $: measurablesCall = measurableStore
        .findMeasurablesBySelector(mkSelectionOptions(mkRef('FLOW_DIAGRAM', diagramId)));

    $: measurables = $measurablesCall.data;

    $: measurableCategoryCall = measurableCategoryStore.findAll();
    $: categoriesById = _.keyBy($measurableCategoryCall.data, d => d.id);

    $: measurablesById =  _.keyBy(measurables, d => d.id);


    $: console.log({relatedEntities});

    $: relatedMeasurableIds = _
        .chain(relatedEntities)
        .filter(e => e.entityReference.kind === 'MEASURABLE')
        .map(e => e.entityReference.id)
        .value();

    $: associatedMeasurables = _
            .chain(relatedMeasurableIds)
            .map(aId => measurablesById[aId])
            .filter(m =>  !_.isNil(m))
            .sortBy(d => d?.name)
            .value();

    $: console.log({categoriesById, measurablesById, relatedMeasurableIds})

    $: suggestedMeasurables = _
        .chain(measurables)
        .reject(aId => _.includes(relatedMeasurableIds, aId))
        .sortBy(d => d?.name)
        .value();


    $: console.log({associatedMeasurables, suggestedMeasurables});


    let activeMode = Modes.VIEW;
    let associatedMeasurables = [];

    function addNode() {
        activeMode = Modes.ADD_NODE
    }


</script>

{#if activeMode === Modes.VIEW}
    <strong>Measurables:</strong>
    <table class="table table-condensed small">
        <!-- IF NONE -->
        {#if _.isEmpty(associatedMeasurables)}
        <tr>
            <td colspan="2">No associated viewpoints</td>
        </tr>
        {:else}
        <!-- LIST -->
        <tbody>
        {#each associatedMeasurables as measurable}
        <tr>
            <td>
                <div >{measurable.name}</div>
                <div class="small text-muted">
                    {measurable.categoryId}
                </div>
            </td>
            <td>
                <button on:click={() => console.log("remove")}
                   class="clickable">
                    <Icon name="trash"/>Remove
                </button>
            </td>
        </tr>
        {/each}
        </tbody>
        {/if}

        <!-- FOOTER-->
        <tfoot>
        <tr>
            <td colspan="2">
                <button class="btn btn-skinny"
                        on:click={() =>  console.log("add")}>
                    <Icon name="plus"/>Add
                </button>
            </td>
        </tr>
        </tfoot>

    </table>

    <strong>Change Initiatives:</strong>
    <button class="btn btn-skinny"
            on:click={() => addNode()}>
        Add node
    </button>
{:else if activeMode === Modes.ADD_MEASURABLE }
    <br>
    <button class="btn btn-skinny"
            on:click={() => activeMode = Modes.VIEW}>
        Cancel
    </button>
{/if}

<style>
</style>