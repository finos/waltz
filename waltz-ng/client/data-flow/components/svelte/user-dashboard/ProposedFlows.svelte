<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import ProposedFlowSection from "./ProposedFlowSection.svelte";
    import {proposeDataFlowRemoteStore} from "../../../../svelte-stores/propose-data-flow-remote-store";

    export let userName;
    export let dataTypeIdToNameMap = {};

    const actionStatusPillDefs = {
        PENDING_APPROVALS: {
            name: "Pending Approvals",
            color: "#8e8e56"
        },
        SOURCE_APPROVED: {
            name: "Source Approved",
            color: "#74a259"
        },
        TARGET_APPROVED: {
            name: "Target Approved",
            color: "#74a259"
        }
    }
    const actionStatuses = Object.keys(actionStatusPillDefs);

    const historicalStatusPillDefs = {
        FULLY_APPROVED: {
            name: "Fully Approved",
            color: "#5bb65d"
        },
        SOURCE_REJECTED: {
            name: "Source Rejected",
            color: "#c1664f"
        },
        TARGET_REJECTED: {
            name: "Target Rejected",
            color: "#c1664f"
        }
    }
    const historicalStatuses = Object.keys(historicalStatusPillDefs)

    const changeTypePillDefs = {
        CREATE: {
            name: "Create",
            color: "#267dda"
        },
        EDIT: {
            name: "Edit",
            color: "#716b9e"
        },
        DELETE: {
            name: "Delete",
            color: "#da524b"
        }
    }

    const proposerTypePillDefs = {
        USER: {
            name: "Proposed By You",
            color: "#000000"
        },
        OTHERS: {
            name: "For Approval",
            color: "#000000"
        }
    }

    const TABS = {
        ACTION: "Actionable",
        HISTORY: "Historical"
    }

    let selectedTab = TABS.ACTION;

    $: getProposedFlowsCall = proposeDataFlowRemoteStore.getProposedFlowsForUser();
    $: flows = $getProposedFlowsCall?.data;

    $: actionableFlows = flows && flows.length
        ? flows.filter(f => actionStatuses.includes(f.workflowState.state))
        : [];

    $: historicalFlows = flows && flows.length
        ? flows.filter(f => historicalStatuses.includes(f.workflowState.state))
        : [];
</script>

<div class="waltz-tabs" style="padding-top: 1em">
    <input type="radio"
           bind:group={selectedTab}
           value={TABS.ACTION}
           id={TABS.ACTION}>
    <label class="wt-label"
           for={TABS.ACTION}>
            <span><Icon name="pencil-square-o"/> {TABS.ACTION} Flows -
                <small class="text-muted">{actionableFlows.length ?? 0}</small>
            </span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value={TABS.HISTORY}
           id={TABS.HISTORY}>
    <label class="wt-label"
           for={TABS.HISTORY}>
        <span><Icon name="clock"/> {TABS.HISTORY} Flows -
            <small class="text-muted">{historicalFlows.length ?? 0}</small>
        </span>
    </label>
    <div class="wt-tab wt-active">
        { #if selectedTab === TABS.ACTION }
        <ProposedFlowSection userName={userName}
                             flows={actionableFlows}
                             dataTypeIdToNameMap={dataTypeIdToNameMap}
                             statusPillDefs={actionStatusPillDefs}
                             changeTypePillDefs={changeTypePillDefs}
                             proposerTypePillDefs={proposerTypePillDefs}
                             currentTabText={TABS.ACTION}/>
        { :else if selectedTab === TABS.HISTORY }
        <ProposedFlowSection userName={userName}
                             flows={historicalFlows}
                             dataTypeIdToNameMap={dataTypeIdToNameMap}
                             statusPillDefs={historicalStatusPillDefs}
                             changeTypePillDefs={changeTypePillDefs}
                             proposerTypePillDefs={proposerTypePillDefs}
                             currentTabText={TABS.HISTORY}/>
        {/if}
    </div>
</div>
