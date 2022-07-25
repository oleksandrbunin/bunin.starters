package org.ob.starters.tenancystarter.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class AlwaysTrueTest {

    @Test
    void alwaysTrue() {
        assertThat(1 + 1).isEqualTo(2);
    }

}
