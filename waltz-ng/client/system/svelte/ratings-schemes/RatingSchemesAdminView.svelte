<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import RatingSchemePreviewBar from "./RatingSchemePreviewBar.svelte";
    import RatingSchemeEditor from "./RatingSchemeEditor.svelte";

    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import {termSearch} from "../../../common";
    import Icon from "../../../common/svelte/Icon.svelte";


    const Modes = {
        LIST: "list",
        EDIT: "edit",
        DELETE: "delete"
    };

    const loadSchemeCall = ratingSchemeStore.loadAll();

    let qry;
    let activeMode = Modes.LIST;
    let activeScheme = null;

    $: ratingSchemes = _
        .chain(termSearch($loadSchemeCall.data, qry, ["name", "description"]))
        .orderBy("name")
        .value();

    function onEdit(scheme) {
        activeScheme = scheme;
        activeMode = Modes.EDIT;
    }

    function doSchemeSave(scheme) {
        return ratingSchemeStore
            .save(scheme)
            .then(() => {
                activeScheme = null;
                activeMode = Modes.LIST;
                ratingSchemeStore.loadAll(true);
            });
    }

    function onCancel() {
        activeScheme = null;
        activeMode = Modes.LIST;
    }

    function mkNew() {
        activeScheme = {
            name: null,
            description: null,
            ratings: []
        };
        activeMode = Modes.EDIT;
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

    {#if activeMode === Modes.EDIT}
        <RatingSchemeEditor scheme={activeScheme}
                            doCancel={onCancel}
                            doSave={doSchemeSave}/>
    {:else if activeMode === Modes.LIST}
    <div class="row">
        <div class="col-md-12">
            <SearchInput bind:value={qry}/>
            <table class="table table-condensed table-striped table-hover"
                   style="table-layout: fixed">
                <thead>
                <tr>
                    <th style="width:25%">Name</th>
                    <th style="width:25%">Ratings</th>
                    <th style="width:25%">Description</th>
                    <th style="width:25%">Operations</th>
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
                            <RatingSchemePreviewBar {scheme}/>
                        </td>
                        <td>
                            {scheme.description}
                        </td>
                        <td>
                            <button class="btn-link"
                                    aria-label="Edit {scheme.name}"
                                    on:click={() => onEdit(scheme)}>
                                <Icon name="edit"/>
                                Edit
                            </button>
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
            <button class="btn-link"
                    on:click={mkNew}>
                <Icon name="plus"/>
                Add new rating scheme
            </button>
        </div>
    </div>
    {/if}
</div>


<style>
</style>