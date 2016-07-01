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
}