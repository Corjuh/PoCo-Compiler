package com.poco.Extractor;

import java.util.ArrayList;

/**
 * Created by yan on 7/6/15.
 */
public class PolicyTreeNode {
    private String strategy;
    private ArrayList<String> children;
    private String ancestor;

    public PolicyTreeNode() {
        strategy = null;
        children = new ArrayList<>();
        ancestor = null;
    }

    public PolicyTreeNode(String strategy) {
        this.strategy = strategy;
        children = new ArrayList<>();
        ancestor = null;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public ArrayList<String> getChildren() {
        return children;
    }

    public void addChildren(String child) {
        this.children.add(child);
    }

    public String getAncestor() {
        return ancestor;
    }

    public void setAncestor(String ancestor) {
        this.ancestor = ancestor;
    }

    @Override
    public String toString() {
        return "PolicyTreeNode{" +
                "strategy='" + strategy + '\'' +
                ", children=" + children +
                ", ancestor='" + ancestor + '\'' +
                '}';
    }
}
