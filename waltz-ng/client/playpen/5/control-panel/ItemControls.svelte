<script>

    import {items, selectedGroup} from "../diagram-builder-store";
    import _ from "lodash";

    function removeItem(id) {
        $items = _.reject($items, d => d.id === id);
    }

</script>


<h4>Item</h4>


<ul>
    {#each $selectedGroup.items as item}
        <li>{item.title}</li>
    {/each}
</ul>
