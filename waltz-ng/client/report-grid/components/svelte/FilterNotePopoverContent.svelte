<script>
    import Markdown from "../../../common/svelte/Markdown.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {entity} from "../../../common/services/enums/entity";
    import {copyTextToClipboard} from "../../../common/browser-utils";
    import toasts from "../../../svelte-stores/toast-store";
    import {displayError} from "../../../common/error-utils";

    export let primaryEntityRef;
    export let grid;
    export let filters;

    function copyText() {
        return copyTextToClipboard(noteContent)
            .then(() => toasts.success("Copied note to clipboard"))
            .catch(e => displayError("Could not copy note to clipboard", e));
    }

    function toName(col) {
        return col?.columnName || col?.displayName;
    }

    let reloadLinkContent = "\n\n\nThis group will refresh overnight, <a href=\"./page/report-grid-view/recalculate/app-group-id/${ctx.ref.id}?sections=10\" target=\"_blank\">click here to refresh group now</a>"

    $: allColDefinitions = _.concat(grid.definition.fixedColumnDefinitions, grid.definition.derivedColumnDefinitions);
    $: gridColumnsById = _.keyBy(allColDefinitions, d => d.gridColumnId);
    $: ratingSchemeItemsById = _.keyBy(grid.instance.ratingSchemeItems, d => d.id);
    $: groupedFilters = _.groupBy(filters, d => d.columnDefinitionId);
    $: ratingColumnKinds = [entity.MEASURABLE.key, entity.ASSESSMENT_DEFINITION.key];
    $: noteContent = _.join([gridContent, filterContent, reloadLinkContent], "");

    $: gridContent = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |\n" +
        "| --- | --- | --- | --- |\n" +
        `| \`${grid.definition.name}\` | \`${grid.definition.externalId}\` | \`${primaryEntityRef.kind}\` | \`${primaryEntityRef.id}\` |\n` +
        "\n" +
        "\n" +
        "| Filter Column | Filter Operator | Value/s |\n" +
        "| --- | --- | --- |\n";

    $: filterContent = _
        .chain(groupedFilters)
        .map((v, k) => {

            const colId = _.toNumber(k);
            const gridColumnDef = gridColumnsById[colId];

            const optionCodes = _.chain(v)
                .map(d => {
                    if (_.isUndefined(d.optionCode)) {
                        return `\`NOT_PROVIDED\``
                    } else if (_.includes(ratingColumnKinds, gridColumnDef?.columnEntityKind)) {
                        const ratingSchemeItem = ratingSchemeItemsById[d.optionCode];
                        return `\`${ratingSchemeItem?.rating}\``;
                    } else {
                        return `\`${d.optionCode}\``;
                    }
                })
                .join("; ")
                .value();

            return `| \`${toName(gridColumnDef)}\` | \`CONTAINS_ANY_OPTION\` | ${optionCodes} |`
        })
        .join("\n")
        .value();


</script>

<div class="intro pull-right">
    <button class="btn btn-default"
            on:click={copyText}>
        <Icon name="clone"/>
        Copy Note Content
    </button>
</div>

<div class="code">
    <textarea class="form-control"
              id="content"
              placeholder="Note content"
              disabled={true}
              rows="10"
              bind:value={noteContent}/>
    <div class="help-block">
        <Icon name="info-circle"/>
        Content to copy and paste into the 'Notes' section of your application group.
    </div>
</div>

<hr>

<div class="preview">
    <Markdown class="force-wrap" text={noteContent}/>
    <div class="help-block">
        <Icon name="info-circle"/>
        Preview of the formatted note once saved
    </div>
</div>


<style>
    .intro {
        padding: 0.5em 0;
    }

    .code {
        padding: 0.5em 0;
    }

    .preview {
        padding: 0.5em 0;
    }

    textarea {
        font-family: monospace;
        font-size: smaller;
    }
</style>