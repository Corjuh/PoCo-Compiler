package com.poco;

/**
 * Abstract class that any class capable of responding to a PoCo event must inherit from
 * (i.e. Policy, Execution, Exchange). Really just a wrapper to say that the class
 * implements both the Queryable and Matchable interfaces.
 */
public abstract class EventResponder implements Queryable, Matchable {
	protected boolean isQueried = false;
	public void resetIsQueried(){
		isQueried = false;
	}
}
