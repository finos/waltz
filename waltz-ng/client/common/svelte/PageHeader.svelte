<script>
    import {truncateMiddle} from "../string-utils";
    import Icon from "./Icon.svelte";

    export let icon = "dot-circle-o";
    export let name;
    export let small = "";

    const pageHeaderDefaultOffset = 60;

    let pageY = 0;

    $: stickyVisible = pageY > pageHeaderDefaultOffset;

    $: pageTitle = name
        ? `${name} - Waltz`
        : "Waltz";

</script>


<svelte:head>
    <title>{pageTitle}</title>
</svelte:head>

<svelte:window bind:scrollY={pageY}/>

<div class="header">
    <div class="header-free no-overflow">

        <slot name="breadcrumbs"/>

        <h2>
            <Icon name={icon}/>

            <span>{truncateMiddle(name, 50)}</span>
            <small>{small}</small>

            <slot name="actions"/>
        </h2>
    </div>

    <div class="header-sticky"
         class:stickyVisible>
        <button class="btn-skinny"
                on:click={() => pageY = 0}>
            <h2 class="clickable">
                <Icon name={icon}/>
                <span>{name}</span>
                <small>{small}</small>
            </h2>
        </button>
    </div>

    {#if $$slots.summary}
    <div class="waltz-page-summary waltz-page-summary-attach">
        <slot name="summary"/>
    </div>
    {/if}
</div>


<style type="text/scss">
    @import "../../../style/_variables";

    .header {
    }

    .header-free {
        background-color: $waltz-section-background-color;
        padding: 6px 6px 6px 16px;
        border-radius: 3px;
        height: 60px;
        margin-left: $waltz-section-margin;
        margin-right: $waltz-section-margin;
        margin-bottom: 12px;
        border: 1px solid $waltz-border-color-lighter;
    }

    .header-free h2 {
        margin-top: 6px;
        margin-bottom: 4px;
        font-size: 22px
    }

    .header-sticky {
        width: 100%;
        height: 30px;
        overflow: hidden;
        position: fixed;
        top: 50px;
        left: auto;
        z-index: 999;
        border-bottom: 2px solid $waltz-border-color-lighter;
        opacity: 0;
        pointer-events: none;
    }

    .header-sticky h2 {
        margin: 4px;
        padding-top: 2px;
        padding-left: 36px;
        font-size: medium;
        display: inline-block;
    }

    .stickyVisible {
        opacity: 1;
        background-color: $waltz-section-background-color;
        cursor: pointer;
        pointer-events: all;
    }
</style>