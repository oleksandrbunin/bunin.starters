package integration.org.ob.starters.tenancystarter.multitenancy.exceptions;

public class TenantCreationException extends RuntimeException {
    public TenantCreationException(String message, Throwable e) {
        super(message, e);
    }
}
