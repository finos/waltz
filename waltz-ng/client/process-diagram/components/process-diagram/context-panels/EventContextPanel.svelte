<script>
    import {selectedObject} from "../diagram-store";
    import ConnectionsSubContextPanel from "./ConnectionsSubContextPanel.svelte";
</script>

<h4>&#8413; {$selectedObject.name}</h4>

<dl>
    <dt>Stereotype</dt>
    <dd>{$selectedObject.stereotype}</dd>
</dl>

<ConnectionsSubContextPanel />
