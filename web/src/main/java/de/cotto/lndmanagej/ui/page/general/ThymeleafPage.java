package de.cotto.lndmanagej.ui.page.general;

import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

public abstract class ThymeleafPage {

    Map<String, Object> modelAttributes = new HashMap<>();

    public abstract String getView();

    public Map<String, Object> getModelAttributes() {
        return modelAttributes;
    }

    protected void add(String attributeName, Object data) {
        modelAttributes.put(attributeName, data);
    }

    public String create(Model model) {
        model.addAllAttributes(getModelAttributes());
        return getView();
    }
}
