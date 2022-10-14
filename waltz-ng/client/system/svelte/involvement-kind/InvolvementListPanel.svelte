<script>

    import {involvementKindStore} from "../../../svelte-stores/involvement-kind-store";
    import InvolvementBreakdown from "./InvolvementBreakdown.svelte";
    import _, {isEmpty} from "lodash";
    import {involvementViewStore} from "../../../svelte-stores/involvement-view-store";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../common";
    import NoData from "../../../common/svelte/NoData.svelte";

    export let involvementKind;

    let usageStatsCall;
    let involvementsCall;

    let selectedEntityKind;
    let qry;

    function selectStat(usageStat) {
        selectedEntityKind = usageStat.entityInfo
    }

    $: {
        if (involvementKind.id) {
            usageStatsCall = involvementKindStore.findUsageStatsForKind(involvementKind.id)
        }
    }


    $: {
        if (involvementKind.id && selectedEntityKind) {
            involvementsCall = involvementViewStore
                .findInvolvementsByKindAndEntityKind(involvementKind.id, selectedEntityKind.key);
        }
    }


    $: involvementUsageStats = $usageStatsCall?.data || [];
    $: involvements = _.orderBy($involvementsCall?.data || [], d => _.toLower(d.involvement.entityReference.name));

    $: displayedInvolvements = isEmpty(qry)
        ? involvements
        : termSearch(involvements, qry, ["involvement.entityReference.name", "person.displayName", "person.email"])

    $: filtersApplied = _.size(displayedInvolvements) !== _.size(involvements)

</script>


{#if !_.isEmpty(involvementUsageStats.breakdown)}
    <InvolvementBreakdown breakdownStats={involvementUsageStats.breakdown}
                          onClick={selectStat}
                          displayMode="TABLE"/>

    {#if selectedEntityKind}
        <hr>
        <h4>Involvements to {selectedEntityKind.name}s
            <span class="text-muted">
                (
                {#if filtersApplied}{_.size(displayedInvolvements)} / {/if}{_.size(involvements)})
            </span>
            :
        </h4>

        {#if _.size(involvements) > 10}
            <SearchInput bind:value={qry}/>
        {/if}
        <div class:waltz-scroll-region-350={_.size(involvements) > 10}>
            <table class="table table-condensed">
                <thead>
                <tr>
                    <th>Entity</th>
                    <th>Person</th>
                </tr>
                </thead>
                <tbody>
                {#each displayedInvolvements as involvement}
                    <tr>
                        <td>
                            <EntityLink ref={involvement.involvement.entityReference}/>
                        </td>
                        <td>
                            <EntityLink ref={involvement.person}/>
                        </td>
                    </tr>
                {:else}
                    <tr>
                        <td>
                            There are no active people with the involvement {involvementKind.name}
                            for {selectedEntityKind.name}s
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    {:else}
        <div class="help-block">
            Select a row from the table to see all the involvements for that entity kind
        </div>
    {/if}
{:else}
    <div>
        <NoData> There are no usages of this involvement kind</NoData>
    </div>
{/if}