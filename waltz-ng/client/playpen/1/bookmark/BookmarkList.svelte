<script>
    import Icon from "./Icon.svelte";
    import BookmarkListItem from "./BookmarkListItem.svelte";
    import {CORE_API} from "../../../common/services/core-api-utils";
    import {nestEnums} from "./enum-utils";
    import {mkBookmarkKinds, nestBookmarks} from "./bookmark-utils";

    export let serviceBroker;
    export let primaryEntityRef;

    let nestedEnums = {};
    let bookmarkKinds = {};
    let origBookmarkGroups = [];
    let bookmarkGroups = [];

    let bookmarksPromise;
    let selectedKind;

    const load = async (ref, force = false) => {
        const er = await serviceBroker
                .loadAppData(CORE_API.EnumValueStore.findAll);

        const br = await serviceBroker
                .loadViewData(CORE_API.BookmarkStore.findByParent, [ref], { force });

        return Promise
                .all([br, er])
                .then(() => {
                    nestedEnums = nestEnums(er.data);
                    origBookmarkGroups = nestBookmarks(nestedEnums, br.data);
                    bookmarkKinds = mkBookmarkKinds(nestedEnums, br.data);
                    bookmarkGroups = origBookmarkGroups;
                })
                .catch(e => console.error(e));
    };

    function bookmarkKindSelected(bookmarkKind) {
        if (selectedKind === bookmarkKind) {
            selectedKind = null;
            bookmarkGroups = origBookmarkGroups;
        } else {
            selectedKind = bookmarkKind;
            bookmarkGroups = _.filter(origBookmarkGroups, g => g.key === bookmarkKind.key);
        }
    }

    $: bookmarksPromise = load(primaryEntityRef);

</script>


<style>
    .bookmarks li {
        border-bottom: 1px solid #eee;
        padding-bottom: 4px;
    }

    .bookmarks li:last-child {
        border-bottom: none;
    }
</style>


<div class="row">

    <div class="col-sm-4">
        <ul class="list-group small">
                {#each bookmarkKinds as bookmarkKind}
                    <li class="list-group-item"
                        class:list-group-item-success={selectedKind === bookmarkKind}
                        class:clickable={bookmarkKind.count > 0}
                        class:text-muted={bookmarkKind.count === 0}
                        on:click={() => bookmarkKind.count > 0 && bookmarkKindSelected(bookmarkKind)}>
                        <Icon name={bookmarkKind.icon}/>
                        {bookmarkKind.name}

                        {#if selectedKind === bookmarkKind}
                            <span class="pull-right">
                                <Icon name="close"/>
                            </span>
                        {/if}
                    </li>
                {/each}
        </ul>
    </div>


    <div class="col-sm-8">
        {#await bookmarksPromise}
            Loading
        {:then result}
            <ul class="list-unstyled bookmarks">
                {#each bookmarkGroups as group, idx}
                    {#each group.value as bookmark}
                        <li>
                            <BookmarkListItem {bookmark}/>
                        </li>
                    {/each}
                {/each}
            </ul>
        {/await}

    </div>

</div>
