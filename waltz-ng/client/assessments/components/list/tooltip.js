import tippy from "tippy.js";

export function tooltip(elem) {
    if (elem) {
        tippy(elem, {
            content: "Hello world!",
            arrow: true,
            interactive: true,
            trigger: 'mouseenter click'
        });
    }
}