package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL;
import static org.mockito.Mockito.when;

public class BaseControllerIT {

    @MockBean
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private StatusService statusService;

    @BeforeEach
    void beforeEach() {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL);
    }

    public ChannelIdResolver getChannelIdResolverMockBean() {
        return channelIdResolver;
    }
}
