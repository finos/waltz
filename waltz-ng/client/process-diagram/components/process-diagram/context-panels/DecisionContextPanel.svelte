<script>
    import {selectedObject} from "../diagram-store";
    import ConnectionsSubContextPanel from "./ConnectionsSubContextPanel.svelte";
</script>

<h4>&#8415; {$selectedObject.name || 'Decision'}</h4>

<ConnectionsSubContextPanel />