<script>
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";

    import {mode, Modes, selectedClassificationRule} from "./editingFlowClassificationRulesState";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";


    export let doCancel;
    export let doDelete;
    export let canEdit = false;

    const classificationsCall = flowClassificationStore.findAll();

    $: classificationsById = _.keyBy($classificationsCall.data, d => d.id);

    let deletePromise;
    let selected = Object.assign({}, $selectedClassificationRule);

    function onCancelDetailView() {
        doCancel();
    }

    function editRule() {
        $mode = Modes.EDIT;
    }

    function deleteRule() {
        deletePromise = doDelete($selectedClassificationRule.id);
    }

    $: ref = {
        id: $selectedClassificationRule?.id,
        kind: "FLOW_CLASSIFICATION_RULE"
    };

</script>


{#if $mode === Modes.DETAIL}
    <div data-testid="source">
        <strong>Source:</strong>
        <EntityLink ref={$selectedClassificationRule.subjectReference}/>
    </div>
    <p class="text-muted">The source application or actor for this rule</p>


    <div data-testid="data-type">
        <strong>Data Type:</strong>
        <EntityLink ref={$selectedClassificationRule.dataType}/>
    </div>
    <p class="text-muted">The data type this application / actor will provide a flow classification for</p>

    <div data-testid="scope">
        <strong>Scope:</strong>
        <EntityLink ref={$selectedClassificationRule?.vantagePointReference}/>
    </div>
    <p class="text-muted">The selector for which this classification rule will apply to</p>

    <div data-testid="classification">
        <strong>Classification:</strong>
        <div class="rating-indicator-block"
             style="background-color: {_.get(selected, 'classification.color', '#ccc')}">
        </div>
        <span>{_.get(selected, 'classification.name', '-')}</span>
        <p class="text-muted">{_.get(selected, 'classification.description', '-')}</p>
    </div>

    <div data-testid="notes">
        <strong>Notes:</strong>
        <span>{selected?.description || "None provided"}</span>
        <p class="text-muted">Additional notes</p>
    </div>
{/if}

{#if canEdit && !$selectedClassificationRule?.isReadonly}
<button class="btn btn-success"
        on:click={editRule}>
    Edit
</button>
<button class="btn btn-danger"
        on:click={deleteRule}>
    Delete
</button>
{/if}

<EntityLink {ref}>
    View Page
</EntityLink>
&nbsp;
|
<button class="btn-link"
        on:click={onCancelDetailView}>
    Cancel
</button>


{#if deletePromise}
    {#await deletePromise}
        Deleting...
    {:then r}
        Deleted!
    {:catch e}
            <div class="alert alert-warning">
                Failed to delete flow classification rule. Reason: {e.data.message}
                <button class="btn-link"
                        on:click={() => {
                            deletePromise = null}}>
                    <Icon name="check"/>
                    Okay
                </button>
            </div>
    {/await}
{/if}



<style>
    .rating-indicator-block {
        display: inline-block;
        width: 1em;
        height: 1.1em;
        border: 1px solid #aaa;
        border-radius: 2px;
        position: relative;
        top: 2px;
    }
</style>