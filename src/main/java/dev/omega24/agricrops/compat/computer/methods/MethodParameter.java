package dev.omega24.agricrops.compat.computer.methods;

import com.agricraft.agricore.core.AgriCore;

public class MethodParameter {

    public static final MethodParameter DIRECTION = new MethodParameter("direction");
    public static final MethodParameter DIRECTION_OPTIONAL = new MethodParameter("direction.optional");

    private String name;

    private MethodParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return AgriCore.getTranslator().translate("agricrops_arg." + name);
    }

    public String getDescription() {
        return AgriCore.getTranslator().translate("agricrops_description.parameter." + name);
    }
}
