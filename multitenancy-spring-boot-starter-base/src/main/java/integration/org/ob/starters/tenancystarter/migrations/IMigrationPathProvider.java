package integration.org.ob.starters.tenancystarter.migrations;

import java.net.URI;
import java.util.List;

public interface IMigrationPathProvider {

    List<URI> tenantsMigrationsPaths();

    List<URI> defaultMigrationsPaths();

}
