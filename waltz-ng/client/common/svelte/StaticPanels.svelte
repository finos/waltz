<script>

    import StaticPanel from "./StaticPanel.svelte";
    import {staticPanelStore} from "../../svelte-stores/static-panel-store";

    export let key;
    export let showTitle = true;
    export let showIcon = true;

    let staticPanelsCall;

    $: {
        if(key) {
            staticPanelsCall = staticPanelStore.findByGroupKey(key);
        }
    }

    $: staticPanels = $staticPanelsCall?.data || [];

</script>

{#each staticPanels as panel}
    <StaticPanel {showTitle} {showIcon} {panel}/>
{/each}
