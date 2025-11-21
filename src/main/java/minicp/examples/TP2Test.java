package minicp.examples;

import minicp.cp.Factory;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;

import java.util.Arrays;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;

/**
 * Programme test du tp2 de INF6101
 */
public class TP2Test {

    public static void main(String[] args) {

        int domaineMin = (args.length > 0 && args[0] != null) ? Integer.parseInt(args[0]) : -1;
        int domaineMax = (args.length > 1 && args[1] != null) ? Integer.parseInt(args[1]) : 2;

        Solver cp = Factory.makeSolver();

        IntVar x = makeIntVar(cp, domaineMin, domaineMax);
        IntVar[] y = new IntVar[3];
        IntVar z = makeIntVar(cp, domaineMin, domaineMax);
        for(int i = 0; i < 3; i++)
            y[i] = makeIntVar(cp, domaineMin, domaineMax);

        cp.post(tp2(x, y, z));


        DFSearch dfs = makeDfs(cp, splitDomRange(x, z, y[0], y[1], y[2]));


        /*dfs.onSolution(() -> {
            System.out.println("Solution found");
            System.out.println("x = " + x.min() + " y = " + y[0].min() + ", " + y[1].min() + ", " + y[2].min() + " z = " + z.min());
        });*/

        SearchStatistics stats = dfs.solve();

        System.out.println(stats);

        DFSearch dfs2 = makeDfs(cp, firstFail(x, z, y[0], y[1], y[2]));

        /*dfs2.onSolution(() -> {
            System.out.println("Solution found");
            System.out.println("x = " + x.min() + " y = " + y[0].min() + ", " + y[1].min() + ", " + y[2].min() + " z = " + z.min());
        });*/

        SearchStatistics stats2 = dfs2.solve();

        System.out.println(stats2);

    }

}
