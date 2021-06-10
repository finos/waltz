<script>
    import {processor, store} from "../diagram-model-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {mkRef} from "../../../../common/entity-utils";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import _ from "lodash";
    import AddGroupSubPanel from "./AddGroupSubPanel.svelte";
    import GroupSelectorPanel from "./GroupSelectorPanel.svelte";
    import {measurableCategoryAlignmentViewStore} from "../../../../svelte-stores/measurable-category-alignment-view-store";
    import {flowDiagramOverlayGroupStore} from "../../../../svelte-stores/flow-diagram-overlay-group-store";

    let workingGroup;
    let categoryId = 9;
    export let diagramId;

    $: measurableAlignmentCall = measurableCategoryAlignmentViewStore
        .findAlignmentsByAppSelectorRoute(mkSelectionOptions(mkRef('FLOW_DIAGRAM', diagramId)));

    $: alignments = $measurableAlignmentCall.data;

    $: overlayGroupCall = flowDiagramOverlayGroupStore.findById(diagramId);
    $: overlays = $overlayGroupCall.data

    $: overlayGroups = $store.model.groups;

    $: console.log({alignments, overlays, overlayGroups});

    function selectGroup(e) {
        console.log("selecting from tree", e);
        workingGroup = e.detail;
    }

    function cancel() {
        workingGroup = null;
    }

    function removeGroup(group) {
        const command = {
            command: "REMOVE_GROUP",
            payload: group
        }

        $processor([command]);
    }

</script>

<div>
    <strong>Groups:</strong>
    {#if _.isEmpty(overlayGroups)}
        You have no groups selected; once added these can be used to group/filter applications.
    {:else}
    <ul>
        {#each overlayGroups as group}
            <li>
                <EntityLink ref={group.group.entityReference}/> ({group.group.symbol}/{group.group.fill})
                <button class="btn btn-skinny"
                        on:click={() => removeGroup(group)}>
                    <Icon name="trash"/>
                </button>
            </li>
        {/each}
    </ul>
    {/if}
</div>

{#if _.isNil(workingGroup) && alignments}
    <GroupSelectorPanel on:select={selectGroup} {alignments}/>
{:else}
    <AddGroupSubPanel  group={workingGroup}
                       existingGroups={overlayGroups}
                       on:cancel={cancel}/>
{/if}

<style>
    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    li {
        padding-top: 0;
    }
</style>