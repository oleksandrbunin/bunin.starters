package org.ob.starters.parent;

import org.junit.jupiter.api.Test;
import org.ob.starters.UtilsTestClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HelloTest {

    @Autowired
    UtilsTestClass utilsTestClass;

    @Test
    void helloTest() {
        System.out.println("Hello");
    }

    @Test
    void contextTest() {
        assertThat(utilsTestClass.isTestContext()).isTrue();
    }

}
