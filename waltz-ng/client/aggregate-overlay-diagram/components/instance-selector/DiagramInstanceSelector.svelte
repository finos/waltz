<script>

    import {getContext} from "svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import _ from "lodash";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import InstanceCreatePanel from "./InstanceCreatePanel.svelte";
    import {aggregateOverlayDiagramInstanceStore} from "../../../svelte-stores/aggregate-overlay-diagram-instance-store";
    import {userStore} from "../../../svelte-stores/user-store";
    import systemRoles from "../../../user/system-roles";

    const Modes = {
        VIEW: "VIEW",
        CREATE: "CREATE"
    }

    export let primaryEntityRef;

    let selectedDiagram = getContext("selectedDiagram");
    let selectedInstance = getContext("selectedInstance");
    let instances = getContext("instances");


    let permissionsCall = userStore.load();
    $: permissions = $permissionsCall?.data;
    $: hasEditPermissions = _.includes(permissions?.roles, systemRoles.AGGREGATE_OVERLAY_DIAGRAM_EDITOR.key) || false;

    let activeMode = Modes.VIEW;
    let instancesCall;

    $: {
        if ($selectedDiagram) {
            instancesCall = aggregateOverlayDiagramInstanceStore.findByDiagramId($selectedDiagram.id);
        }
    }

    $: $instances = $instancesCall?.data || [];


</script>

{#if activeMode === Modes.VIEW}
    <h4>Selected: {$selectedDiagram?.name}</h4>
    {#if !_.isEmpty($instances)}
        <p>Select an instance from the list below to see callouts
            {#if hasEditPermissions}
                or
                <button class="btn btn-skinny"
                        on:click={() => activeMode = Modes.CREATE}>
                    create a new instance
                </button>
            {/if}
        </p>

        <table class="table table-condensed table-hover">
            <colgroup>
                <col width="40%"/>
                <col width="60%"/>
            </colgroup>
            <tbody>
            {#each $instances as instance}
                <tr>
                    <td>
                        <EntityLink ref={instance}/>
                    </td>
                    <td>
                        <span class="text-muted">
                        <LastEdited entity={instance}/>
                    </span>
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    {:else}
        <p>There are no instances of this diagram at this vantage point
            {#if hasEditPermissions}
                , would you like to
                <button class="btn btn-skinny"
                        on:click={() => activeMode = Modes.CREATE}>
                    create one
                </button>
                ?
            {/if}
        </p>
    {/if}
{:else if activeMode === Modes.CREATE}
    <InstanceCreatePanel {primaryEntityRef}
                         on:cancel={() => activeMode = Modes.VIEW}/>
{/if}
