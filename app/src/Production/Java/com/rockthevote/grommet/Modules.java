package com.rockthevote.grommet;

import com.rockthevote.grommet.data.ProdDataModule;

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
