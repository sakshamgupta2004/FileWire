package org.swiftp;

public class QuotaStats {
    private int quota;
    private int used;

    public QuotaStats(int used2, int quota2) {
        this.quota = quota2;
        this.used = used2;
    }

    public int getQuota() {
        return this.quota;
    }

    public int getUsed() {
        return this.used;
    }
}
