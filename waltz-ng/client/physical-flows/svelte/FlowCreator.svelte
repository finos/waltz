<script>

    import Icon from "../../common/svelte/Icon.svelte";
    import EntitySearchSelector from "../../common/svelte/EntitySearchSelector.svelte";
    import {logicalFlowStore} from "../../svelte-stores/logical-flow-store";
    import toasts from "../../svelte-stores/toast-store";
    import {createEventDispatcher} from "svelte";
    import {Direction} from "./physical-flow-registration-utils";
    import EntityLabel from "../../common/svelte/EntityLabel.svelte";

    export let primaryEntityRef;
    export let direction;
    export let source;
    export let target;

    const dispatch = createEventDispatcher();

    function createNewLogical() {
        const command = {
            source,
            target
        }
        logicalFlowStore.addFlow(command)
            .then(r => {
                toasts.success(`Successfully created new logical flow from ${source.name} to ${target.name}`);
                dispatch("select", r.data);
            });
    }

    function onSelectSource(sourceEntity) {
        source = sourceEntity;
    }

    function onSelectTarget(targetEntity) {
        target = targetEntity;
    }

    function cancel() {
        dispatch("cancel");
    }


</script>


<div class="help-block">
    <Icon name="info-circle"/>
    Pick a <span>{direction === Direction.UPSTREAM ? "source" : "target"}</span> for the new flow
</div>

<form on:submit|preventDefault={createNewLogical}>

    <div class="form-group">
        <label for="source">
            Source
        </label>
        <div id="source">
            {#if source}
                <div>
                    <EntityLabel ref={source}/>
                    {#if direction === Direction.UPSTREAM}
                        <button class="btn btn-skinny"
                                on:click={() => source = null}>
                            <Icon name="times"/>
                            select a different source
                        </button>
                    {/if}
                </div>
            {:else}
                {#if direction === Direction.UPSTREAM}
                    <EntitySearchSelector on:select={(evt) => onSelectSource(evt.detail)}
                                          placeholder="Search for source"
                                          entityKinds={['APPLICATION', 'ACTOR']}>
                    </EntitySearchSelector>
                {/if}
            {/if}
        </div>
    </div>

    <div class="form-group">
        <label for="target">
            Target
        </label>
        <div id="target">
            {#if target}
                <div>
                    <EntityLabel ref={target}/>
                    {#if direction === Direction.DOWNSTREAM}
                        <button class="btn btn-skinny"
                                on:click={() => target = null}>
                            <Icon name="times"/>
                            select a different target
                        </button>
                    {/if}
                </div>
            {:else}
                <EntitySearchSelector on:select={(evt) => onSelectTarget(evt.detail)}
                                      placeholder="Search for target"
                                      entityKinds={['APPLICATION', 'ACTOR']}>
                </EntitySearchSelector>
            {/if}
        </div>
    </div>

    <button class="btn btn-success"
            disabled={!(source && target)}
            on:click={() => createNewLogical()}>
        Save
    </button>
    <button class="btn btn-skinny"
            on:click={() => cancel()}>
        Cancel
    </button>
</form>