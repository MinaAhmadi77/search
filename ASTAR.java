package com.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

public class ASTAR {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int m = scanner.nextInt();
        String[][] map = new String[n][m];
        for (int i = 0; i < n; i++) {
            String[] row = new String[m];
            for (int j = 0; j < m; j++) {
                row[j] = scanner.next();
            }
            map[i] = row;
        }
        State state = new State(map, n, m);

        State goal = AStar(state);
        if (goal != null) {
            ArrayList<String> path = getPath(goal);
            for (String str : path) {
                System.out.print(str + ' ');
            }
            System.out.print('\n');
            System.out.println(goal.cost);
            System.out.println(path.size());
        } else
            System.out.println("can’t pass the butter");
        GUI.GUIOutput(state, "gui_out.txt");
    }

    public static ArrayList<String> getPath(State goal) {
        State root = null;
        while (goal.father != null) {
            goal.father.child = goal;
            goal.father.move = goal.to_move;
            root = goal.father;
            goal = goal.father;
        }
        ArrayList<String> path = new ArrayList<>();
        while (root.move != null) {
            switch (root.move) {
                case RIGHT:
                    path.add("R");
                    break;
                case DOWN:
                    path.add("D");
                    break;
                case UP:
                    path.add("U");
                    break;
                case LEFT:
                    path.add("L");
                    break;
            }
            root = root.child;

        }
        return path;
    }

    private static int h(State state) {
        return state.distance();
    }

    public static State AStar(State root) {
        HashMap<State, Integer> costs = new HashMap<>();
        PriorityQueue<State> pq = new PriorityQueue<>((s1, s2) -> {
            int f1 = h(s1) + costs.get(s1);
            int f2 = h(s2) + costs.get(s2);
            return Integer.compare(f1, f2);
        });
        costs.put(root, 0);
        pq.add(root);
        while (pq.size() != 0) {
            State state = pq.poll();
            if (state.isGoal()) {
                state.cost = costs.get(state);
                return state;
            }
            for (State next : state.nextStates(false)) {
                int new_cost = costs.get(state) + state.getCost(next.getRobotLoc());
                if (!costs.containsKey(next)) {
                    costs.put(next, Integer.MAX_VALUE);
                }
                if (new_cost < costs.get(next)) {
                    costs.put(next, new_cost);
                    next.father = state;
                    if (!pq.contains(next))
                        pq.add(next);
                }
            }
        }
        return null;
    }
}

