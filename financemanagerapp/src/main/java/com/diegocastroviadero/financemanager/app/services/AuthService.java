package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.views.common.AuthDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.VaadinSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
public class AuthService {
    public static final String AUTHPASSWORD_ENTRY_KEY = "password";

    private final Map<Class<? extends Component>, AuthDialog> map = new HashMap<>();

    public AuthDialog configureAuth(final Component component) {
        final AuthDialog authDialog = new AuthDialog();
        map.put(component.getClass(), authDialog);

        return authDialog;
    }

    public void authenticate(final Component component, final Consumer<char[]> listener) {
        char[] pass = getAuthPassword();

        if (pass != null) {
            listener.accept(pass);
        } else {
            if (map.containsKey(component.getClass())) {
                final AuthDialog authDialog = map.get(component.getClass());

                authDialog.setOnClosedListener(password -> {
                    final char[] p = password.toCharArray();

                    setAuthPassword(p);
                    listener.accept(p);
                });

                authDialog.open();
            } else {
                // TODO: manage this case in a better way
                throw new RuntimeException(String.format("AuthService was not configured for %s", component.getClass().getSimpleName()));
            }
        }
    }

    public boolean forgetPassword() {
        return forgetPassword(VaadinSession.getCurrent());
    }

    public boolean forgetPassword(final VaadinSession session) {
        final boolean passwordWillBeCleaned = null != session.getAttribute(AUTHPASSWORD_ENTRY_KEY);

        session.setAttribute(AUTHPASSWORD_ENTRY_KEY, null);

        return passwordWillBeCleaned;
    }

    private char[] getAuthPassword() {
        return (char[]) VaadinSession.getCurrent().getAttribute(AUTHPASSWORD_ENTRY_KEY);
    }

    private void setAuthPassword(final char[] password) {
        VaadinSession.getCurrent().setAttribute(AUTHPASSWORD_ENTRY_KEY, password);
    }
}
