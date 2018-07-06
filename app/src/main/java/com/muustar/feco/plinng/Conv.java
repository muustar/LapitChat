package com.muustar.feco.plinng;

/**
 * Készítette: feco
 * 2018.05.13.
 */
public class Conv {
    private Boolean seen;
    private long timestemp;

    public Conv(){}

    public Conv(Boolean seen, long timestemp) {
        this.seen = seen;
        this.timestemp = timestemp;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public long getTimestemp() {
        return timestemp;
    }

    public void setTimestemp(long timestemp) {
        this.timestemp = timestemp;
    }
}
