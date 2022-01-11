package org.finos.waltz.service.workflow.side_effect;

public class TestSideEffectNamespace {

    String lastMessage;

    public void sendMessage(String message) {
        lastMessage = message;
    }
}
