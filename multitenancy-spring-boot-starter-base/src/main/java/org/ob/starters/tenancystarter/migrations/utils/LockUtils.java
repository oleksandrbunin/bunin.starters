package org.ob.starters.tenancystarter.migrations.utils;

public class LockUtils {

    private static final int MASK = (1 << 31) - 1;

    public static int calculateLockId(String schema) {
        int hashCode = schema.hashCode();
        if (hashCode <= 0) {
            hashCode = (hashCode & MASK) + 1;
        }
        return hashCode;
    }
}
