package com.interpreter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class IDS {
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
        State goal = ids(state, 40);
        if (goal != null) {
            ArrayList<String> path = getPath(goal);
            for (String str: path) {
                System.out.print(str + ' ');
            }
            System.out.print('\n');
            System.out.println(path.size());
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

    public static State ids(State root, int max_depth) {
        for (int i = 0; i < max_depth; i++) {
            Set<State> visited = new HashSet<>();
            State result = DLS(root, i, visited);
            if (result != null)
                return result;
        }
        return null;
    }

    public static State DLS(State root, int depth, Set<State> visited) {
        visited.add(root);
        if (root.isGoal())
            return root;
        if (depth == 0)
            return null;

        for (State state : root.nextStates(false)) {
            if (!visited.contains(state)) {
                state.father = root;
                State result = DLS(state, depth - 1, new HashSet<>(visited));
                if (result != null)
                    return result;
            }
        }
        return null;
    }
}

