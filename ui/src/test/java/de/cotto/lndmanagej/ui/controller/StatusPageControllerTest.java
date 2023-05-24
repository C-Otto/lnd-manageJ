package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.ui.StatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusPageControllerTest {

    @InjectMocks
    private StatusPageController statusPageController;

    @Mock
    private StatusService statusService;

    @Mock
    private Model model;

    @Test
    void status() {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL);
        assertThat(statusPageController.status(model)).isEqualTo("status-page");
        verify(model).addAttribute("status", STATUS_MODEL);
    }
}
