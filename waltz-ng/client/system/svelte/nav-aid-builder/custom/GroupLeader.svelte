<script>
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";

    export let leader = null;
    export let scheme = "secondary";

    let color = "#eee";
    let backgroundColor = '#1b3497';

    $: color = scheme === 'primary'
        ? "#eee"
        : "#031452";

    $: backgroundColor = scheme === 'primary'
        ? "#1b3497"
        : "#e5e9fb";

</script>

<div style="text-align: center"
     class:primary={scheme === 'primary'}
     class:secondary={scheme === 'secondary'}>
    <div style="padding:0.5em"
         style:background-color={backgroundColor}
         style:color>
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

