<script>
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";
    import {RenderModes, renderModeStore} from "./builderStore";

    export let leader = null;
    export let scheme = "secondary";

    $: wrapperElem = $renderModeStore == RenderModes.DEV ? "span" : "a";

</script>

<div class="navaid-leader"
     class:primary={scheme === 'primary'}
     class:secondary={scheme === 'secondary'}>
    {#if leader?.person}
        <svelte:element this={wrapperElem}
                        href="person/id/{leader.person.id}">
            <div class="banner">
                <div>
                    <strong>{leader?.title}</strong>
                </div>
                <div>
                    <EntityLabel ref={leader.person}
                                 showIcon={false}/>
                </div>
            </div>
        </svelte:element>
    {:else}
        <div class="banner">
            <div>
                {leader?.title || "?"}
            </div>
        </div>
    {/if}
</div>
<style>
    .navaid-leader {
        text-align: center;
    }

    .navaid-leader .banner {
        padding: 0.5em;
    }

    .navaid-leader.secondary .banner {
        background-color: #e5e9fb;
        color: #031452;
    }

    .navaid-leader.primary .banner {
        background-color: #1b3497;
        color: #eee;
    }
</style>

