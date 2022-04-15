package com.rockthevote.grommet;

final class Modules {
    static Object[] list(GrommetApp app) {
        return new Object[]{
                new GrommetModule(app),
                new InternalReleaseGrommetModule()
        };
    }

    private Modules() {
        // No instances.
    }
}