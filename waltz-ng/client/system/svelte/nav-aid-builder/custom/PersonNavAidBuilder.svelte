<script>
    import {model, RenderModes, renderMode} from "./builderStore";
    import BuilderControl from "./control/BuilderControl.svelte";
    import PersonNavAid from "./PersonNavAid.svelte";
    import Toggle from "../../../../common/svelte/Toggle.svelte";

    let vizElem = null;
    let html = "";

    function prettyHTML(elemHtml) {
        if (_.isNil(elemHtml)) {
            return "";
        }
        const tab = "    ";
        let result = "";
        let indent= "";

        elemHtml
            .split(/>\s*</)
            .forEach(function(element) {
                if (element.match( /^\/\w/ )) {
                    indent = indent.substring(tab.length);
                }

                result += indent + "<" + element + ">\r\n";

                if (element.match( /^<?\w[^>]*[^\/]$/ ) && !element.startsWith("input")  ) {
                    indent += tab;
                }
            });

        return result.substring(1, result.length - 3);
    }

    $: {
        if ($model && vizElem && $renderMode) {
            // using a timeout to give innerHTML chance to be updated
            // before we copy it
            setTimeout(() => html = prettyHTML(vizElem.innerHTML));
        }
    }

</script>


<div class="row">
    <div class="col-sm-8">
        <div bind:this={vizElem}>
            <PersonNavAid/>
        </div>
    </div>
    <div class="col-sm-4"
         style="padding-left: 10px">
        <BuilderControl/>
    </div>
</div>

<hr>

<div class="row">
    <div class="col-md-6">
        <label for="render-mode-toggle">
            Render Mode
        </label>
        <div id="render-mode-toggle">
            <Toggle id="foo"
                    state={$renderMode === RenderModes.LIVE}
                    labelOn="Live Mode (with links)"
                    labelOff="Dev Mode (without links)"
                    onToggle={() => renderMode.toggle()}/>
            <div class="help-block">
                Render mode determines whether to have clickable
                regions being actual links, or used to focus
            </div>
        </div>

        <pre>{html}</pre>
    </div>
    <div class="col-md-6">
        <pre>{JSON.stringify($model, "", 2)}</pre>
    </div>
</div>
