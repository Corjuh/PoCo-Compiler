package com.poco.PoCoRuntime;

import java.util.HashSet;

import dk.brics.automaton.Automaton;

public class CalculatedSRE extends SRE {
    private Automaton posAutomaton;
    private Automaton negAutomaton;
    private HashSet<String> concreteMethods;

    // the concentrate result set will be string type, that is,
    // a. it either will be a string value that can be used create an object, or
    // b. it will the a variable, whose value can be retrieved at runtime.
    private HashSet<String> concreteResults;

    public CalculatedSRE() {
        posAutomaton = null;
        negAutomaton = null;
        concreteMethods = new HashSet<String>();
        concreteResults = new HashSet<String>();
    }

    public CalculatedSRE(Automaton posAutomaton, Automaton negAutomaton) {
        this.posAutomaton = posAutomaton;
        this.negAutomaton = negAutomaton;
        concreteMethods = new HashSet<String>();
        concreteResults = new HashSet<String>();
    }

    public CalculatedSRE(Automaton posAutomaton, Automaton negAutomaton,
                         HashSet<String> concreteMethods, HashSet<String> concreteResults) {
        this.posAutomaton = posAutomaton;
        this.negAutomaton = negAutomaton;
        this.concreteMethods = concreteMethods;
        this.concreteResults = concreteResults;
    }

    public HashSet<String> getConcreteMethods() {
        return concreteMethods;
    }

    public void setConcreteMethods(HashSet<String> concreteMethods) {
        this.concreteMethods = concreteMethods;
    }

    public void addConcreteMethod(String concreteMethod) {
        if (concreteMethod != null)
            this.concreteMethods.add(concreteMethod);
    }

    public void addConcreteMethods(HashSet<String> concreteMethods) {
        if (concreteMethods != null && concreteMethods.size() > 0)
            this.concreteMethods.addAll(concreteMethods);
    }

    public HashSet<String> getConcreteResults() {
        return concreteResults;
    }

    public void setConcreteResults(HashSet<String> concreteResults) {
        this.concreteResults = concreteResults;
    }

    public void addConcreteResult(String concreteResult) {
        if (concreteResult != null)
            this.concreteResults.add(concreteResult);
    }

    public void addConcreteResults(HashSet<String> concreteResults) {
        if (concreteResults != null && concreteResults.size() > 0)
            this.concreteResults.addAll(concreteResults);
    }

    public Automaton getPosAutomaton() {
        return posAutomaton;
    }

    public Automaton getNegAutomaton() {
        return negAutomaton;
    }

    public void setPosAutomaton(Automaton automaton) {
        this.posAutomaton = automaton;
    }

    public void setNegAutomaton(Automaton automaton) {
        this.negAutomaton = automaton;
    }

    @Override
    public SRE complement() {
        return new CalculatedSRE(negAutomaton, posAutomaton,
                this.concreteMethods, this.concreteResults);
    }

    @Override
    /**
     * Includes only positive portion of SRE
     */
    public SRE getPostiveVal() {
        return new CalculatedSRE(this.posAutomaton, null, this.concreteMethods,
                this.concreteResults);
    }

    /**
     * Includes only negative portion of SRE
     *
     * @return
     */
    public SRE getNegativeVal() {
        return new CalculatedSRE(null, this.negAutomaton, this.concreteMethods,
                this.concreteResults);
    }

    @Override
    public SRE getAction() {
        // 1. check if automaton has value or not
        if (this.posAutomaton == null && this.negAutomaton == null)
            return null;
        // 2. generate automaton for action
        Automaton amAct = SREUtil.genMethodAutomaton();
        return new CalculatedSRE(posAutomaton.intersection(amAct),
                negAutomaton.intersection(amAct), this.concreteMethods, null);
    }

    /**
     * Includes only the result in SRE
     *
     * @return
     */
    @Override
    public SRE getResult() {
        // 1. check if automaton has value or not
        if (this.posAutomaton == null && this.negAutomaton == null)
            return null;

        // 2. generate automaton for action
        Automaton amAct = SREUtil.genMethodAutomaton();
        Automaton amPos = posAutomaton.minus(posAutomaton.intersection(amAct));
        Automaton amNeg = negAutomaton.minus(negAutomaton.intersection(amAct));

        return new CalculatedSRE(amPos, amNeg, null, this.concreteResults);
    }

    @Override
    public CalculatedSRE convert2CalculatedSre() {
        return this;
    }

    @Override
    public String toString() {
        return "CalculatedSRE [posAutomaton=" + posAutomaton
                + ", negAutomaton=" + negAutomaton + ", concreteMethods="
                + concreteMethods + ", concreteResults=" + concreteResults
                + "]";
    }
}