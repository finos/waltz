<script>

    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import {actorStore} from "../../../svelte-stores/actor-store";
    import {termSearch} from "../../../common";
    import _ from "lodash";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import DataExtractLink from "../../../common/svelte/DataExtractLink.svelte";

    let actorsCall = actorStore.findAll();

    let qry;

    $: actors = _
        .chain($actorsCall?.data)
        .map(d => Object.assign({}, d, {externalString: d.isExternal ? "External" : "Internal"}))
        .orderBy(d => _.toLower(d.name))
        .value();

    $: actorList = _.isEmpty(qry)
        ? actors
        : termSearch(actors, qry, ["name", "description", "externalString"]);

</script>

<PageHeader icon="user-circle"
            name="Actor List">

    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.actors.list">Actors</ViewLink>
            </li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="waltz-display-section">

        <div class="row">

            <div class="col-sm-12">
                <SearchInput bind:value={qry}/>
                <br>
                <div class="help-block pull-right small"
                     style="font-style: italic">
                    {#if _.size(actorList) === _.size(actors)}
                        Displaying all {_.size(actors)} actors
                    {:else}
                        Displaying {_.size(actorList)} out of {_.size(actors)} actors
                    {/if}
                </div>
                <div class="waltz-scroll-region-500">
                    <table class="table table-condensed">
                        <colgroup>
                            <col width="30%"/>
                            <col width="50%"/>
                            <col width="10%"/>
                            <col width="10%"/>
                        </colgroup>
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Description</th>
                            <th>Internal/External</th>
                            <th>External Id</th>
                        </tr>
                        </thead>
                        <tbody>
                        {#each actorList as actor}
                            <tr>
                                <td>
                                    <EntityLink ref={actor}/>
                                </td>
                                <td class="force-wrap">{actor.description}</td>
                                <td>{actor.externalString}</td>
                                <td>{actor.externalId || "-"}</td>
                            </tr>
                        {/each}
                        </tbody>
                    </table>
                </div>

                <br>

                <div class="pull-right">
                    <DataExtractLink name="Export Actors"
                                     filename="Actors"
                                     extractUrl="actor/all"
                                     styling="link"/>
                </div>
            </div>
        </div>
    </div>
</div>