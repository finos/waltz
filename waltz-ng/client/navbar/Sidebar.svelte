<script>
    import store from "./sidebar-store";


    $: console.log({sections: $store})
</script>
<h1>Svelte sidebar</h1>

<ul>
{#each $store as section}
    <li>{section.name}</li>
{/each}
</ul>