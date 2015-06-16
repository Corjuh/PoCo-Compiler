package com.poco.PoCoRuntime;

/**
 * Created by caoyan on 12/4/14.
 */
public class AlternationExecution extends SequentialExecution {
	@Override
	public String toString() {
		return "AlternationExecution [currentCursor=" + currentCursor
				+ ", children=" + children + ", resultBool=" + resultBool
				+ ", resultSRE=" + resultSRE + "]";
	}

	public AlternationExecution(String modifier) throws PoCoException {
		super(modifier);
	}

	@Override
	public boolean childrenExhausted() {
		if (this.children.size() > 0 && currentCursor < this.children.size()) {
			Class<AbstractExecution> classAE = AbstractExecution.class;
			Class<? extends EventResponder> classChild = children.get(
					this.currentCursor).getClass();
			if (classAE.isAssignableFrom(classChild)) {
				boolean result = false;
				AbstractExecution temp = (AbstractExecution)children.get(this.currentCursor);
				if(temp.exhausted == true)
					result = true;
				return result;
			}
		}
		return true;
	}
	
	@Override
	public SRE query(Event event) {
		if (children.size() == 0) 
			return null;
		
		resultBool = true;
		resultSRE = children.get(currentCursor).query(event);
		if(childrenExhausted())
			this.exhausted =true;
		
		return resultSRE;
	}

	@Override
	public boolean accepts(Event event) {
		if (children.size() == 0)
			return false;
		if (currentCursor == 0) {
			if (children.get(0).accepts(event)){
				return true;
			}else {
				currentCursor = 1;
				boolean result =  children.get(1).accepts(event);
				return result;
			}
		}else
			return children.get(1).accepts(event);
	}
}
