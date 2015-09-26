package com.poco.PoCoRuntime;

import java.util.ArrayList;

public class FirstApplicable implements ICombinatorLogic {
    @Override
    public SRE performLogic(ArrayList<SRE> sres) {
        if (sres == null || sres.size() == 0)
            return null;
        else
            return sres.get(0);
    }
}