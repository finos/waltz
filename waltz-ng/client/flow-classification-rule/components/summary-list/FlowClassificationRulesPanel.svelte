<script>
    import FlowClassificationRulesTable from "../table/FlowClassificationRulesTable.svelte";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import {flowClassificationRuleStore} from "../../../svelte-stores/flow-classification-rule-store";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import _ from "lodash";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {userStore} from "../../../svelte-stores/user-store";
    import systemRoles from "../../../user/system-roles";
    import Toasts from "../../../svelte-stores/toast-store";
    import {displayError} from "../../../common/error-utils";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import FlowClassificationRuleEditor from "./FlowClassificationRuleEditor.svelte";
    import {toEntityRef} from "../../../common/entity-utils";
    import {messageSeverity} from "../../../common/services/enums/message-severity";
    import {settingsStore} from "../../../svelte-stores/settings-store";

    const PanelModes = {
        LIST: "LIST",
        SELECTION: "SELECTION",
        CREATE: "CREATE",
        BUSY: "BUSY"
    }

    const ActionModes = {
        LIST: "LIST",
        REMOVE: "REMOVE",
        EDIT: "EDIT",
        BUSY: "BUSY"
    };

    export let primaryEntityRef;

    const flowClassificationRuleViewOnlySettingKey = "feature.flow-classification-rules.view-only";

    let permissionsCall = userStore.load();
    let settingsCall = settingsStore.loadAll();
    let ruleViewCall;
    let rulesView;

    let actionMode = ActionModes.LIST;
    let panelMode = PanelModes.LIST;

    let selectionOpts = null;
    let hasEditPermissions = false;
    let selectedRule = null;

    let inboundFlowClassifications = [];
    let outboundFlowClassifications = [];
    let formData = mkEmptyFormData();

    function mkEmptyFormData() {
        return {
            severity: null,
            message: null,
            classification: null,
            description: null,
            id: null,
            dataType: null,
            subject: null,
            vantagePoint: null
        };
    }

    function showDetail(e) {
        selectedRule = e.detail;
        panelMode = PanelModes.SELECTION;
        actionMode = ActionModes.LIST; //ActionModes.LIST;
    }

    function clearSelected() {
        selectedRule = null;
        panelMode = PanelModes.LIST;
        actionMode = ActionModes.LIST;
    }

    function directionToStr(direction) {
        switch (direction) {
            case "INBOUND":
                return "Consumer";
            case "OUTBOUND":
                return "Producer";
            default:
                return "??" + direction;
        }
    }

    function oppositeDirectionToStr(direction) {
        switch (direction) {
            case "INBOUND":
                return "Producer";
            case "OUTBOUND":
                return "Consumer";
            default:
                return "??" + direction;
        }
    }

    function onSuccess(message) {
        Toasts.success(message);
        ruleViewCall = flowClassificationRuleStore.view(selectionOpts, true);
        clearSelected();
    }

    function onRemove() {
        actionMode = ActionModes.REMOVE;
    }

    function onEdit() {
        formData = Object.assign(
            mkEmptyFormData(),
            {
                id: selectedRule.id,
                description: selectedRule.description,
                message: selectedRule.message,
                classification: selectedRule.classification,
                severity: selectedRule.messageSeverity
            });
        actionMode = ActionModes.EDIT;
    }

    function onCreate() {
        formData = mkEmptyFormData();
        panelMode = PanelModes.CREATE;
        selectedRule = null;
    }

    function doRemoval() {
        actionMode = ActionModes.BUSY;
        flowClassificationRuleStore
            .remove(selectedRule.id)
            .then(() => onSuccess("Removed rule, flow ratings will be refreshed on the next recalculation run"))
            .catch(e => displayError("Failed to remove rule", e));
    }

    function doUpdate(e) {
        const form = e.detail;
        const cmd = {
            classificationId: form.classification.id,
            message: form.message,
            severity: form.severity,
            description: form.description,
            id: form.id
        };
        actionMode = ActionModes.BUSY;
        flowClassificationRuleStore
            .update(cmd)
            .then(() => onSuccess("Updated rule, flow ratings will be refreshed on the next recalculation run"))
            .catch(e => displayError("Failed to update rule", e))
    }

    function doCreate(e) {
        const form = e.detail;
        const cmd = {
            classificationId: form.classification.id,
            dataTypeId: form.dataType?.id,
            subjectReference: toEntityRef(form.subject),
            parentReference: toEntityRef(form.vantagePoint),
            message: form.message,
            severity: form.severity,
            description: form.description
        };
        panelMode = PanelModes.BUSY;
        flowClassificationRuleStore
            .create(cmd)
            .then(() => onSuccess("Created rule, flow ratings will be refreshed on the next recalculation run"))
            .catch(e => displayError("Failed to create rule", e))
    }

    $: selectionOpts = primaryEntityRef && mkSelectionOptions(primaryEntityRef);

    $: {
        if (selectionOpts) {
            ruleViewCall = flowClassificationRuleStore.view(selectionOpts);
        }
    }

    $: rulesView = $ruleViewCall?.data;
    $: permissions = $permissionsCall?.data;
    $: flowClassificationRuleEditableSetting = _
        .chain($settingsCall?.data)
        .filter(d => d.name === flowClassificationRuleViewOnlySettingKey)
        .map(d => d.value === "false")
        .value()[0];

    $: hasEditPermissions = flowClassificationRuleEditableSetting && _.includes(permissions?.roles, systemRoles.AUTHORITATIVE_SOURCE_EDITOR.key) || false;
    $: inboundFlowClassifications = _
        .chain(rulesView?.flowClassifications)
        .filter(d => d.direction === 'INBOUND')
        .orderBy(d => d.name)
        .value();
    $: outboundFlowClassifications = _
        .chain(rulesView?.flowClassifications)
        .filter(d => d.direction === 'OUTBOUND')
        .orderBy(d => d.name)
        .value();

</script>

<div class="container-fluid">
    <div class="row">
        <div class="col-md-8">
            <FlowClassificationRulesTable {rulesView}
                                          on:select={showDetail}/>
        </div>
        <div class="col-md-4 small">
            {#if panelMode === PanelModes.BUSY}
                <LoadingPlaceholder>
                    Working...
                </LoadingPlaceholder>
            {/if}  <!-- ActionModes.BUSY -->
            {#if panelMode === PanelModes.LIST}
                <div class="help-block">
                    Flow classifications allow data flows to be categorized against a set of rules.
                    Each rule consists of a
                        <em>Data Type</em>,
                        <em>Subject</em> (the source or target system),
                        <em>Scope</em> (the set of systems covered by this rule)
                        and a <em>Classification</em>.
                    The classification determines the <em>Direction</em> of the rule.
                    <em>Producer</em> classifications are used to define rules from the source systems point of view (e.g. system X is an authoritative source of data type Y for org unit Z).
                    <em>Consumer</em> classifications define rules from the consumers point of view (e.g.  system A discourages incoming flows of data type B for org unit C).
                </div>

                {#if hasEditPermissions}
                    <button class="btn btn-xs btn-primary"
                            data-testid="create-rule"
                            on:click={onCreate}>
                        <Icon name="plus"/>
                        Add new Flow Classification Rule
                    </button>
                {/if}

                <div class="legend help-block">
                    {#if outboundFlowClassifications}
                        <h5>Producer Classifications</h5>
                        <dl>
                            {#each outboundFlowClassifications as c}
                                <dt>
                                    <span class="indicator" style={`border-left-color: ${c.color}`}>
                                        {c.name}
                                    </span>
                                </dt>
                                <dd>{c.description}</dd>
                            {/each}
                        </dl>
                    {/if}
                    {#if inboundFlowClassifications}
                        <h5>Consumer Classifications</h5>
                        <dl>
                            {#each inboundFlowClassifications as c}
                                <dt>
                                    <span class="indicator" style={`border-left-color: ${c.color}`}>
                                        {c.name}
                                    </span>
                                </dt>
                                <dd>{c.description}</dd>
                            {/each}
                        </dl>
                    {/if}
                </div>
            {/if} <!-- PanelModes.LIST -->

            {#if panelMode === PanelModes.CREATE}
                <FlowClassificationRuleEditor {formData}
                                              on:save={doCreate}
                                              on:cancel={() => panelMode = PanelModes.LIST}
                                              {inboundFlowClassifications}
                                              {outboundFlowClassifications}/>
            {/if} <!-- PanelModes.CREATE -->

            {#if panelMode === PanelModes.SELECTION}
                <div data-testid="direction">
                    <strong>Direction:</strong>
                    {directionToStr(selectedRule.classification.direction)}
                </div>
                <p class="help-block">Indicates if this is an producer or consumer rule</p>

                <div data-testid="source">
                    <strong>Subject ({directionToStr(selectedRule.classification.direction)}):</strong>
                    <EntityLink ref={selectedRule.subjectReference}/>
                </div>
                <p class="help-block">The subject application, end user application, or actor for this rule</p>

                <div data-testid="data-type">
                    <strong>Data Type:</strong>
                    {#if selectedRule.dataType}
                        <EntityLink ref={selectedRule.dataType}/>
                    {:else}
                        <Icon name="asterisk"/> All
                    {/if}
                </div>
                <p class="help-block">
                    The data type/s this application / actor will provide a flow classification for.
                    This will apply to all sub-classes unless overridden by another rule.
                </p>

                <div data-testid="scope">
                    <strong>Scope ({oppositeDirectionToStr(selectedRule.classification.direction)}/s)::</strong>
                    <EntityLink ref={selectedRule.vantagePointReference}/>
                </div>
                <p class="help-block">This specifies either a single node (app, actor, or end user app) or a group of nodes which this classification rule applies to</p>

                <div data-testid="classification">
                    <strong>Classification:</strong>
                    <span class="indicator"
                          style={`border-left-color: ${_.get(selectedRule, 'classification.color', '#ccc')}`}>
                        {_.get(selectedRule, ['classification', 'name'], '-')}
                    </span>
                    <p class="help-block">{_.get(selectedRule, ['classification', 'description'], '-')}</p>
                </div>

                {#if !_.isEmpty(selectedRule.message)}

                    <div data-testid="message-severity">
                        <strong>Message Severity:</strong>
                        <span>
                            {messageSeverity[_.get(selectedRule, ['messageSeverity'], 'NONE')].name}
                        </span>
                        <p class="help-block">The severity is used to decorate the message</p>
                    </div>

                    <div data-testid="message">
                        <strong>Message:</strong>
                        <span>
                            {_.get(selectedRule, ['message'], '-')}
                        </span>
                        <p class="help-block">An optional message to give the user when the rule matches</p>
                    </div>

                {/if}

                <div data-testid="notes">
                    <strong>Notes:</strong>
                    <span>{selectedRule.description || "None provided"}</span>
                    <p class="help-block">Additional notes</p>
                </div>

                {#if hasEditPermissions && selectedRule.classification.userSelectable}
                    <details>
                        <summary>Actions</summary>
                        {#if actionMode === ActionModes.LIST}
                            <menu>
                                <li>
                                    <button class="btn btn-xs btn-primary"
                                            on:click={onEdit}>
                                        Edit
                                    </button>
                                    <div class="help-block">
                                        Update the classification of this rule
                                    </div>
                                </li>
                                <li>
                                    <button class="btn btn-xs btn-danger"
                                            on:click={onRemove}>
                                        Remove
                                    </button>
                                    <div class="help-block">
                                        Remove this flow classification rule
                                    </div>
                                </li>
                            </menu>
                        {/if} <!-- ActionModes.LIST -->

                        {#if actionMode === ActionModes.REMOVE}
                            <div class="removal-box">
                                <h4>Confirm Removal</h4>
                                <p class="help-block">
                                    Remove this flow classification rule.
                                    Flow ratings will be refreshed on the next recalculation run.
                                </p>
                                <button class="btn btn-xs btn-warning"
                                        on:click={doRemoval}>
                                    Remove
                                </button>
                                <button class="btn btn-xs btn-primary"
                                        on:click={() => actionMode = ActionModes.LIST}>
                                    Cancel
                                </button>
                            </div>
                        {/if} <!-- ActionModes.REMOVE -->

                        {#if actionMode === ActionModes.EDIT}
                            <p class="help-block">
                                You may change the current classification.
                                You may also configure a message (with severity) to display when users select any matching data type.
                            </p>

                            <FlowClassificationRuleEditor {formData}
                                                          on:save={doUpdate}
                                                          on:cancel={() => actionMode = ActionModes.LIST}
                                                          {inboundFlowClassifications}
                                                          {outboundFlowClassifications}/>
                        {/if} <!-- ActionModes.EDIT -->

                        {#if actionMode === ActionModes.BUSY}
                            <LoadingPlaceholder>
                                Working...
                            </LoadingPlaceholder>
                        {/if}  <!-- ActionModes.BUSY -->
                    </details>
                {/if}  <!-- Actions -->

                <div>
                    <ViewLink state="main.flow-classification-rule.view"
                              ctx={{id: selectedRule.id}}
                              title="View the flow classification rule page">
                        View page
                    </ViewLink>

                    |

                    <button class="btn-skinny"
                            on:click={clearSelected}>
                        Close
                    </button>

                </div>
            {/if}  <!-- PanelModes.SELECTION -->
        </div>
    </div>

</div>

<style>
    menu {
        padding-left: 1em;
    }

    menu li {
        list-style: none;
    }

    .removal-box{
        border-width: 1px;
        border-style: solid;
        border-color: #d93f44;
        background-color: #fae9ee;
        padding-left: 2em;
        padding-right: 2em;
        padding-bottom: 1.5em;
        padding-top: 1.5em;
        border-radius: 2px;
    }

    .indicator {
        border-left-width: 2px;
        border-left-style: solid;
        padding-left: 0.2em;
    }
</style>