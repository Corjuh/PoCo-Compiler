package com.poco.PoCoRuntime;

/**
 * Created by caoyan on 12/4/14.
 */
public class AlternationExecution extends SequentialExecution {
	@Override
	public String toString() {
		return "AlternationExecution [currentCursor=" + currentCursor
				+ ", children=" + children + ", isQueried="
				+ ", resultBool=" + resultBool + ", resultSRE=" + resultSRE
				+ "]";
	}

	public AlternationExecution(String modifier) throws PoCoException {
		super(modifier);
	}

	@Override
	public SRE query(Event event) {
		if (children.size() == 0) {
			return null;
		}
		if (children.get(0).accepts(event)) {
			resultBool = true;
			resultSRE = children.get(0).query(event);
		} else {
			resultBool = children.get(1).accepts(event);
			resultSRE = children.get(1).query(event);
		}
		return resultSRE;
	}

	@Override
	public boolean accepts(Event event) {
		if (children.size() == 0) {
			return false;
		}
		if(children.get(0).accepts(event)) {
			return true;
		}else {
			boolean result =children.get(1).accepts(event);
			return result;
		}
	}
}
