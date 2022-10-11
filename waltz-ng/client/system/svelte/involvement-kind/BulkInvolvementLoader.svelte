<script>

    import {entity} from "../../../common/services/enums/entity";
    import DropdownPicker from "../../../common/svelte/DropdownPicker.svelte";
    import {rawInvolvements, selectedKind, resolvedRows, resolveParams} from "./bulk-invovlement-loader-store";
    import _ from "lodash";
    import {mkRef} from "../../../common/entity-utils";
    import {bulkUploadStore} from "../../../svelte-stores/bulk-upload-store";
    import {displayError} from "../../../common/error-utils";

    export let involvementKind;

    let resolveCall;

    const items = [
        entity.APPLICATION,
        entity.CHANGE_INITIATIVE,
        entity.ORG_UNIT];

    function selectEntityKind(kind) {
        console.log({kind});
        $selectedKind = kind.key;
    }

    function verifyEntries() {
        console.log({re: $rawInvolvements});

        const resolveParams = {
            inputString: $rawInvolvements,
            targetDomain: mkRef(involvementKind.kind, involvementKind.id),
            rowSubjectKind: $selectedKind
        }

        return resolveCall = bulkUploadStore.resolve(resolveParams)
            .then(d => {
                $resolvedRows = d.data;
                console.log({d});
                console.log("done loading");
            })
            .catch(e => displayError("Could not resolve rows", e));
    }


    $: console.log({resolvedRows: $resolvedRows});


</script>

<DropdownPicker {items}
                onSelect={selectEntityKind}
                defaultMessage="Select an entity kind"/>


<div style="padding-top: 1em">
    Use the text box below to provide involvements as comma or tab separated value
    e.g. external identifier, email.
</div>

<form on:submit|preventDefault={verifyEntries}>
    <div class="form-group">
        <label for="involvements">
            Involvements
        </label>
        <textarea id="involvements"
                  class="form-control"
                  rows="6"
                  placeholder="Please insert involvement external identifiers and email as comma or tab separated values split by newline or pipe characters"
                  bind:value={$rawInvolvements}></textarea>
    </div>
    <button type="submit"
            class="btn btn-success"
            disabled={_.isEmpty($rawInvolvements) || _.isNull($selectedKind)}>
        Search
    </button>
</form>
