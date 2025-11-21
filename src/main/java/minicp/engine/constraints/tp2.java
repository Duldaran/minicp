/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;

import static minicp.cp.Factory.*;

/**
 * Less or equal constraint between two variables
 */
public class tp2 extends AbstractConstraint {

    private final IntVar x;
    private final IntVar[] y;
    private final IntVar z;
    private final IntVar sumY;

    public tp2(IntVar x, IntVar[] y, IntVar z) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.z = z;
        Solver cp = x.getSolver();
        this.sumY = makeIntVar(cp, y.length * y[0].min(), y.length * y[0].max());
        cp.post(new Sum(y, sumY));
    }

    @Override
    public void post() {
        x.propagateOnExcludeZero(this);
        for(IntVar yi : y)
            yi.propagateOnLowerBoundChange(this);
        z.propagateOnUpperBoundChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        z.removeBelow(0);
        if(!x.contains(0)) {
            z.removeBelow(Math.ceilDiv(sumY.min(), y.length));
            sumY.removeAbove(z.max()*y.length);
        }
        else{
            z.removeBelow(Math.ceilDiv(sumY.min(), 2*y.length));
            sumY.removeAbove(2*z.max()*y.length);
        }
    }
}
