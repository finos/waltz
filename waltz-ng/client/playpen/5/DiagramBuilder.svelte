<script>

    import DiagramGroup from "./DiagramGroup.svelte";
    import {DefaultProps} from "./diagram-builder-store";
    import DiagramControls from "./DiagramControls.svelte";


    let items = [];

    for (let i = 1; i < 10; i++) {
        const newItem = {title: "Item " + i.toString(), items:[], props: DefaultProps,  id: i, parentId: 0}
        items.push(newItem);
    }

    let group = {
        id: 0,
        parentId: null,
        title: "Diagram 1",
        items,
        props: DefaultProps
    }


</script>


<h1>Hello there!</h1>

<DiagramControls/>

<div style="height: 30em; outline: #0f0746 solid 1px">
    <DiagramGroup {group}>
    </DiagramGroup>
</div>

<div>
</div>


<style>

</style>