package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL;
import static org.mockito.Mockito.when;

public class BaseControllerIT {

    @MockitoBean
    private ChannelIdResolver channelIdResolver;

    @MockitoBean
    private StatusService statusService;

    @BeforeEach
    void beforeEach() {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL);
    }

    public ChannelIdResolver getChannelIdResolverMockitoBean() {
        return channelIdResolver;
    }
}
