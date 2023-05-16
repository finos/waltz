<script>
    import SortableList from "./SortableList.svelte";

    export let list = [];
    export let onReorder = (newList = []) => console.log("Default impl of onReorder", {newList});
</script>

<SortableList list={[...list]}
              on:reorder={(evt) => onReorder(evt.detail)}/>