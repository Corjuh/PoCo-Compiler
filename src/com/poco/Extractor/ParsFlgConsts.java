package com.poco.Extractor;

/**
 * Created by yan on 5/10/15.
 */
public final class ParsFlgConsts {
    public static final int parsArgs = 0;
    public static final int clousrFunc = 1;
    public static final int closurFunwUop = 2;
    public static final int isAction =3;
    public static final int isResult =4;

    public static final int hasAsterisk = 5;
    public static final int hasPlus = 6;
    public static final int hasNone = 7;


    public static final int isMapSre = 11;
    public static final int isExchLHSWild = 12; //the case when exec's LHS is wildCard
    public static final int isExchRHSSre = 13; //the sre of the exch
    public static final int isExchMatch = 14;

    public static final int isReBop = 21;
    public static final int isIREAction = 22;
    public static final int isIREResult = 23;
    public static final int isIREResMatch = 24;
    public static final int isSrePosRE = 25;
    public static final int isSreNegRE = 26;



    public static final int isSreBop1 = 31;
    public static final int isSreBop2 = 32;

    private ParsFlgConsts(){
        throw new AssertionError();
    }
}
