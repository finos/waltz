<script>
    import {processor} from "../diagram-model-store";
    import _ from "lodash";
    import {applicationStore} from "../../../../svelte-stores/application-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {determineFillAndSymbol} from "./group-utils";
    import {createEventDispatcher} from "svelte";

    export let group;
    export let existingGroups;

    $: console.log({selector, group, existingGroups, newGroup});
    const dispatch = createEventDispatcher();

    let relatedAppIds = [];

    $: relatedAppsCall = applicationStore.findBySelector(mkSelectionOptions(group));
    $: relatedAppIds = $relatedAppsCall.data;

    $: selector = mkSelectionOptions(group);

    function getNewGroup() {
        return Object.assign(
            {},
            determineFillAndSymbol(existingGroups),
            {entityReference: group});
    }

    $: newGroup =  getNewGroup();

    function saveGroup(){
        console.log("selected", group);

        const saveCmd = {
            command: "ADD_GROUP",
            payload: Object.assign({}, {group: newGroup, applicationIds: _.map(relatedAppIds, d => d.id)})
        }

        console.log({saveCmd});
        $processor([saveCmd]);
        cancel();
    }


    function cancel(){
        dispatch("cancel");
    }


</script>

<span>
    Adding: <strong>{newGroup.entityReference.name}</strong><p>({newGroup.symbol}/{newGroup.fill})</p>
</span>

<button class="btn btn-skinny" on:click={() => saveGroup()}>Save</button>|
<button class="btn btn-skinny" on:click={() => newGroup = getNewGroup()}>Refresh Icon</button>|
<button class="btn btn-skinny" on:click={cancel}>Cancel</button>

<style>
</style>