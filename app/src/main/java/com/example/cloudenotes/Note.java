package com.example.cloudenotes;

public class Note {

    private String content;

    private String title;

    public Note(String content, String title) {
        this.content = content;
        this.title = title;
    }

    public Note(String title) {
        this.title = title;
    }

    public Note() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
