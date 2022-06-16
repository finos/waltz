<script>
    import {createEventDispatcher} from "svelte";
    import {fade} from "svelte/transition";
    import Icon from "../../../common/svelte/Icon.svelte";

    const dispatch = createEventDispatcher();

    export let dismissible = true;

</script>


<article transition:fade>

    {#if dismissible}
        <div>
            <button class="close"
                    on:click={() => dispatch("dismiss")}>
                <Icon name="times" width="0.8em"/>
            </button>
        </div>
    {/if}

    <div class="text">
        <slot name="content"/>
    </div>

</article>

<style>
    article {
        padding: 0.75rem 1.5rem;
        border-radius: 0.2rem;
        align-items: center;
        margin: 0 auto 0.5rem auto;
        width: 100%;
        z-index: 1000;
    }

    .text {
        margin-left: 1rem;
        margin-top: 10px;
    }

    button {
        background: transparent;
        border: 0 none;
        padding: 0;
        margin: 0 0 0 auto;
        line-height: 1;
        font-size: 2rem;
    }
</style>
