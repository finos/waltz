<script>
    import { sortByField, STATES } from "../utils";
    export let proposedFlow;

    $: workflowState = proposedFlow?.workflowState || {};
    $: workflowTransitionList = proposedFlow?.workflowTransitionList
    ? sortByField(proposedFlow.workflowTransitionList, 'lastUpdatedAt', 'asc')
    : [];

    const initialStep = {
        key: "request",
        label: "Request Submitted",
        states: [STATES.PENDING_APPROVALS, STATES.SUBMITTED]
    };

    const stepTypes = {
        SOURCE: {
            label: "Source approval",
            approved: STATES.SOURCE_APPROVED,
            rejected: STATES.SOURCE_REJECTED
        },
        TARGET: {
            label: "Target approval",
            approved: STATES.TARGET_APPROVED,
            rejected: STATES.TARGET_REJECTED
        },
        FINAL: {
            label: "Flow provisioned",
            approved: STATES.FULLY_APPROVED
        }
    };

    // Dynamically build approval steps in the order transitions occur
    $: getApprovalSteps = function(transitions) {
        const steps = [];
        transitions?.forEach(t => {
            if ([STATES.SOURCE_APPROVED, STATES.SOURCE_REJECTED].includes(t.toState)) {
                steps.push({
                    key: "source",
                    label: stepTypes.SOURCE.label,
                    states: [stepTypes.SOURCE.approved, stepTypes.SOURCE.rejected],
                    directState: t.toState
                });
            } else if ([STATES.TARGET_APPROVED, STATES.TARGET_REJECTED].includes(t.toState)) {
                steps.push({
                    key: "target",
                    label: stepTypes.TARGET.label,
                    states: [stepTypes.TARGET.approved, stepTypes.TARGET.rejected],
                    directState: t.toState
                });
            }
        });
        
        // If a step hasn't appeared yet, add it after
        if (!steps.find(s => s.key === "source")) {
            steps.push({
                key: "source",
                label: stepTypes.SOURCE.label,
                states: [stepTypes.SOURCE.approved, stepTypes.SOURCE.rejected]
            });
        }
        if (!steps.find(s => s.key === "target")) {
            steps.push({
                key: "target",
                label: stepTypes.TARGET.label,
                states: [stepTypes.TARGET.approved, stepTypes.TARGET.rejected]
            });
        }
        return steps;
    }

    $: approvalSteps = getApprovalSteps(workflowTransitionList);
    $: steps = [initialStep, ...approvalSteps, {
        key: "final",
        label: stepTypes.FINAL.label,
        states: [stepTypes.FINAL.approved]
    }];

    // Collect completed states
    $: completedStates = new Set(workflowTransitionList?.map(t => t.toState));

    $: showLoading = (
        !workflowState ||
        !workflowTransitionList ||
        workflowTransitionList.length === 0 ||
        approvalSteps.length < 2 ||
        steps.length < 4
    );

    $: stepStatus = !showLoading ? (() => {
        let foundActive = false;
        // If fully approved, all steps are complete
        if (completedStates.has(STATES.FULLY_APPROVED)) {
            return steps.map(s => ({ ...s, status: "complete" }));
        }
        return steps.map((step, idx) => {
            let status = "pending";
            if (step.key === "request") {
                status = "complete";
            } else if (step.states.some(s => completedStates.has(s))) {
                // Check if this is a rejected step
                if ([STATES.SOURCE_REJECTED, STATES.TARGET_REJECTED].includes(step.states.find(s => completedStates.has(s)))) {
                    status = "rejected";
                } else if ([STATES.SOURCE_APPROVED, STATES.TARGET_APPROVED].includes(step.states.find(s => completedStates.has(s)))) {
                    status = "complete";
                }
            }
            // Mark only the FIRST pending step as active (amber)
            if (!foundActive && status === "pending") {
                status = "active";
                foundActive = true;
            }
            return { ...step, status };
        });
    })(): [];

    // If "fully approved", mark all steps complete
    if (completedStates?.has(STATES.FULLY_APPROVED)) {
        stepStatus = stepStatus?.map(s => ({ ...s, status: "complete" }));
    }

    // Find active (amber) step: first "pending"
    $: activeStepIdx = stepStatus?.findIndex(s => s.status === "pending");

    // If none pending, no active step
    if (stepStatus && activeStepIdx !== -1) {
        stepStatus[activeStepIdx].status = "active";
    }

    function getCircleClass(status) {
        return status;
    }
</script>

{#if !showLoading}
    <div class="wizard-wrapper">
        <div class="wizard-steps">
            {#each stepStatus as step, idx}
                <div class="step-container">
                    <span class="circle {getCircleClass(step.status)}">
                        {#if step.status === "complete"}
                            <svg class="icon-check" viewBox="0 0 20 20" width="22" height="22">
                                <polyline points="4 11 8 15 16 6" fill="none" stroke="#25b740" stroke-width="2" />
                            </svg>
                        {:else if step.status === "rejected"}
                            <svg class="icon-cross" viewBox="0 0 20 20" width="22" height="22">
                                <line x1="5" y1="5" x2="15" y2="15" stroke="#d32f2f" stroke-width="2"/>
                                <line x1="15" y1="5" x2="5" y2="15" stroke="#d32f2f" stroke-width="2"/>
                            </svg>
                        {:else if step.status === "active"}
                            <svg class="icon-exclaim" viewBox="0 0 20 20" width="22" height="22">
                                <rect x="9" y="5" width="2" height="6" fill="#ffc107"/>
                                <rect x="9" y="13" width="2" height="2" fill="#ffc107"/>
                            </svg>
                        {:else}
                            {idx + 1}
                        {/if}
                    </span>
                    <div class="label">{step.label}</div>
                    {#if idx < stepStatus.length - 1}
                        <div class="connector {step.status === 'complete' ? 'complete' : ''}"></div>
                    {/if}
                </div>
            {/each}
        </div>
    </div>
{:else}
    <div class="loading-state">Loading flow information...</div>
{/if}

<style>
    .loading-state {
        text-align: center;
        padding: 2rem;
        font-size: 1.2rem;
        color: #999;
    }

    .wizard-wrapper {
        width: 45vw;
        min-width: 320px;
        max-width: 700px;
        margin: 2rem auto;
    }
    .wizard-steps {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        position: relative;
        width: 100%;
        padding: 0 16px;
    }
    .step-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        flex: 1 1 0%;
        position: relative;
        z-index: 2;
    }
    .circle {
        width: 35px;
        height: 35px;
        border-radius: 50%;
        border: 2px solid #d9d9d9;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        background: #fff;
        transition: border-color 0.2s, background 0.2s;
        box-sizing: border-box;
        position: relative;
        z-index: 3;
    }
    .circle.complete {
        background: #dff1d2;
        border-color: #25b740;
        color: #fff;
    }
    .circle.complete .icon-check {
        stroke: #25b740;
    }
    .circle.active {
        background: #fffbe6;
        border-color: #ffc107;
        color: #ffc107;
        box-shadow: 0 0 0 4px #fffbe6;
    }
    .circle.pending {
        background: #fff;
        border-color: #d9d9d9;
        color: #d9d9d9;
    }
    .circle.rejected {
        background: #ffd4d4;
        border-color: #d32f2f;
        color: #d32f2f;
        box-shadow: 0 0 0 4px #ffd4d4;
    }
    .icon-check,
    .icon-exclaim,
    .icon-cross {
        font-size: 24px;
        display: block;
    }
    .label {
        font-size: 1rem;
        text-align: center;
        width: 85px;
        font-weight: bold;
        color: #333;
        min-height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        white-space: normal;
    }
    .connector {
        position: absolute;
        top: 18px;
        left: calc(100% - 40px);
        width: calc(100% - 56px);
        height: 2px;
        background: #d9d9d9;
        border-radius: 2px;
        z-index: 1;
        transition: background 0.2s;
    }
    .connector.complete {
        background: #25b740;
    }
    .step-container:last-child .connector {
        display: none;
    }
    @media (max-width: 600px) {
        .wizard-wrapper {
            width: 95vw;
            min-width: 0;
            max-width: 100vw;
            padding: 0 4vw;
        }
        .label {
            font-size: 0.9rem;
            width: 80px;
            min-height: 32px;
        }
    }
</style>