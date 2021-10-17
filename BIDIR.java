package com.interpreter;

import java.util.*;

public class BIDIR {
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
        State goal = bidirectional(state, state.allPossibleGoals());
        if (goal != null) {
            ArrayList<String> path = getPath(goal);
            for (String str: path) {
                System.out.print(str + ' ');
            }
            System.out.print('\n');
            System.out.println(path.size());
            System.out.println(path.size());
        }else
            System.out.println("can’t pass the butter");
        GUI.GUIOutput(state, "gui_out.txt");
    }

    public static State bidirectional(State src, ArrayList<State> goals) {
        Queue<State> sQueue = new LinkedList<>();
        Set<State> sVisited = new HashSet<>();
        sQueue.add(src);
        sVisited.add(src);
        Queue<State> dQueue = new LinkedList<>(goals);
        Set<State> dVisited = new HashSet<>(goals);
        while (!sQueue.isEmpty() && !dQueue.isEmpty()) {
            State i = intersection(sVisited, dVisited);
            if (i != null) {
                while (i.child != null) {
                    State tmp = i.child.father;
                    i.child.father = i;
                    i.child.child = tmp;
                    i.child.to_move = i.whichMove(i.child);
                    i = i.child;
                }
                return i;
            }
            sQueue = BFS(sQueue, sVisited, false);
            dQueue = BFS(dQueue, dVisited, true);
        }
        return null;
    }

    private static State intersection(Set<State> sVisited, Set<State> dVisited) {
        for (State state : dVisited) {
            if (sVisited.contains(state)) {
                for (State state2 : sVisited) {
                    if (state2.equals(state)) {
                        state2.child = state.father;
                        return state2;
                    }
                }
            }
        }
        return null;
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

    private static Queue<State> BFS(Queue<State> queue, Set<State> visited, boolean recursive) {
        Queue<State> nQueue = new LinkedList<>();
        while (!queue.isEmpty()) {
            State state = queue.remove();
            for (State child : state.nextStates(recursive)) {
                if (!visited.contains(child)) {
                    child.father = state;
                    visited.add(child);
                    nQueue.add(child);
                }
            }
        }
        return nQueue;
    }

}


