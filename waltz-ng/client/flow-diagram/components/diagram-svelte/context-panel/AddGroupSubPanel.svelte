<!--<script>-->
<!--    import {processor} from "../diagram-model-store";-->
<!--    import _ from "lodash";-->
<!--    import {applicationStore} from "../../../../svelte-stores/application-store";-->
<!--    import {mkSelectionOptions} from "../../../../common/selector-utils";-->
<!--    import {determineFillAndSymbol} from "./group-utils";-->
<!--    import {createEventDispatcher} from "svelte";-->

<!--    export let overlay;-->
<!--    export let existingOverlays;-->

<!--    const dispatch = createEventDispatcher();-->

<!--    let relatedAppIds = [];-->

<!--    $: relatedAppsCall = applicationStore.findBySelector(mkSelectionOptions(overlay));-->
<!--    $: relatedAppIds = $relatedAppsCall.data;-->

<!--    $: selector = mkSelectionOptions(overlay);-->

<!--    function getNewGroup() {-->
<!--        return Object.assign(-->
<!--            {},-->
<!--            determineFillAndSymbol(existingOverlays),-->
<!--            {entityReference: overlay});-->
<!--    }-->

<!--    $: newGroup =  getNewGroup();-->

<!--    function saveGroup(){-->
<!--        const saveCmd = {-->
<!--            command: "ADD_GROUP",-->
<!--            payload: Object.assign({}, {group: newGroup, applicationIds: _.map(relatedAppIds, d => d.id)})-->
<!--        }-->

<!--        $processor([saveCmd]);-->
<!--        cancel();-->
<!--        dispatch("submit");-->
<!--    }-->


<!--    function cancel(){-->
<!--        dispatch("cancel");-->
<!--    }-->



<!--</script>-->

<!--<span>-->
<!--    Adding: <strong>{newGroup.entityReference.name}</strong><p>({newGroup.symbol}/{newGroup.fill})</p>-->
<!--</span>-->

<!--<button class="btn btn-skinny" on:click={() => saveGroup()}>Save</button>|-->
<!--<button class="btn btn-skinny" on:click={() => newGroup = getNewGroup()}>Refresh Icon</button>|-->
<!--<button class="btn btn-skinny" on:click={cancel}>Cancel</button>-->

<!--<style>-->
<!--</style>-->