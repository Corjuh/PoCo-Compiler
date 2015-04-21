package com.poco;


/**
 * Created by caoyan on 12/4/14.
 */
public class AlternationExecution extends SequentialExecution
{
    private Boolean resultBool = false;
	private SRE resultSRE = null;
	
    public AlternationExecution(String modifier) throws PoCoException {
        super(modifier);
    }
 	
    @Override
    public SRE query(Event event) {
    	if (children.size() == 0){
			return null;
		}
		if (!isQueried) {
			if (children.get(0).accepts(event))  {
				resultBool = true;
				resultSRE = children.get(0).query(event);
			} else  {
				resultBool = children.get(1).accepts(event);
				resultSRE = children.get(1).query(event);
			}
			isQueried = true;
			return resultSRE;
		}
		else {
			return resultSRE;
		}
    }

    @Override
    public boolean accepts(Event event) {
    	if (children.size() == 0) {
			return false;
		}
		// The first child of a sequential execution must accept for its parent
		// to accept
		this.query(event);
		return resultBool;
    }
}
