package org.ob.starters.tenancystarter.migrations.utils;

public class LockUtils {
    public static int calculateLockId(String schema) {
        int hashCode = schema.hashCode();
        if (hashCode <= 0) {
            hashCode = (hashCode * (-1)) << 1 + 1;
        }
        return hashCode;
    }
}
