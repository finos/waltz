<script>
    import {processor} from "../diagram-model-store";
    import _ from "lodash";
    import {toGraphId} from "../../../flow-diagram-utils";
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";

    function saveNode(e) {
        const addCmd = {
            command: 'ADD_NODE',
            payload: e.detail
        };
        const dx = _.random(-80, 80);
        const dy = _.random(50, 80);

        const moveCmd = {
            command: 'MOVE',
            payload: {
                id: toGraphId(e.detail),
                dx,
                dy
            }
        };
        $processor([addCmd, moveCmd]);
    }

</script>

<div>
    <h4>Add a new node:</h4>
    <EntitySearchSelector on:select={saveNode}
                          placeholder="Search for source"
                          entityKinds={['APPLICATION', 'ACTOR']}>
    </EntitySearchSelector>
</div>

<style>
</style>