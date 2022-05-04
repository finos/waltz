<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {aggregateOverlayDiagramStore} from "../../../svelte-stores/aggregate-overlay-diagram-store";
    import {aggregateOverlayDiagramInstanceStore} from "../../../svelte-stores/aggregate-overlay-diagram-instance-store";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import _ from "lodash";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {termSearch} from "../../../common";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";


    let diagramsCall = aggregateOverlayDiagramStore.findAll();
    $: diagrams = $diagramsCall?.data;
    $: diagramsById = _.keyBy(diagrams, d => d.id);

    let qry = "";

    let instancesCall = aggregateOverlayDiagramInstanceStore.findAll();

    $: instances = _.map(
        $instancesCall?.data,
        d => Object.assign({}, d, {diagram: diagramsById[d.diagramId]}));

    $: instanceList = _.isEmpty(qry)
        ? instances
        : termSearch(instances, qry, ['diagram.name', 'name', 'description']);

</script>


<PageHeader icon="object-group"
            name={"Overlay Diagrams"}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.aggregate-overlay-diagram.list">Diagrams</ViewLink>
            </li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="waltz-display-section">

        <div class="row">
            <div class="col-sm-12">
                {#if _.size(instances) > 10}
                    <SearchInput bind:value={qry}
                                 placeholder="Search diagram instances"/>
                    <br>
                {/if}
                {#if _.isEmpty(instances)}
                    <NoData>There are no diagram instances</NoData>
                {:else}
                    <table class="table table-condensed">
                        <colgroup>
                            <col width="20%"/>
                            <col width="20%"/>
                            <col width="45%"/>
                            <col width="15%"/>
                        </colgroup>
                        <thead>
                        <tr>
                            <th>Diagram</th>
                            <th>Instance</th>
                            <th>Description</th>
                            <th>Last Updated</th>
                        </tr>
                        </thead>
                        <tbody>
                        {#each _.orderBy(instanceList, d => [d.diagram?.name, d.name]) as instance}
                            <tr>
                                <td title={instance.diagram?.description}>
                                    {instance.diagram?.name}
                                </td>
                                <td>
                                    <EntityLink ref={instance}/>
                                </td>
                                <td>{instance.description}</td>
                                <td>
                                    <LastEdited entity={instance}/>
                                </td>
                            </tr>
                        {/each}
                        </tbody>
                    </table>
                {/if}
            </div>

        </div>

    </div>
</div>