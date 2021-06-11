<script>
    import {toGraphId} from "../../../flow-diagram-utils";
    import {createEventDispatcher} from "svelte";
    import model from "../store/model";
    import {positions} from "../store/layout";

    export let selected;
    const dispatch = createEventDispatcher();

    let note = selected.note || "";

    function mkNewAnnotation(selected) {
        return Object.assign(
            {},
            {
                note,
                id: +new Date() + '',
                kind: 'ANNOTATION',
                entityReference: selected
            });
    }

    function saveAnnotation() {
        const commands = [];

        if (selected.kind === 'ANNOTATION') {
            model.updateAnnotation({id: toGraphId(selected), note: note})

        } else {
            const payload = mkNewAnnotation(selected);
            model.addAnnotation({id: toGraphId(payload), data: payload})
            positions.move({
                    id: toGraphId(payload),
                    dx: 30,
                    dy: 30
            })
        }
        cancel();
    }

    function cancel(){
        dispatch("cancel");
    }

</script>

<div>
    <p>Annotation text:</p>
    <textarea id="description"
              class="form-control"
              rows="4"
              style="width: 100%"
              required="required"
              bind:value={note}/>
</div>
<span>
    <button class="btn btn-skinny" on:click={cancel}>
        Cancel
    </button>
    |
    <button class="btn btn-skinny" on:click={saveAnnotation}>
        Update text
    </button>
</span>
<style>
</style>