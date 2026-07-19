/*
 */
package dev.omega24.agricrops.core;

import com.agricrops.agricore.util.AgriConverter;
import com.agricrops.agricore.util.TypeHelper;
import dev.omega24.agricrops.api.v1.util.FuzzyStack;
import dev.omega24.agricrops.utility.OreDictUtil;
import java.util.List;
import java.util.Optional;
/**
 *
 *
 */
public class ModConverter implements AgriConverter {

    @Override
    public <T> Optional<T> toStack(Class<T> token, String element, int meta, int amount, String tags, boolean ignoreMeta, boolean useOreDict, List<String> ignoreTags) {
        return Optional.ofNullable(token)
                .filter(type -> TypeHelper.isType(FuzzyStack.class, type))
                .flatMap(type -> OreDictUtil.makeItemStack(element, meta, amount, tags))
                .map(stack -> (T) new FuzzyStack(stack, ignoreMeta, useOreDict, ignoreTags));
    }
    
}
