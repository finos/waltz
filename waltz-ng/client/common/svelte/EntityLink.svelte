<script>
    import ViewLink from "./ViewLink.svelte";
    import EntityLabel from "./EntityLabel.svelte";
    import {kindToViewState} from "../link-utils";
    import _ from "lodash";

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

    export let ref = null;
    export let isSecondaryLink = false;
    export let showIcon = true;

    let state = null

    $: {
        try {
            state = _.isEmpty(ref)
                ? null
                : kindToViewState(ref.kind);
        } catch(e){
            console.error(e, "bad ref?", ref);
        }
    }

</script>

{#if ref}
    <ViewLink {state} {isSecondaryLink}
              ctx={ref}>
        {#if $$slots.default}
            <slot/>
        {:else}
            <EntityLabel {ref} {showIcon}/>
        {/if}
    </ViewLink>
{/if}