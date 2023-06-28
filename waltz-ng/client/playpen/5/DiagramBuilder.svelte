<script>

    import _ from "lodash";

    const FlexDirections = {
        COLUMN: "column",
        ROW: "row"
    }


    let items = [];

    for (let i = 1; i < 10; i++) {
        const newItem = {name: "Item " + i.toString()}
        items.push(newItem);
    }

    let props = {
        itemHeight: 5,
        itemWidth: 10,
        flexDirection: FlexDirections.ROW
    }

    function toggleFlexDirection() {
        props.flexDirection = props.flexDirection === FlexDirections.ROW
            ? FlexDirections.COLUMN
            : FlexDirections.ROW
    }

    function addItem() {
        const itemNumber = items.length + 1;
        const newItem = {name: "Item " + itemNumber.toString()}
        items = _.concat(items, newItem);
    }

    function addGroup() {
    }

    $: console.log({items});

    let groups = [{title: "title", items}];


</script>


<h1>Hello there!</h1>

<div class="diagram-control-panel">
    <button class="btn btn-default"
            on:click={toggleFlexDirection}>
        Toggle alignment
    </button>
    <button class="btn btn-default"
            on:click={addItem}>
        Add item
    </button>
    <button class="btn btn-default"
            on:click={addGroup}>
        Add group
    </button>
</div>

<div style="height: 60em; outline: #0f0746 solid 1px">
    <div class="diagram-title">Title</div>
    <div class={`diagram-container diagram-container-${props.flexDirection}`}>
        {#each items as item}
            <div class="item"
                 style={`height: ${props.itemHeight}em; width: ${props.itemWidth}em`}>
                {item.name}
            </div>
        {/each}
    </div>
</div>


<style>

    .diagram-control-panel {
        padding: 5em 0;
    }

    .diagram-container {

        display: flex;
        flex-wrap: wrap;
        justify-content: space-evenly;
        align-content: center;

        border: 1px solid red;
        background-color: antiquewhite;

        height: 100%;
        width: 100%
    }

    .diagram-container-row {
        flex-direction: row;
    }

    .diagram-container-column {
        flex-direction: column;
    }

    .item {
        border: 1px solid blue;
        background-color: #d7f4fa;
        margin: 0.5em;
    }

    .diagram-title {
        text-align: center;
        background-color: #0b23ea;
        font-weight: bolder;
        color: white;
    }

</style>