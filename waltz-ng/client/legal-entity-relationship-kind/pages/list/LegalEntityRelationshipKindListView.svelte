<script>

    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {entity} from "../../../common/services/enums/entity";
    import {legalEntityRelationshipKindStore} from "../../../svelte-stores/legal-entity-relationship-kind-store";
    import {termSearch} from "../../../common";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import pageInfo from "../../../svelte-stores/page-navigation-store";
    import _ from "lodash";
    import NoData from "../../../common/svelte/NoData.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    let legalEntityRelationshipKindCall = legalEntityRelationshipKindStore.findAll();
    let statsCall = legalEntityRelationshipKindStore.findUsageStats();

    let qry;

    $: legalEntityRelationshipKinds = $legalEntityRelationshipKindCall?.data;
    $: usageStatsByKindId = _.keyBy($statsCall?.data, d => d.relKindId);

    $: filteredKinds = _.isEmpty(qry)
        ? legalEntityRelationshipKinds
        : termSearch(legalEntityRelationshipKinds, qry, ["name", "description", "externalId"])

    function gotoPage(kind) {
        $pageInfo = {
            state: "main.legal-entity-relationship-kind.view",
            params: {
                id: kind.id
            }
        };
    }

</script>


<PageHeader icon={entity.LEGAL_ENTITY_RELATIONSHIP_KIND.icon}
            name="Legal Entity Relationship Kinds">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                Legal Entity Relationship Kinds
            </li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach"
     style="margin-bottom: 5em;">
    <div class="waltz-display-section">
        <div class="row">
            <div class="col-sm-12">
                <SearchInput bind:value={qry}/>
                <div style="padding-top: 2em">
                    <table class="table table-condensed table-hover">
                        <colgroup>
                            <col width="30%"/>
                            <col width="30%"/>
                            <col width="10%"/>
                            <col width="10%"/>
                            <col width="10%"/>
                            <col width="10%"/>
                        </colgroup>
                        <thead>
                        <tr>
                            <td>Name</td>
                            <td>Description</td>
                            <td>External Id</td>
                            <td>Target Kind</td>
                            <td>Cardinality</td>
                            <td>Usage Stats</td>
                        </tr>
                        </thead>
                        <tbody>
                        {#each filteredKinds as kind}
                            <tr class="clickable"
                                on:click={() => gotoPage(kind)}>
                                <td>{kind.name}</td>
                                <td>{kind.description}</td>
                                <td>{kind.externalId}</td>
                                <td>{entity[kind.targetKind].name}</td>
                                <td>{kind.cardinality}</td>
                                <td>
                                    <ul class="list-unstyled">
                                        <li>
                                            <Icon name={entity[kind.targetKind].icon}/>
                                            - {_.get(usageStatsByKindId, [kind.id, "targetEntityCount"], 0)}
                                        </li>
                                        <li>
                                            <Icon name={entity.LEGAL_ENTITY.icon}/>
                                            - {_.get(usageStatsByKindId, [kind.id, "legalEntityCount"], 0)}
                                        </li>
                                        <li>
                                            <Icon name={entity.LEGAL_ENTITY_RELATIONSHIP.icon}/>
                                            - {_.get(usageStatsByKindId, [kind.id, "relationshipCount"], 0)}
                                        </li>
                                    </ul>
                            </tr>
                        {:else}
                            <NoData>
                                There are no legal entity relationship kinds
                            </NoData>
                        {/each}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
