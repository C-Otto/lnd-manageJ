package de.cotto.lndmanagej.ui.page.general;

public class ErrorPage extends ThymeleafPage {

    public ErrorPage(String message) {
        super();
        add("error", message);
    }

    @Override
    public String getView() {
        return "error";
    }
}
