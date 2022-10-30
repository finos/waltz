<script>
    import Icon from '../../common/svelte/Icon.svelte';
    import {createEventDispatcher} from "svelte";

    export let bookmarkKinds = [];

    let selected = null;

    const dispatch = createEventDispatcher();

    function eq(k1, k2) {
        if (_.isNil(k1) || _.isNil(k2)) {
            return false;
        }
        return k1 === k2 || k1.key === k2.key;
    }


    function bookmarkKindSelected(k) {
        selected = eq(selected, k)
            ? null
            : k;

        dispatch("kindSelect", { kind: selected });
    }
</script>


<ul class="list-group">
    {#each bookmarkKinds as bookmarkKind}
        <li class="list-group-item"
            class:selected={eq(selected, bookmarkKind)}
            class:clickable={bookmarkKind.count > 0}
            class:text-muted={bookmarkKind.count === 0}
            on:click,keydown={() => bookmarkKind.count > 0 && bookmarkKindSelected(bookmarkKind)}>

            <Icon name={bookmarkKind.icon}/>

            {bookmarkKind.name}

            {#if eq(selected, bookmarkKind)}
                <span class="pull-right">
                    <Icon name="close"/>
                </span>
            {/if}
        </li>
    {/each}
</ul>


<style>
    .selected {
        background-color: #e2ffd9;
    }

    .clickable > :global(.icon) {
        color: #5BB65D;
    }
</style>