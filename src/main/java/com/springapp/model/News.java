package com.springapp.model;

import io.searchbox.annotations.JestId;

/**
 * Created by xinhuan on 2016/1/18.
 */
public class News {
    @JestId
    private int id;
    private String title;
    private String content;
    private String createTime;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
