<script>

    import {getContext} from "svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import _ from "lodash";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import InstanceCreatePanel from "./InstanceCreatePanel.svelte";
    import {aggregateOverlayDiagramInstanceStore} from "../../../svelte-stores/aggregate-overlay-diagram-instance-store";

    const Modes = {
        VIEW: "VIEW",
        CREATE: "CREATE"
    }

    export let primaryEntityRef;

    let selectedDiagram = getContext("selectedDiagram");
    let selectedInstance = getContext("selectedInstance");
    let instances = getContext("instances");

    let activeMode = Modes.VIEW;
    let instancesCall;

    $: {
        if ($selectedDiagram) {
            instancesCall = aggregateOverlayDiagramInstanceStore.findByDiagramId($selectedDiagram.id);
        }
    }

    $: $instances = $instancesCall?.data || [];

    $: console.log({instnaces: $instances})

</script>

{#if activeMode === Modes.VIEW}
    <h4>Selected: {$selectedDiagram?.name}</h4>
    {#if !_.isEmpty($instances)}
        <p>Select an instance from the list below to see callouts or
            <button class="btn btn-skinny"
                    on:click={() => activeMode = Modes.CREATE}>
                create a new instance
            </button>
        </p>
        <ul>
            {#each $instances as instance}
                <li>
                    <EntityLink ref={instance}/>
                    <span class="text-muted">
                        <LastEdited entity={instance}/>
                    </span>
                </li>
            {/each}
        </ul>
    {:else}
        <p>There are no instances of this diagram at this vantage point, would you like to
            <button class="btn btn-skinny"
                    on:click={() => activeMode = Modes.CREATE}>
                create one
            </button>
            ?
        </p>
    {/if}
{:else if activeMode === Modes.CREATE}
    <InstanceCreatePanel {primaryEntityRef}
                         on:cancel={() => activeMode = Modes.VIEW}/>
{/if}

<style>
    ul {
        padding: 0.2em 0 0 0;
        margin: 0 0 0 0;
        list-style: none;
    }

    li {
        padding-top: 0.2em;
    }

</style>