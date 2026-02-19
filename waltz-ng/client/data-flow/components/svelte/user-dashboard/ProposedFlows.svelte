<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import ProposedFlowSection from "./ProposedFlowSection.svelte";
    import {proposeDataFlowRemoteStore} from "../../../../svelte-stores/propose-data-flow-remote-store";
    import {displayError} from "../../../../common/error-utils";

    export let userName;
    export let dataTypeIdToNameMap = {};
    export let person;

    const actionStatusPillDefs = {
        PENDING_APPROVALS: {
            name: "Pending Approvals",
            borderColor: "#8e8e56",
            textColor: "#000000",
            bgColor: "#666666"
        },
        SOURCE_APPROVED: {
            name: "Source Approved",
            borderColor: "#74a259",
            textColor: "#000000",
            bgColor: "#666666"
        },
        TARGET_APPROVED: {
            name: "Target Approved",
            borderColor: "#74a259",
            textColor: "#000000",
            bgColor: "#666666"
        }
    }
    const actionStatuses = Object.keys(actionStatusPillDefs);

    const historicalStatusPillDefs = {
        FULLY_APPROVED: {
            name: "Fully Approved",
            borderColor: "#5bb65d",
            textColor: "#000000",
            bgColor: "#666666"
        },
        SOURCE_REJECTED: {
            name: "Source Rejected",
            borderColor: "#c1664f",
            textColor: "#000000",
            bgColor: "#666666"
        },
        TARGET_REJECTED: {
            name: "Target Rejected",
            borderColor: "#c1664f",
            textColor: "#000000",
            bgColor: "#666666"
        },
        CANCELLED: {
            name: "Cancelled",
            borderColor: "#c16644",
            textColor: "#000000",
            bgColor: "#666666"
        }
    }
    const historicalStatuses = Object.keys(historicalStatusPillDefs)

    const changeTypePillDefs = {
        CREATE: {
            name: "Create",
            borderColor: "#267dda",
            textColor: "#000000",
            bgColor: "#666666"
        },
        EDIT: {
            name: "Edit",
            borderColor: "#716b9e",
            textColor: "#000000",
            bgColor: "#666666"
        },
        DELETE: {
            name: "Delete",
            borderColor: "#da524b",
            textColor: "#000000",
            bgColor: "#666666"
        }
    }

    const proposerTypePillDefs = {
        USER: {
            name: "Proposed By You",
            borderColor: "#000000",
            textColor: "#000000",
            bgColor: "#666666"
        },
        OTHERS: {
            name: "For Approval",
            borderColor: "#000000",
            textColor: "#000000",
            bgColor: "#666666"
        }
    }

    const TABS = {
        ACTION: "Actionable",
        HISTORY: "Historical"
    }

    let selectedTab = TABS.ACTION;

    let flows = [];

    $: selectionOptions = {
        entityLifecycleStatuses: ["ACTIVE"],
        entityReference: {
            id: person ? person.id : null,
            kind: person ? person.kind : null
        },
        filters : {},
        scope: "EXACT"
    }

    $ : {
        proposeDataFlowRemoteStore.findProposedFlowsBySelector(selectionOptions)
        .then(r => {
            flows = r.data;
        })
        .catch(e => {
            displayError("Something went wrong, please try again.");
        })
    }

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
