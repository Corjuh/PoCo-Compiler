package com.poco.PoCoRuntime;

/**
 * Created by cjuhlin on 8/23/14.
 */
public class SequentialExecution extends AbstractExecution implements Queryable, Matchable {
    protected int currentCursor = 0;
    protected boolean exhausted = false;

    public SequentialExecution(String modifier) throws PoCoException {
        super(modifier);
    }

    /**
     * Advances the cursor pointing to the current child to be queried. For special cases
     * (i.e. the * modifier), the cursor loops back around to the front when we reach the end.
     */
    private void advanceCursor() {
        if (isZeroPlus || isOnePlus) {
            currentCursor = (currentCursor + 1) % children.size();
        } else {
            currentCursor++;
        }

        if (currentCursor >= children.size()) {
            exhausted = true;
        }
    }

    @Override
    public SRE query(Event event) {
        // Don't do anything without children
        if (children.size() == 0) {
            return null;
        }

        // Also don't do anything if no more children left
        if (exhausted) {
            return null;
        }

        EventResponder currentChild = children.get(currentCursor);

        if (currentChild.accepts(event)) {
            if (!isZeroPlus && !isOnePlus) {
                advanceCursor();
                return currentChild.query(event);
            } else {
                return currentChild.query(event);
            }
        } else if (isZeroPlus) {
            // We can skip a zero-plus (*) modifier
            advanceCursor();
            return this.query(event);
        } else {
            // CurrentChild doesn't accept and can't be skipped
            return null;
        }
    }

    @Override
    public boolean accepts(Event event) {
        if (children.size() == 0) {
            return false;
        }

        // The first child of a sequential execution must accept for its parent to accept
        return children.get(0).accepts(event);
    }
}
