<script>
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";

    export let leader = null;
    export let scheme = "secondary";
</script>

<div class="navaid-leader"
     class:primary={scheme === 'primary'}
     class:secondary={scheme === 'secondary'}>
    <div class="banner">
        <div>
            <strong>{leader?.title}</strong>
        </div>
        <div>
            {#if leader.person}
                <EntityLabel ref={leader.person}
                             showIcon={false}/>
            {:else}
                <i>tbc</i>
            {/if}

        </div>
    </div>
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

