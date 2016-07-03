package com.wispy.wispy;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Leonid_Poliakov
 */
public class SlackAnswer {
    private String text;
    private List<SlackAttachment> attachments;

    public SlackAnswer() {
        attachments = new LinkedList<>();
    }

    public String getText() {
        return text;
    }

    public List<SlackAttachment> getAttachments() {
        return attachments;
    }

    public SlackAnswer text(String text) {
        this.text = text;
        return this;
    }

    public SlackAnswer attach(SlackAttachment attachment) {
        attachments.add(attachment);
        return this;
    }

    public static SlackAnswer answer() {
        return new SlackAnswer();
    }

}