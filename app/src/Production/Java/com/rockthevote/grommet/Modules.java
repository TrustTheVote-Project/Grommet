package com.rockthevote.grommet;

final class Modules {
    static Object[] list(GrommetApp app) {
        return new Object[]{
                new GrommetModule(app)
        };
    }

    private Modules() {
        // No instances.
    }
}
