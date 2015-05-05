package com.poco.PoCoRuntime;

/**
 * Created by cjuhlin on 8/23/14.
 */
public class SequentialExecution extends AbstractExecution implements
		Queryable, Matchable {
	protected int currentCursor = 0;
	protected boolean exhausted = false;

	protected boolean currentChildIsZeroPlus = false;
	protected boolean currentChildIsOnePlus = false;

	public SequentialExecution(String modifier) throws PoCoException {
		super(modifier);
	}

	// use to set the current modifier for the first child before start query,
	// and later update modifier while advance cursor
	public void getCurrentChildModifier() { 
		if (this.children.size() > 0 && currentCursor < this.children.size()) {
			Class<AbstractExecution> classAE = AbstractExecution.class;
			Class<? extends EventResponder> classChild = children.get(
					this.currentCursor).getClass();
			if (classAE.isAssignableFrom(classChild)) {
				// System.out.println("it is assignable from abstractExecution");
				currentChildIsZeroPlus = ((AbstractExecution) this.children
						.get(this.currentCursor)).isZeroPlus();
				currentChildIsOnePlus = ((AbstractExecution) this.children
						.get(this.currentCursor)).isOnePlus();
			} else {
				// now is query the exchange, so isZero and isPlus is the same
			}
		}
	}

	public int getCurrentCursor() {
		return currentCursor;
	}

	public boolean isExhausted() {
		return exhausted;
	}

	public boolean isCurrentChildIsZeroPlus() {
		return currentChildIsZeroPlus;
	}

	public boolean isCurrentChildIsOnePlus() {
		return currentChildIsOnePlus;
	}

	/**
	 * Advances the cursor pointing to the current child to be queried. For
	 * special cases (i.e. the * modifier), the cursor loops back around to the
	 * front when we reach the end. while advance cursor, we also should update
	 * the modifier so that we always get current execution's modifier
	 */
	protected void advanceCursor() {
		if (isZeroPlus || isOnePlus)
			currentCursor = (currentCursor + 1) % children.size();
		else
			currentCursor++;
		
		if (currentCursor >= children.size()) {
			exhausted = true; 
		} else {
			// while advance cursor, we also should update the modifier so that
			// we always get
			// current execution's modifier
			getCurrentChildModifier();
		}
	}

	@Override
	public SRE query(Event event) { 
		// Don't do anything without children b
		if (children.size() == 0) {
			return null;
		}
		// Also don't do anything if no more children left
		if (exhausted) {
			return null;
		}
		getCurrentChildModifier();
		EventResponder currentChild = children.get(currentCursor);
		
		if (currentChild.accepts(event)) {
			if (!currentChildIsZeroPlus && !currentChildIsOnePlus) {
				advanceCursor(); 
			}
			resultSRE = currentChild.query(event);
			return resultSRE;
		} else { //not accepting 
			if (currentChildIsZeroPlus) { 
				// We can skip a zero-plus (*) modifier 
				advanceCursor();
				return this.query(event);
			} else { 
				// CurrentChild doesn't accept and can't be skipped
				return null;
			}
		}
	}

	@Override
	public boolean accepts(Event event) {
		if (children.size() == 0) {
			return false;
		}
		// The first child of a sequential execution must accept for its parent
		// to accept
		return children.get(0).accepts(event);
	}

	@Override
	public String toString() {
		return "SequentialExecution [currentCursor=" + currentCursor
				+ ", exhausted=" + exhausted + ", isZeroPlus=" + isZeroPlus
				+ ", isOnePlus=" + isOnePlus + ", children=" + children + "]";
	}

}
