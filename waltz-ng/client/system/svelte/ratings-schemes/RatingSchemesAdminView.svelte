<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";

    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import {termSearch} from "../../../common";
    import RatingSchemePreviewBar from "./RatingSchemePreviewBar.svelte";

    const loadSchemeCall = ratingSchemeStore.loadAll();

    $: ratingSchemes = _
        .chain(termSearch($loadSchemeCall.data, qry, ["name", "description"]))
        .orderBy("name")
        .value();

    let qry;

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

    <div class="row">
        <div class="col-md-12">
            <SearchInput bind:value={qry}/>
            <table class="table table-condensed table-striped"
                   style="table-layout: fixed">
                <thead>
                <tr>
                    <th style="width:40%">Name</th>
                    <th style="width:25%">Description</th>
                    <th style="width:35%">Operations</th>
                </tr>
                </thead>
                <tbody>
                {#each ratingSchemes as scheme}
                    <tr>
                        <td>
                            <span title={scheme.description}>
                                {scheme.name}
                            </span>
                            <RatingSchemePreviewBar {scheme}/>
                        </td>
                        <td>
                            {scheme.description}
                        </td>
                        <td>

                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    </div>
</div>


<style>
    .rating-square {
        width: 1em;
        height: 1em;
        display: inline-block;
    }
</style>