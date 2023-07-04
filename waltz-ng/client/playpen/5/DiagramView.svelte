<script>

    import _ from "lodash";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import pageInfo from "../../svelte-stores/page-navigation-store";
    import {kindToViewState} from "../../common/link-utils";

    export let group;

    function goTo(data) {
        console.log({data});

        $pageInfo = {
            state: kindToViewState(data.kind),
            params: {
                kind: data.kind,
                id: data.id
            }
        };
    }

</script>

<div style="height: 100%; width: 100%">

    <div style="display: flex; width: 100%">

        <div style="flex: 1 1 80%">
            {#if group.props.showTitle}
                <div class="diagram-title clickable">
                    {#if group.data}
                        <button class="btn btn-plain"
                                on:click={() => goTo(group.data)}>
                            {group.title}
                        </button>
                    {:else}
                        {group.title}
                    {/if}
                </div>
            {/if}

            <div class={`diagram-container diagram-container-${group.props.flexDirection}`}>
                {#each _.orderBy(group.children, d => d.position) as child (child.id)}
                    <div class="group"
                         style={`flex: ${child.props.proportion} 1 ${_.floor(100 / (group.props.bucketSize + 1))}%;`}>
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
            </div>
        </div>

    </div>

</div>


<style type="text/scss">

    .diagram-container {

        display: flex;
        flex-wrap: wrap;
        justify-content: space-evenly;
        gap: 0.5em;

        border: 1px solid #000d79;
        background-color: #f6f7ff;

        height: fit-content;
        min-height: 5em;
    }

    .diagram-container.hovered {
        &:hover {
            background-color: lighten(#f6f7ff, 20%);
        }
    }

    .diagram-container-row {
        flex-direction: row;
        align-items: flex-start;
        align-content: flex-start;
    }

    .diagram-container-row > .group {
        //flex: 1 1 25%; /* when rows this sets the width*/
        height: fit-content;
        min-height: 5em;
    }

    .diagram-container-row > .item {
        //flex: 0 1 10em; /* when rows this sets the width*/
        height: fit-content;
        min-height: 5em;
    }

    .diagram-container-column {
        flex-direction: column;
        align-items: center;
        align-content: center;
        max-height: 60em;
    }

    .diagram-container-column > .group {
        flex: 1 1 45%; /* when columns this sets the height*/
        min-width: 10em;
    }

    .diagram-container-column > .item {
        flex: 1 1 5em; /* when columns this sets the height*/
        width: fit-content;
        min-width: 10em;
    }

    .item {
        border: 1px solid #000d79;
        background-color: #d7f4fa;
        margin: 0.5em;
        padding: 0.25em;
    }

    .group {
        margin: 0.5em;
    }

    .diagram-title {
        text-align: center;
        border: 1px solid #000d79;
        background-color: #000d79;
        font-weight: bolder;
        color: white;
        padding: 0 0.5em;

        &:hover {
            background-color: lighten(#000d79, 50%);
        }

        button {
            width: 100%;
            outline: none !important;
            color: white;
        }
    }

    .diagram-title.hovered {
        background-color: lighten(#000d79, 20%);
    }

</style>