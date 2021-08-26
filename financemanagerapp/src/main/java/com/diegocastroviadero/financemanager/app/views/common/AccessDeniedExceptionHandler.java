package com.diegocastroviadero.financemanager.app.views.common;

import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.ParentLayout;

import javax.servlet.http.HttpServletResponse;

@Tag(Tag.DIV)
@ParentLayout(MainView.class)
public class AccessDeniedExceptionHandler extends Component implements HasErrorParameter<AccessDeniedException> {

    @Override
    public int setErrorParameter(final BeforeEnterEvent event, final ErrorParameter<AccessDeniedException> parameter) {
        getElement()
                .setText("Tried to navigate to a view without required access rights");

        getElement()
                .getStyle().set("padding", "var(--lumo-space-m)");

        return HttpServletResponse.SC_FORBIDDEN;
    }
}
