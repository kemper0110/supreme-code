package org.supremecode.testrunner;

import org.supremecode.pluginsdk.LanguageTester;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class LanguagePluginService {
    Map<String, LanguageTester> map = new HashMap<>();

    LanguagePluginService() {
        ServiceLoader<LanguageTester> loader = ServiceLoader.load(LanguageTester.class);

        for (LanguageTester service : loader) {
            map.put(service.languageId(), service);
        }
    }

    LanguageTester getLanguageTester(String languageId) {
        return map.get(languageId);
    }
}
