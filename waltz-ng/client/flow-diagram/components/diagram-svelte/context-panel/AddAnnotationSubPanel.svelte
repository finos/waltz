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

<div class="context-panel-footer">
    <button class="btn btn-skinny" on:click={saveAnnotation}>
        Update
    </button>
    |
    <button class="btn btn-skinny" on:click={cancel}>
        Cancel
    </button>
</div>


<style>
    .context-panel-footer {
        border-top: 1px solid #eee;
        margin-top:0.5em;
        padding-top:0.5em;
    }
</style>