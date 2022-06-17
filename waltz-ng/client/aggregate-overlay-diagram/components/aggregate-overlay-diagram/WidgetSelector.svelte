<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import {getContext} from "svelte";
    import _ from "lodash";
    import {widgets} from "./aggregate-overlay-diagram-utils";
    import {settingsStore} from "../../../svelte-stores/settings-store";

    let displayedWidgets;

    const selectedOverlay = getContext("selectedOverlay");
    const selectedDiagram = getContext("selectedDiagram");
    const focusWidget = getContext("focusWidget");
    const filterParameters = getContext("filterParameters");
    const widgetParameters = getContext("widgetParameters");
    const diagramPresets = getContext("diagramPresets");
    const disabledWidgetKeys = getContext("disabledWidgetKeys");

    let settingsCall = settingsStore.loadAll();
    let disabledWidgetsSetting;

    $: displayedWidgets = _.filter(
        widgets,
        d => {
            const allowedKindForParentEntity = _.includes(d.aggregatedEntityKinds, $selectedDiagram.aggregatedEntityKind);
            const notDisabled = !_.some($disabledWidgetKeys, k => k === d.key);
            return allowedKindForParentEntity && notDisabled;
        });

    function onCancel() {
        $focusWidget = null;
        $selectedOverlay = null;
    }

    function selectWidget(widget) {
        $widgetParameters = null;
        $focusWidget = widget;
    }

</script>


{#if $focusWidget}
    <h4>
        <Icon name={$focusWidget.icon}/>
        {$focusWidget.label}
        <button class="small btn btn-skinny"
                on:click={onCancel}>
            (Change overlay)
        </button>
    </h4>

    <svelte:component this={$focusWidget.parameterWidget}/>

{:else}
    <div class="help-block">
        <Icon name="info-circle"/>
        Select an overlay from the list below
    </div>

    <table class="table table-condensed small table-striped">
        <tbody>
        {#each displayedWidgets as widget}
            <tr>
                <td>
                    <button class="btn btn-skinny"
                            on:click={() => selectWidget(widget)}>
                        {widget.label}
                    </button>
                </td>
                <td>{widget.description}</td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}


