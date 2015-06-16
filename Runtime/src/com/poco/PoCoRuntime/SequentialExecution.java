package com.poco.PoCoRuntime;

/**
 * Created by cjuhlin on 8/23/14.
 */
public class SequentialExecution extends AbstractExecution implements
		Queryable, Matchable {
	//use to flag if the sequential Execution has exhange or not, 
	//if no, it will only have exchange as child
	private boolean hasExch = false;

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
				currentChildIsZeroPlus = ((AbstractExecution) this.children
						.get(this.currentCursor)).isZeroPlus();
				currentChildIsOnePlus = ((AbstractExecution) this.children
						.get(this.currentCursor)).isOnePlus();
			} else {
				// now is query the exchange, so isZero and isPlus is the same
			}
		}
	}
	
	public boolean childrenExhausted() {
		if(hasExch)
			return true;
		if (this.children.size() > 0 && currentCursor < this.children.size()) {
			Class<AbstractExecution> classAE = AbstractExecution.class;
			Class<? extends EventResponder> classChild = children.get(
					this.currentCursor).getClass();
			if (classAE.isAssignableFrom(classChild)) 
				return ((AbstractExecution) this.children.get(this.currentCursor)).exhausted;				
		}
		return true;
	}

	public int getCurrentCursor() {
		return currentCursor;
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
		if (isZeroPlus || isOnePlus) {
			currentCursor = (currentCursor + 1) % children.size();
			if(currentCursor ==0 && !this.hasExch) {
				//reset all its children's currentCursor to 0
				resetChildrenCursor();
			}
			
		}else
			currentCursor++;
		if (currentCursor >= children.size()) {
			exhausted = true; 
		} else {
			// while advance cursor, we also should update the modifier so that
			// we always get current execution's modifier
			getCurrentChildModifier();
		}
	}

	@Override
	public SRE query(Event event) { 
		if (children.size() == 0 || exhausted) 
			return null;
		resultSRE = children.get(currentCursor).query(event);
		if (!currentChildIsZeroPlus && !currentChildIsOnePlus && childrenExhausted())  
				advanceCursor(); 
		return resultSRE;
	}

	@Override
	public boolean accepts(Event event) {
		if (children.size() == 0 || exhausted)  
			return false;
		// The first child of a sequential execution must accept for its parent
		// to accept
		if(this.hasExch) {
			return children.get(0).accepts(event);
		}else {
			boolean result = children.get(currentCursor).accepts(event);
			//if return false, check if it is exhausted or not, if so, return false
			if(!result && currentCursor==(children.size()-1)) {
				this.exhausted = true;
				return false;
			}
			while (!result ) {
				getCurrentChildModifier();
				if (currentChildIsZeroPlus) {
					advanceCursor();
					if(!exhausted)
						result = children.get(currentCursor).accepts(event);
					else
						return false;
				} else {
					break;
				}
			}
			return result;
		}
	}

	public void resetChildrenCursor() {
		if(this.hasExch != true && this.children.size() >0){
			for (int i = 0; i < this.children.size(); i++) {
				((AbstractExecution)children.get(i)).currentCursor=0;
				((AbstractExecution)children.get(i)).exhausted = false;
				((AbstractExecution)children.get(i)).resetChildrenCursor(); 
			}
		}
	}
	 
	
	public void setHasExch(boolean boolVal){
		this.hasExch = boolVal;
	}
	
	@Override
	public String toString() {
		return "SequentialExecution [currentCursor=" + currentCursor
				 + ", children=" + children + "]";
	}

}
