/*
 */
package dev.omega24.agricrops.core;

import com.agricraft.agricore.lang.AgriTranslationAdapter;
import dev.omega24.agricrops.AgriCraft;

/**
 *
 *
 */
public class ModTranslator implements AgriTranslationAdapter {

    @Override
    public String translateKey(String key) {
        return AgriCraft.proxy.translateToLocal(key);
    }

    @Override
    public String getLocale() {
        return AgriCraft.proxy.getLocale();
    }

}
