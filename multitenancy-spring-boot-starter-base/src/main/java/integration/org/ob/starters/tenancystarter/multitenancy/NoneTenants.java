package integration.org.ob.starters.tenancystarter.multitenancy;

public class NoneTenants extends MultitenancyConditional {

    @Override
    protected Multitenancy getMultitenancy() {
        return Multitenancy.NONE;
    }
}
