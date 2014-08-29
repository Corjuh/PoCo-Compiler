package com.poco.RuntimeTest;


import com.poco.PoCoRuntime.Event;
import com.poco.PoCoRuntime.Match;
import com.poco.PoCoRuntime.Matchs;
import com.poco.PoCoRuntime.PoCoException;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class TestMatchs {
    private Matchs matchs;

    @Before
    public void setup() throws PoCoException {
        matchs = new Matchs();
    }

    @Test(expected = PoCoException.class)
    public void constructorVerifiesModifier() throws PoCoException{
        Matchs badMatchs1 = new Matchs("@");
        Matchs badMatchs2 = new Matchs(null);
    }

    @Test
    public void constructorInterpretsModifier() throws PoCoException {
        Matchs andMatchs = new Matchs("&&");
        Matchs orMatchs = new Matchs("||");
        Matchs notMatchs = new Matchs("!");

        assertTrue(andMatchs.isAND());
        assertFalse(andMatchs.isNOT() || andMatchs.isOR());

        assertTrue(orMatchs.isOR());
        assertFalse(orMatchs.isAND() || orMatchs.isNOT());

        assertTrue(notMatchs.isNOT());
        assertFalse(notMatchs.isAND() || notMatchs.isOR());
    }

    @Test
    public void notMatchsCorrectlyNegatesResults() throws PoCoException {
        matchs.setOperator("!");

        assertTrue("NOT matchs is returning false for isNOT()", matchs.isNOT());
        assertFalse("NOT matchs is returning true for isAND() or isOR()", matchs.isAND() || matchs.isOR());

        Match match = new Match("something");
        matchs.addChild(match);

        Event matchingEvent = new Event("something");
        Event wrongEvent = new Event("bucket");

        assertFalse("NOT Matchs accepted when its child did", matchs.accepts(matchingEvent));
        assertTrue("NOT Matchs did not accept when its child did not accept", matchs.accepts(wrongEvent));
    }

    @Test
    public void andMatchsBehavesCorrectly() throws PoCoException {
        matchs.setOperator("&&");

        assertTrue("AND matchs is returning false for isAND()", matchs.isAND());
        assertFalse("AND matchs is returning true for isNOT() or isOR()", matchs.isNOT() || matchs.isOR());

        Match cakeMatch = new Match("cake");
        Match tacoMatch = new Match("taco");

        matchs.addChild(cakeMatch);
        matchs.addChild(tacoMatch);

        Event cakeEvent = new Event("cake");
        Event tacoEvent = new Event("taco");

        assertFalse(matchs.accepts(cakeEvent));
        assertFalse(matchs.accepts(tacoEvent));

        Event combinedEvent = new Event("caketaco");

        assertTrue(matchs.accepts(combinedEvent));
    }

    @Test
    public void orMatchsBehavesCorrectly() throws PoCoException {
        matchs.setOperator("||");

        assertTrue("OR matchs is returning false for isOR()", matchs.isOR());
        assertFalse("OR matchs is returning true for isAND() or isNOT()", matchs.isAND() || matchs.isNOT());

        Match cakeMatch = new Match("cake");
        Match tacoMatch = new Match("taco");

        matchs.addChild(cakeMatch);
        matchs.addChild(tacoMatch);

        Event cakeEvent = new Event("cake");
        Event tacoEvent = new Event("taco");

        assertTrue(matchs.accepts(cakeEvent));
        assertTrue(matchs.accepts(tacoEvent));

        Event neitherEvent = new Event("something");

        assertFalse(matchs.accepts(neitherEvent));
    }
}
