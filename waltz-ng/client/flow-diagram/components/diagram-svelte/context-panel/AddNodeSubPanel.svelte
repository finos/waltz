<script>
    import _ from "lodash";
    import {toGraphNode} from "../../../flow-diagram-utils";
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import model from "../store/model";
    import {positions} from "../store/layout";

    function saveNode(e) {
        const node = toGraphNode(e.detail);

        model.addNode(node);
        positions.move({
            id: node.id,
            dx: _.random(-80, 80),
            dy: _.random(50, 80)
        });
    }

</script>

<div>
    <strong>Add a new node:</strong>
    <EntitySearchSelector on:select={saveNode}
                          placeholder="Search for source"
                          entityKinds={['APPLICATION', 'ACTOR']}>
    </EntitySearchSelector>
</div>

<style>
</style>