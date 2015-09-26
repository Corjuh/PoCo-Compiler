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
	public SRE query(Event event) {
		if (children.size() == 0)
			return null;

		resultBool = true;
		resultSRE = children.get(currentCursor).query(event);

		if (isZeroPlus || isOnePlus)
			exhausted = false;
		else
			exhausted = true;

		return resultSRE;
	}

	@Override
	public boolean accepts(Event event) {
		if (children.size() == 0 || exhausted)
			return false;

		if (children.get(0).accepts(event)) {
			currentCursor = 0;
			return true;
		} else {
			currentCursor = 1;
			boolean result = children.get(1).accepts(event);

			if(result == false)
				exhausted = true;
			return result;
		}
	}
}