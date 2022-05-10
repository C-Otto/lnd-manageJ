package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.general.ErrorPage;
import de.cotto.lndmanagej.ui.page.node.NodeDetailsPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.NoSuchElementException;

import static de.cotto.lndmanagej.controller.dto.NodeDetailsDtoFixture.NODE_DETAILS_DTO;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeDetailsControllerTest {
    @InjectMocks
    private NodeDetailsController nodeDetailsController;

    @Mock
    private PageService pageService;

    @Mock
    private Model model;

    @Test
    void nodeDetails() {
        when(pageService.nodeDetails(PUBKEY)).thenReturn(new NodeDetailsPage(NODE_DETAILS_DTO));
        assertThat(nodeDetailsController.nodeDetails(PUBKEY, model)).isEqualTo("node-details");
    }

    @Test
    void nodeDetails_no_such_element() {
        when(pageService.nodeDetails(PUBKEY)).thenThrow(NoSuchElementException.class);
        when(pageService.error("Node not found.")).thenReturn(new ErrorPage("x"));
        assertThat(nodeDetailsController.nodeDetails(PUBKEY, model)).isEqualTo("error");
    }

    @Test
    void nodeDetails_illegal_argument() {
        when(pageService.nodeDetails(PUBKEY)).thenThrow(IllegalArgumentException.class);
        when(pageService.error("Invalid public key.")).thenReturn(new ErrorPage("x"));
        assertThat(nodeDetailsController.nodeDetails(PUBKEY, model)).isEqualTo("error");
    }
}
