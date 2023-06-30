<div style="height: 100%; width: 100%">
    {#if !_.isEmpty(group.title)}
        <div class="diagram-title">
            {group.title}
        </div>
    {/if}

    <div class={`diagram-container diagram-container-${group.props.flexDirection}`}>
        {#each group.children as child}
            <div class="group">
                <svelte:self group={child}>
                </svelte:self>
            </div>
        {:else}
            {#if group.data}
                <div class="item">
                    <EntityLink ref={group.data}/>
                </div>
            {/if}
        {/each}
        {#each group.items as item}
            <div class="item">
                <div class="diagram-title">
                    {item.title}
                </div>
                <div>
                    <EntityLink ref={item.data}/>
                </div>
            </div>
        {/each}
    </div>
</div>

<script>

    import _ from "lodash";
    import EntityLink from "../../common/svelte/EntityLink.svelte";

    export let group;

</script>


<style>

    .diagram-container {

        display: flex;
        flex-wrap: wrap;
        justify-content: space-evenly;
        align-content: flex-start;
        gap: 0.5em;

        border: 1px solid red;
        background-color: antiquewhite;

        height: fit-content;
        min-height: 5em;
    }

    .diagram-container-row {
        flex-direction: row;
        align-items: flex-start;
    }

    .diagram-container-row > .group {
        flex: 1 1 25%; /* when rows this sets the width*/
        height: fit-content;
        min-height: 5em;
    }

    .diagram-container-row > .item {
        flex: 0 1 10em; /* when rows this sets the width*/
        height: fit-content;
        min-height: 5em;
    }

    .diagram-container-column {
        flex-direction: column;
        align-items: center;
    }

    .diagram-container-column > .group {
        flex: 1 1 45%; /* when columns this sets the height*/
        width: fit-content;
        min-width: 10em;
    }

    .diagram-container-column > .item {
        flex: 1 1 5em; /* when columns this sets the height*/
        width: fit-content;
        min-width: 10em;
    }

    .item {
        border: 1px solid blue;
        background-color: #d7f4fa;
        margin: 0.5em;
        padding: 0.25em;
    }

    .group {
        border: 1px solid purple;
        background-color: #eecfff;
        margin: 0.5em;
    }

    .diagram-title {
        text-align: center;
        background-color: #0b23ea;
        font-weight: bolder;
        color: white;
    }

</style>