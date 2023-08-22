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
    import Icon from "../../../common/svelte/Icon.svelte";
    import {entity} from "../../../common/services/enums/entity";
    import {overlayDiagramKind} from "../../../common/services/enums/overlay-diagram-kind";


    let diagramsCall = aggregateOverlayDiagramStore.findByKind(overlayDiagramKind.WALTZ_STATIC_SVG_OVERLAY.key);
    $: diagrams = $diagramsCall?.data;
    $: diagramsById = _.keyBy(diagrams, d => d.id);

    let qry = "";

    let instancesCall = aggregateOverlayDiagramInstanceStore.findAll();

    $: instances = _.map($instancesCall?.data, d => Object.assign({}, d, {diagram: _.get(diagramsById, d.diagramId)}));

    $: instanceList = _.isEmpty(qry)
        ? instances
        : termSearch(instances, qry, ["diagram.name", "name", "description"]);

    $: instancesByDiagramId = _
        .chain(instanceList)
        .orderBy(d => _.toLower(d.name))
        .groupBy(d => d.diagramId)
        .value();

    $: diagramsWithInstances = _.map(diagrams, d => Object.assign({}, d, {instances: _.get(instancesByDiagramId, d.id, [])}))

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
                {#if _.isEmpty(diagrams)}
                    <NoData>There are no diagrams</NoData>
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
                        {#each _.orderBy(diagramsWithInstances, d => d.name) as diagram}
                            <tbody>
                            {#each diagram.instances as instance}
                                <tr class="waltz-visibility-parent">
                                    {#if _.indexOf(diagram.instances, instance) === 0}
                                        <td title={diagram.description}>
                                            <strong>
                                                <Icon name={entity[diagram.aggregatedEntityKind].icon}/>
                                                {diagram.name}
                                            </strong>
                                        </td>
                                    {:else}
                                        <td title={diagram.description}
                                            class="waltz-visibility-child-30">
                                            {diagram.name}
                                        </td>
                                    {/if}
                                    <td>
                                        <EntityLink ref={instance}/>
                                    </td>
                                    <td>{instance.description}</td>
                                    <td>
                                        <LastEdited entity={instance}/>
                                    </td>
                                </tr>
                            {:else}
                                <tr>
                                <tr>
                                <td title={diagram.description}>
                                        <span class="text-muted">
                                            <Icon name={entity[diagram.aggregatedEntityKind].icon}/>
                                            {diagram.name}
                                        </span>
                                </td>
                                    <td colspan="3">
                                        <span class="text-muted">There are no instances for this diagram</span>
                                    </td>
                                </tr>
                            {/each}
                            </tbody>
                        {/each}
                    </table>

                {/if}
            </div>

        </div>

    </div>
</div>