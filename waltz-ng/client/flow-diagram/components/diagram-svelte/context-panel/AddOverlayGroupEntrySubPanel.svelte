<script>
    import _ from "lodash";
    import GroupSelectorPanel from "./GroupSelectorPanel.svelte";
    import {createEventDispatcher} from "svelte";
    import {applicationStore} from "../../../../svelte-stores/application-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {determineFillAndSymbol} from "./group-utils";
    import {processor} from "../diagram-model-store";
    import overlay from "../store/overlay";

    export let group;
    export let alignments;
    export let overlays;

    let workingOverlay;
    let relatedAppIds = [];

    const dispatch = createEventDispatcher();

    function cancel() {
        workingOverlay = null;
    }

    function submit(){
        dispatch("cancel");
    }

    function selectOverlay(e) {
        workingOverlay = e.detail;
    }

    function getNewGroup(overlay) {
        return Object.assign(
            {},
            determineFillAndSymbol(overlays),
            {entityReference: overlay, kind: 'OVERLAY'});
    }

    function saveGroup(){
        const saveCmd = {
            command: "ADD_GROUP",
            payload: Object.assign({}, {group: newGroup, applicationIds: _.map(relatedAppIds, d => d.id)})
        }

        $processor([saveCmd]);

        overlay.addOverlay(Object.assign({},
            newGroup,
            {
                groupRef: group.id,
                applicationIds: _.map(relatedAppIds, d => d.id)
            }))
        cancel();
        submit();
    }

    $: relatedAppsCall = workingOverlay && applicationStore.findBySelector(mkSelectionOptions(workingOverlay));
    $: relatedAppIds = $relatedAppsCall?.data || [];
    $: newGroup =  getNewGroup(workingOverlay);

    $:console.log({overlayStore: $overlay})
</script>


<div>
    {#if _.isNil(workingOverlay) && alignments}
        <GroupSelectorPanel on:select={selectOverlay} {alignments}/>
    {:else}
        <div style="padding-bottom: 1em"><strong>{newGroup.entityReference.name}</strong> ({newGroup.symbol}/{newGroup.fill})</div>
        <button class="btn btn-skinny" on:click={() => saveGroup()}>Save</button>|
        <button class="btn btn-skinny" on:click={() => newGroup = getNewGroup(workingOverlay)}>Refresh Icon</button>|
        <button class="btn btn-skinny" on:click={cancel}>Cancel</button>
    {/if}
</div>

<style>
</style>