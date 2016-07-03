package com.wispy.wispy;

import com.google.gson.annotations.SerializedName;

/**
 * @author WispY
 */
public class SlackAttachment {
    private String fallback;
    private String color;
    private String pretext;
    @SerializedName("author_name")
    private String authorName;
    @SerializedName("author_link")
    private String authorLink;
    @SerializedName("author_icon")
    private String authorIcon;
    private String title;
    @SerializedName("title_link")
    private String titleLink;
    private String text;
    private String footer;
    @SerializedName("footer_icon")
    private String footerIcon;
    @SerializedName("ts")
    private Integer timestamp;

    public String getFallback() {
        return fallback;
    }

    public String getColor() {
        return color;
    }

    public String getPretext() {
        return pretext;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorLink() {
        return authorLink;
    }

    public String getAuthorIcon() {
        return authorIcon;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleLink() {
        return titleLink;
    }

    public String getText() {
        return text;
    }

    public String getFooter() {
        return footer;
    }

    public String getFooterIcon() {
        return footerIcon;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public SlackAttachment fallback(String fallback) {
        this.fallback = fallback;
        return this;
    }

    public SlackAttachment color(String color) {
        this.color = color;
        return this;
    }

    public SlackAttachment pretext(String pretext) {
        this.pretext = pretext;
        return this;
    }

    public SlackAttachment authorName(String authorName) {
        this.authorName = authorName;
        return this;
    }

    public SlackAttachment authorLink(String authorLink) {
        this.authorLink = authorLink;
        return this;
    }

    public SlackAttachment authorIcon(String authorIcon) {
        this.authorIcon = authorIcon;
        return this;
    }

    public SlackAttachment title(String title) {
        this.title = title;
        return this;
    }

    public SlackAttachment titleLink(String titleLink) {
        this.titleLink = titleLink;
        return this;
    }

    public SlackAttachment text(String text) {
        this.text = text;
        return this;
    }

    public SlackAttachment footer(String footer) {
        this.footer = footer;
        return this;
    }

    public SlackAttachment footerIcon(String footerIcon) {
        this.footerIcon = footerIcon;
        return this;
    }

    public SlackAttachment timestamp(Integer timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public static SlackAttachment attachment() {
        return new SlackAttachment();
    }
}