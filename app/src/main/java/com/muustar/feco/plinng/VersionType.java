package com.muustar.feco.plinng;

/**
 * Készítette: feco
 * 2018.06.09.
 */
public class VersionType {
    private int version;
    private long release_date;
    private String text;

    public  VersionType(){}

    public VersionType(int version, long release_date, String text) {
        this.version = version;
        this.release_date = release_date;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getRelease_date() {
        return release_date;
    }

    public void setRelease_date(long release_date) {
        this.release_date = release_date;
    }
}
