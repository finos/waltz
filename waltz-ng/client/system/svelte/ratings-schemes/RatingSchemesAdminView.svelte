<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import ItemPreviewBar from "./ItemPreviewBar.svelte";
    import SchemeEditor from "./SchemeEditor.svelte";
    import ItemsView from "./ItemsView.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import toasts from "../../../svelte-stores/toast-store"

    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import {termSearch} from "../../../common";
    import {countUsageStatsBy, sortItems} from "./rating-scheme-utils";
    import SchemeRemovalConfirmation from "./SchemeRemovalConfirmation.svelte";
    import _ from "lodash";


    const Modes = {
        LIST: "list",
        EDIT_SCHEME: "edit_scheme",
        EDIT_RATINGS: "edit_ratings",
        DELETE: "delete"
    };

    const loadSchemeCall = ratingSchemeStore.loadAll(true);
    const usageCall = ratingSchemeStore.calcRatingUsageStats(true);

    $: usageCountsBySchemeId = countUsageStatsBy($usageCall.data, d => d.schemeId);

    let qry;
    let activeMode = Modes.LIST;
    let activeSchemeId = null;
    let activeScheme = null;

    $: ratingSchemes = _
        .chain(termSearch($loadSchemeCall.data, qry, ["name", "description"]))
        .orderBy("name")
        .value();


    $: activeScheme = activeSchemeId
        ? _.find(ratingSchemes, ({id: activeSchemeId}))
        : null;


    function onEditScheme(scheme) {
        activeSchemeId = scheme.id;
        activeMode = Modes.EDIT_SCHEME;
    }

    function onEditRatings(scheme) {
        activeScheme = scheme;
        activeMode = Modes.EDIT_RATINGS;
    }


    function doSaveScheme(scheme) {
        const savePromise = ratingSchemeStore
            .save(scheme);

        return Promise
            .resolve(savePromise)
            .then(() => {
                activeScheme = null;
                activeMode = Modes.LIST;
                ratingSchemeStore.loadAll(true);
                toasts.success("Successfully saved rating scheme")
            })
            .catch(e => toasts.error("Unable to save rating scheme. " + e.error));
    }


    function doSaveItem(item) {
        const saveItemPromise = ratingSchemeStore
            .saveItem(item);

        return Promise
            .resolve(saveItemPromise)
            .then(() => {
                ratingSchemeStore.loadAll(true);
                toasts.success("Successfully saved rating scheme item")
            })
            .catch(e => toasts.error("Unable to save rating scheme item. " + e.error));
    }


    function doRemoveItem(itemId) {
        const removeItemPromise = ratingSchemeStore
            .removeItem(itemId);

        return Promise
            .resolve(removeItemPromise)
            .then(() => {
                ratingSchemeStore.loadAll(true);
                toasts.success("Successfully removed rating scheme item")
            })
            .catch(e => toasts.error("Unable to remove rating scheme item. " + e.error));
    }


    function doRemoveScheme(schemeId) {
        const removePromise = ratingSchemeStore
            .removeScheme(schemeId);

        return Promise
            .resolve(removePromise)
            .then(() => {
                activeScheme = null;
                activeMode = Modes.LIST;
                ratingSchemeStore.loadAll(true);
                toasts.success("Successfully removed rating scheme")
            })
            .catch(e => toasts.error("Unable to remove rating scheme. " + e.error));
    }


    function onCancel() {
        activeScheme = null;
        activeMode = Modes.LIST;
    }

    function onRemoveScheme(scheme) {
        activeScheme = scheme;
        activeMode = Modes.DELETE;
    }

    function mkNew() {
        activeScheme = {
            name: null,
            description: null,
            ratings: []
        };
        activeMode = Modes.EDIT_SCHEME;
    }

</script>

<PageHeader icon="puzzle-piece"
            name="Rating Schemes">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Rating Schemes</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">
            <p>Rating schemes define values which can be used to describe entities.</p>
            <p>
                The main usages are with measurables, where each category has an associated rating scheme,
                and assessments, where each definition is associated to a scheme.
            </p>
        </div>
    </div>

    {#if activeMode === Modes.EDIT_SCHEME}

        <SchemeEditor scheme={activeScheme}
                            doCancel={onCancel}
                            {doSaveScheme}/>

    {:else if activeMode === Modes.DELETE}
        <SchemeRemovalConfirmation scheme={activeScheme}
                                   doCancel={onCancel}
                                   doRemove={doRemoveScheme}/>

    {:else if activeMode === Modes.EDIT_RATINGS && !_.isNil(activeScheme) }

        <ItemsView scheme={activeScheme}
                   ratings={sortItems(activeScheme.ratings)}
                   doCancel={onCancel}
                   doRemove={doRemoveItem}
                   doSave={doSaveItem}/>

    {:else if activeMode === Modes.LIST}
    <div class="row">
        <div class="col-md-12">
            <SearchInput bind:value={qry}/>
            <table class="table table-condensed table-striped table-hover"
                   style="table-layout: fixed">
                <thead>
                <tr>
                    <th style="width:20%">Name</th>
                    <th style="width:20%">Ratings</th>
                    <th style="width:20%">Description</th>
                    <th style="width:10%">Usages</th>
                    <th style="width:30%">Operations</th>
                </tr>
                </thead>
                <tbody>
                {#each ratingSchemes as scheme}
                    <tr>
                        <td>
                            <span title={scheme.description}>
                                {scheme.name}
                            </span>
                        </td>
                        <td>
                            <ItemPreviewBar items={sortItems(scheme.ratings)}/>
                        </td>
                        <td>
                            {scheme.description}
                        </td>
                        <td>
                            {usageCountsBySchemeId[scheme.id] || "-"}
                        </td>
                        <td>
                            <button class="btn-link"
                                    aria-label="Edit {scheme.name}"
                                    on:click={() => onEditScheme(scheme)}>
                                <Icon name="edit"/>
                                Edit Scheme
                            </button>
                            |
                            <button class="btn-link"
                                    aria-label="Edit {scheme.name}"
                                    on:click={() => onEditRatings(scheme)}>
                                <Icon name="edit"/>
                                Edit Ratings
                            </button>
                            |
                            <button class="btn-link"
                                    aria-label="Remove"
                                    disabled={usageCountsBySchemeId[scheme.id]||0 > 0}
                                    on:click={() => onRemoveScheme(scheme)}>
                                <Icon name="trash"/>
                                Remove
                            </button>
                        </td>
                    </tr>
                {/each}
                </tbody>
                <tfoot>
                <tr>
                    <td colspan="5">
                        <button class="btn-link"
                                on:click={mkNew}>
                            <Icon name="plus"/>
                            Add new rating scheme
                        </button>
                    </td>
                </tr>
                </tfoot>
            </table>
        </div>
    </div>
    {/if}
</div>


<style>
    button:disabled {
        color: #999;
    }
    button:disabled:hover {
        cursor: not-allowed;
    }
</style>