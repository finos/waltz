<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import {flowDirection, flowDirections, layoutDirection, layoutDirections} from "./flow-decorator-store";
    import Toggle from "../../../common/svelte/Toggle.svelte";
    import _ from "lodash";

    export let parentEntity;

    let selectedTab = "context";

    function toggleDirection() {
        $layoutDirection = $layoutDirection === layoutDirections.categoryToClient
            ? layoutDirections.clientToCategory
            : layoutDirections.categoryToClient
    }

</script>

<div class="help-block">
    <Icon name="info-circle"/>
    Select a datatype or entity on the diagram for further information. Click the plus icon
    to drill down to child data types. Filters in the next tab can be used to simplify the view.
</div>
<div>
    You are currently viewing
    <strong>{_.toLower($flowDirection)}</strong>
    flows
    {$flowDirection === flowDirections.INBOUND ?  "to" : "from"}
    {parentEntity.name || "unknown entity" }.
</div>
<br>
<div>
    <Toggle labelOn="Show inbound flows"
            labelOff="Show outbound flows"
            state={$layoutDirection === layoutDirections.categoryToClient}
            onToggle={() => toggleDirection()}/>
</div>