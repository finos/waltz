<script>
    import moment from "moment";
    import _ from "lodash";
    import { SlickGrid } from "slickgrid";
    import Icon from "../../../common/svelte/Icon.svelte";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import { createEventDispatcher } from "svelte";
    import MiniActions from "../../../common/svelte/MiniActions.svelte";
    import DateTime from "../../../common/svelte/DateTime.svelte";
    import { mkSortFn } from "../../../common/slick-grid-utils";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import { termSearch } from "../../../common";
    import { compareDates } from "../../../common/sort-utils";
    import { severity } from "../../../common/services/enums/severity";

    export let measurableCategory;
    export let isAttestable = false;
    export let latestAttestation = null;
    export let unAttestedChanges = [];
    let grid;
    let viewData = [];
    let elem = null;
    let searchStr = "";
    let showingUnAttestedChanges = false;

    const dispatcher = createEventDispatcher();

    const attestMiniAction = {
        name: "Attest Now",
        icon: "check",
        description: "Initiate Attestation",
        handleAction: (ctx) => dispatcher("attestationInitiated", ctx),
    };

    const options = {
        enableCellNavigation: false,
        enableColumnReorder: false,
    };

    function initGrid(elem) {
        grid = new SlickGrid(elem, [], columnDefs, options);
        grid.onSort.subscribe((e, args) => {
            const sortCol = args.sortCol;
            grid.data.sort(mkSortFn(sortCol, args.sortAsc));
            grid.invalidate();
        });
    }

    function mkGridData(data) {
        return data;
    }

    function doGridSearch(data = [], searchStr) {
        return termSearch(data, searchStr, [
            "severity",
            "message",
            "userId",
            "createdAt",
        ]);
    }

    $: actions = _.compact([isAttestable ? attestMiniAction : null]);

    $: hasEverBeenAttested = !_.isNil(_.get(latestAttestation, "attestedBy"));

    $: columnDefs = [
        {
            id: "severity",
            field: "severity",
            name: "Severity",
            width: 50,
            sortable: true,
            formatter: (row, cell, value) => {
                return value
                    ? `<span title="${value}">${severity[value].name}</span>`
                    : "";
            },
        },
        {
            id: "message",
            field: "message",
            name: "Message",
            width: 200,
            formatter: (row, cell, value) => {
                return `<span title="${value}">${value}</span>`;
            },
        },
        {
            id: "userId",
            field: "userId",
            name: "User",
            width: 100,
            formatter: (row, cell, value) => {
                return `<span title="${value}">${value}</span>`;
            },
        },
        {
            id: "createdAt",
            field: "createdAt",
            name: "Timestamp",
            width: 50,
            sortable: true,
            formatter: (row, cell, value) => {
                if (value) {
                    const formatStr = "YYYY-MM-DD HH:mm:ss";
                    const format = (t) =>
                        moment.utc(t).local().format(formatStr);

                    return `<span>${format(value)}</span>`;
                }
                return "";
            },
            sortFn: (a, b) => compareDates(a?.createdAt, b?.createdAt),
        },
    ];
    $: {
        if (unAttestedChanges.length) {
            viewData = mkGridData(unAttestedChanges);
        }

        if (elem && !_.isEmpty(viewData)) {
            initGrid(elem);
        }
    }

    $: {
        const data = doGridSearch(viewData, searchStr);
        if (grid) {
            grid.data = data;
            grid.invalidate();
        }
    }
</script>

{#if measurableCategory}
    <SubSection>
        <div slot="header">
            {measurableCategory.name}
        </div>
        <div slot="content">
            {#if hasEverBeenAttested}
                <table class="table waltz-field-table waltz-field-table-border">
                    <tr>
                        <td class="wft-label">Attested By:</td>
                        <td>{latestAttestation.attestedBy}</td>
                    </tr>
                    <tr>
                        <td class="wft-label">Attested At:</td>
                        <td>
                            <DateTime dateTime={latestAttestation.attestedAt} />
                        </td>
                    </tr>
                </table>
            {:else}
                <NoData type="warning">
                    Never attested.
                    {#if isAttestable}
                        <button
                            class="btn-link"
                            on:click={() =>
                                attestMiniAction.handleAction(
                                    measurableCategory,
                                )}
                        >
                            Attest now
                        </button>
                    {/if}
                </NoData>
            {/if}
        </div>

        <div slot="controls">
            <div style="float:right" class="small">
                {#if isAttestable}
                    <MiniActions {actions} ctx={measurableCategory} />
                {/if}
            </div>
        </div>
        <div slot="changes">
            {#if !_.isEmpty(viewData)}
                <NoData type={"warning"}>
                    {#if showingUnAttestedChanges}
                        <SearchInput bind:value={searchStr} />
                        <div
                            class="slick-container"
                            style="width:100%;height:250px;"
                            bind:this={elem}
                        ></div>
                    {:else}
                        <Icon 
                            name="exclamation-triangle" 
                            size="2x"
                        />
                        Unattested changes detected
                        <button
                            class="btn-link"
                            on:click={() => (showingUnAttestedChanges = true)}
                        >
                            Show the <span>{unAttestedChanges.length}</span> unattested
                            change/s.
                        </button>
                    {/if}
                </NoData>
            {:else}
                <NoData>No attestation changes</NoData>
            {/if}
        </div>
    </SubSection>
{/if}
