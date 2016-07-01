package com.wispy.wispy;

import java.util.List;

/**
 * @author Leonid_Poliakov
 */
public class SlackAnswer {
    private String text;
    private List<SlackAttachment> attachments;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<SlackAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<SlackAttachment> attachments) {
        this.attachments = attachments;
    }
}