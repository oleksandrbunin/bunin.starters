package integration.org.ob.starters.tenancystarter.multitenancy;

public class SharedSchema extends MultitenancyConditional {

    @Override
    protected Multitenancy getMultitenancy() {
        return Multitenancy.SHARED_SCHEMA;
    }
}
