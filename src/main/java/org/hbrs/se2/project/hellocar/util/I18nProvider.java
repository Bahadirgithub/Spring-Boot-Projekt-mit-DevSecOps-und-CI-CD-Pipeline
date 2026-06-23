package org.hbrs.se2.project.hellocar.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class I18nProvider implements com.vaadin.flow.i18n.I18NProvider {

    private final MessageSource messageSource;

    public I18nProvider(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public List<Locale> getProvidedLocales() {
        // Definieren Sie hier alle unterstützten Sprachen
        return List.of(Locale.GERMAN, Locale.ENGLISH);
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        // Falls kein Locale übergeben wird, nutzen Sie den Spring-Kontext oder das Standard-Locale
        Locale targetLocale = (locale != null) ? this.getProvidedLocales().get(0) : LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(key, params, targetLocale);
        } catch (Exception e) {
            return "!" + key + "!"; // Fallback, falls der Key fehlt
        }
    }
}
