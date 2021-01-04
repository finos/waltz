<script>
    import ViewLink from "./ViewLink.svelte";
    import EntityLabel from "./EntityLabel.svelte";

    /**
     * Entity Link takes an entity ref
     * and generates a page link for the entity.
     * By default it will render an icon and the name of the
     * entity (if it's on the reference).
     *
     * If the default slot is provided then that will be used.
     *
     * @type {{id: number}}
     */


    export let ref = {};
</script>

<ViewLink state="main.app.view"
          ctx={ref}>
    {#if $$slots.default}
        <slot/>
    {:else}
        <EntityLabel {ref}/>
    {/if}
</ViewLink>
