package org.ob.starters.tenancystarter.migrations;

import java.util.concurrent.locks.Lock;

public interface AutoCloseableLock extends Lock, AutoCloseable {

}
