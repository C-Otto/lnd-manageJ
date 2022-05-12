package de.cotto.lndmanage.ui.demo;

import de.cotto.lndmanagej.ui.demo.DemoApplication;
import de.cotto.lndmanagej.ui.demo.data.DemoDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DemoApplication.class)
class DemoApplicationContextTest {

    @Autowired
    private DemoDataService demoDataService;

    @Test
    void contextStarts() {
        assertThat(demoDataService).isNotNull();
    }

}