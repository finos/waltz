<script>
    import BookmarkCategoryMenu from "./BookmarkCategoryMenu.svelte";
    import BookmarkEditor from "./BookmarkEditor.svelte";
    import BookmarkTable from "./BookmarkTable.svelte";
    import BookmarkRemovalConfirmation from "./BookmarkRemovalConfirmation.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import NoData from "../../common/svelte/NoData.svelte";
    import SearchInput from "../../common/svelte/SearchInput.svelte";
    import {nestEnums} from "../../common/svelte/enum-utils";
    import {filterBookmarks, mkBookmarkKinds, nestBookmarks} from "./bookmark-utils";
    import {bookmarkStore} from "../../svelte-stores/bookmark-store";
    import {enumValueStore} from "../../svelte-stores/enum-value-store";
    import {userStore} from "../../svelte-stores/user-store";
    import roles from "../../user/system-roles";

    import _ from "lodash";
    import {mkOverallStatus} from "../../svelte-stores/store-utils";
    import LoadingPlaceholder from "../../common/svelte/LoadingPlaceholder.svelte";

    export let primaryEntityRef = null;

    let nestedEnums = {};
    let bookmarkKinds = {};
    let bookmarkGroups = [];

    let removalCandidate = null;
    let editCandidate = null;

    let selectedKind = null;
    let qry = "";
    let actions = [];
    let status = null;

    const editAction = {
        icon: "pencil",
        name: "Edit",
        handleAction: (d) => editCandidate = d
    };

    const removeAction = {
        icon: "trash",
        name: "Remove",
        handleAction: (d) => removalCandidate = d
    };

    function onKindSelect(e) {
        selectedKind = e.detail.kind;
    }

    function doRemove() {
        return bookmarkStore
            .remove(removalCandidate)
            .then(() => removalCandidate = null);
    }

    function doSave(bookmark) {
        return bookmarkStore
            .save(bookmark)
            .then(() => editCandidate = null);
    }

    function onCreate() {
        editCandidate = {
            bookmarkKind: _.get(selectedKind, "key", "DOCUMENTATION"),
            parent: primaryEntityRef,
            lastUpdatedBy: "ignored, server will set"
        };
    }

    let bookmarksCall = bookmarkStore.load(primaryEntityRef);
    let userCall = userStore.load();
    let enumsCall = enumValueStore.load();

    $: nestedEnums = nestEnums($enumsCall.data);
    $: bookmarkGroups = nestBookmarks(
        nestedEnums,
        filterBookmarks($bookmarksCall.data, selectedKind, qry));
    $: bookmarkKinds = mkBookmarkKinds(
        nestedEnums,
        $bookmarksCall.data);
    $: actions = _.includes($userCall.data?.roles, roles.BOOKMARK_EDITOR.key)
        ? [editAction, removeAction]
        : [];

    $: status = mkOverallStatus([$bookmarksCall])
</script>

{#if (status.hasErrors)}
    <h1>Error</h1>
    <pre>{JSON.stringify(status.errors, "", 2)}</pre>
{:else if (status.isLoading)}
    <LoadingPlaceholder>
        Loading bookmarks...
    </LoadingPlaceholder>
{:else}
    <div class="row row-mini-gutters">
        <div class="col-sm-3">
            <BookmarkCategoryMenu on:kindSelect={onKindSelect}
                                  bookmarkKinds={bookmarkKinds} />
        </div>

        <div class="col-sm-9">

            {#if removalCandidate}
                <BookmarkRemovalConfirmation bookmark={removalCandidate}
                                             {doRemove}
                                             doCancel={()=> removalCandidate = null}>
                </BookmarkRemovalConfirmation>
            {:else if editCandidate}
                <BookmarkEditor bookmark={editCandidate}
                                kinds={bookmarkKinds}
                                {doSave}
                                doCancel={()=> editCandidate = null} >
                </BookmarkEditor>
            {:else}
                {#if $bookmarksCall.data.length > 5}
                    <SearchInput bind:value={qry}
                                 placeholder="Search bookmarks..."/>
                    <br>
                {/if}
                {#if _.isEmpty(bookmarkGroups)}
                    <NoData>
                        No bookmarks
                        {#if actions.length > 0}
                            <div style="padding-top: 1em;">
                                <button class="btn btn-sm btn-default"
                                        on:click={()=> onCreate()}>
                                    <Icon name="plus"/>
                                    Add bookmark
                                </button>
                            </div>
                        {/if}
                    </NoData>

                {:else}
                    <BookmarkTable {bookmarkGroups} {actions}>
                        <tfoot slot="footer">
                        {#if actions.length > 0}
                            <tr>
                                <td colspan="3">
                                    <button class="btn btn-link"
                                            on:click={()=> onCreate()}>
                                        <Icon name="plus"/>
                                        Add bookmark
                                    </button>
                                </td>
                            </tr>
                        {/if}
                        </tfoot>
                    </BookmarkTable>
                {/if}
            {/if}

        </div>
    </div>

{/if}


