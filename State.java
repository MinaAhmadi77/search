package com.interpreter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class State implements Serializable {
    static class Coordinate implements Serializable {
        public int x, y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public ArrayList<ArrayList<String>> map;
    public ArrayList<ArrayList<Integer>> costs;
    public Coordinate robotLoc;
    public ArrayList<Coordinate> goals;
    public State father;
    public State child;
    public DIR to_move;
    public DIR move;
    public int cost;

    public enum DIR {
        UP, DOWN, LEFT, RIGHT
    }

    public void updateRobotLoc() {
        this.robotLoc = this.getRobotLoc();
    }

    public State(State s) {
        this.map = new ArrayList<>();
        this.costs = new ArrayList<>();
        for (int i = 0; i < s.map.size(); i++) {
            ArrayList<String> map_row = new ArrayList<>(s.map.get(i));
            ArrayList<Integer> cost_row = new ArrayList<>(s.costs.get(i));
            this.map.add(map_row);
            this.costs.add(cost_row);
        }
        this.robotLoc = new Coordinate(s.robotLoc.x, s.robotLoc.y);
        this.goals = new ArrayList<>(s.goals);
        this.cost = Integer.MAX_VALUE;
    }

    public DIR whichMove(State dst) {
        Coordinate r = this.getRobotLoc();
        Coordinate dr = dst.getRobotLoc();
        if (r.x - 1 == dr.x)
            return DIR.UP;
        if (r.x + 1 == dr.x)
            return DIR.DOWN;
        if (r.y + 1 == dr.y)
            return DIR.RIGHT;
        if (r.y - 1 == dr.y)
            return DIR.LEFT;
        return null;
    }

    public State(String[][] map, int n, int m) {
        this.map = new ArrayList<>();
        this.costs = new ArrayList<>();
        this.goals = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ArrayList<String> map_row = new ArrayList<>();
            ArrayList<Integer> cost_row = new ArrayList<>();
            for (int j = 0; j < m; j++) {
                String d = map[i][j].replaceAll("[^0-9]", "");
                String obj = map[i][j].replaceAll("[^a-zA-Z]", "");
                if (obj.equals("p")) {
                    map_row.add("");
                    this.goals.add(new Coordinate(i, j));
                } else
                    map_row.add(obj);
                if (!d.equals(""))
                    cost_row.add(Integer.valueOf(d));
                else
                    cost_row.add(0);
            }
            this.map.add(map_row);
            this.costs.add(cost_row);
        }
        this.robotLoc = this.getRobotLoc();
        this.cost = Integer.MAX_VALUE;
    }

    public int getCost(Coordinate loc) {
        return this.costs.get(loc.x).get(loc.y);
    }

    public ArrayList<State> allPossibleGoals() {
        ArrayList<State> possibleGoals = new ArrayList<>();
        State goal = new State(this);
        for (Coordinate butter : this.getButtersLoc()) {
            goal.map.get(butter.x).set(butter.y, "");
        }
        for (Coordinate p : this.goals) {
            goal.map.get(p.x).set(p.y, "b");
        }
        for (Coordinate butter : goal.getButtersLoc()) {
            if (butter.x - 1 > 0 && goal.map.get(butter.x - 1).get(butter.y).equals("")) {
                State possible = new State(goal);
                possible.swap(possible.getRobotLoc(), new Coordinate(butter.x - 1, butter.y), possible.map);
                possible.updateRobotLoc();
                possibleGoals.add(possible);
            }
            if (butter.x + 1 < goal.map.size() && goal.map.get(butter.x + 1).get(butter.y).equals("")) {
                State possible = new State(goal);
                possible.swap(possible.getRobotLoc(), new Coordinate(butter.x + 1, butter.y), possible.map);
                possible.updateRobotLoc();
                possibleGoals.add(possible);
            }
            if (butter.y - 1 > 0 && goal.map.get(butter.x).get(butter.y - 1).equals("")) {
                State possible = new State(goal);
                possible.swap(possible.getRobotLoc(), new Coordinate(butter.x, butter.y - 1), possible.map);
                possible.updateRobotLoc();
                possibleGoals.add(possible);
            }
            if (butter.y + 1 < goal.map.get(0).size() && goal.map.get(butter.x).get(butter.y + 1).equals("")) {
                State possible = new State(goal);
                possible.swap(possible.getRobotLoc(), new Coordinate(butter.x, butter.y + 1), possible.map);
                possible.updateRobotLoc();
                possibleGoals.add(possible);
            }
        }
        return possibleGoals;
    }

    public int distance() {
        ArrayList<Coordinate> butteres = getButtersLoc();
        int dist = 0;
        for (Coordinate butter : butteres) {
            int min = Integer.MAX_VALUE;
            int d = 0;
            for (Coordinate goal : this.goals) {
                d = (int) (Math.sqrt(goal.x - butter.x) * (goal.x - butter.x) + (goal.y - butter.y) * (goal.x - butter.x));
                if (d < min)
                    d = min;
            }
            dist += d;
        }
        int r_dist = Integer.MAX_VALUE;
        for (Coordinate butter : butteres) {
            int d = (int) (Math.sqrt(robotLoc.x - butter.x) * (robotLoc.x - butter.x) + (robotLoc.y - butter.y) * (robotLoc.x - butter.x));
            if (d < r_dist)
                r_dist = d;
        }
        return dist + r_dist;
    }

    private ArrayList<Coordinate> getButtersLoc() {
        ArrayList<Coordinate> locs = new ArrayList<>();
        for (int i = 0; i < this.map.size(); i++) {
            for (int j = 0; j < this.map.get(0).size(); j++) {
                if (this.map.get(i).get(j).equals("b"))
                    locs.add(new Coordinate(i, j));
            }
        }
        return locs;
    }

    public boolean isGoal() {
        for (Coordinate goal : this.goals) {
            if (!this.map.get(goal.x).get(goal.y).equals("b"))
                return false;
        }
        return true;
    }

    public ArrayList<State> nextStates(boolean recursive) {
        ArrayList<State> states = new ArrayList<>();
        for (DIR dir : State.DIR.values()) {
            if (isMovePossible(dir, recursive))
                states.add(move(dir, recursive));
        }
        return states;
    }

    private State move(DIR dir, boolean recursive) {
        State next = new State(this);
        Coordinate dst;
        Coordinate dst_b;
        switch (dir) {
            case UP:
                dst = new Coordinate(robotLoc.x - 1, robotLoc.y);
                dst_b = new Coordinate(robotLoc.x - 2, robotLoc.y);
                if (recursive)
                    dst_b = new Coordinate(robotLoc.x + 1, robotLoc.y);
                next.to_move = DIR.UP;
                break;
            case DOWN:
                dst = new Coordinate(robotLoc.x + 1, robotLoc.y);
                dst_b = new Coordinate(robotLoc.x + 2, robotLoc.y);
                if (recursive)
                    dst_b = new Coordinate(robotLoc.x - 1, robotLoc.y);
                next.to_move = DIR.DOWN;
                break;
            case LEFT:
                dst = new Coordinate(robotLoc.x, robotLoc.y - 1);
                dst_b = new Coordinate(robotLoc.x, robotLoc.y - 2);
                if (recursive)
                    dst_b = new Coordinate(robotLoc.x, robotLoc.y + 1);
                next.to_move = DIR.LEFT;
                break;
            case RIGHT:
                dst = new Coordinate(robotLoc.x, robotLoc.y + 1);
                dst_b = new Coordinate(robotLoc.x, robotLoc.y + 2);
                if (recursive)
                    dst_b = new Coordinate(robotLoc.x, robotLoc.y - 1);
                next.to_move = DIR.RIGHT;
                break;
            default:
                dst = new Coordinate(-1, -1);
                dst_b = new Coordinate(-1, -1);
                break;
        }
        if (recursive) {
            swap(this.robotLoc, dst, next.map);
            if (!isOutOfBox(dst_b) && next.map.get(dst_b.x).get(dst_b.y).equals("b")) {
                swap(this.getRobotLoc(), dst_b, next.map);
            }
            next.robotLoc = next.getRobotLoc();
            return next;
        }
        if (next.map.get(dst.x).get(dst.y).equals("b")) {
            swap(dst, dst_b, next.map);
        }
        swap(this.robotLoc, dst, next.map);
        next.robotLoc = next.getRobotLoc();
        return next;
    }

    private void swap(Coordinate loc1, Coordinate loc2, ArrayList<ArrayList<String>> map) {
        String tmp = map.get(loc2.x).get(loc2.y);
        map.get(loc2.x).set(loc2.y, map.get(loc1.x).get(loc1.y));
        map.get(loc1.x).set(loc1.y, tmp);
    }

    private boolean isMovePossible(DIR dir, boolean recursive) {
        Coordinate dst;
        Coordinate dst_b;
        switch (dir) {
            case UP:
                dst = new Coordinate(robotLoc.x - 1, robotLoc.y);
                dst_b = new Coordinate(robotLoc.x - 2, robotLoc.y);
                if (recursive)
                    dst_b = new Coordinate(robotLoc.x + 1, robotLoc.y);
                break;
            case DOWN:
                dst = new Coordinate(robotLoc.x + 1, robotLoc.y);
                dst_b = new Coordinate(robotLoc.x + 2, robotLoc.y);
                if (recursive)
                    dst_b = new Coordinate(robotLoc.x - 1, robotLoc.y);
                break;
            case LEFT:
                dst = new Coordinate(robotLoc.x, robotLoc.y - 1);
                dst_b = new Coordinate(robotLoc.x, robotLoc.y - 2);
                if (recursive)
                    dst_b = new Coordinate(robotLoc.x, robotLoc.y + 1);
                break;
            case RIGHT:
                dst = new Coordinate(robotLoc.x, robotLoc.y + 1);
                dst_b = new Coordinate(robotLoc.x, robotLoc.y + 2);
                if (recursive)
                    dst_b = new Coordinate(robotLoc.x, robotLoc.y - 1);
                break;
            default:
                dst = new Coordinate(-1, -1);
                dst_b = new Coordinate(-1, -1);
                break;
        }

        if (isOutOfBox(dst))
            return false;
        if (this.map.get(dst.x).get(dst.y).equals("x"))
            return false;
        if (recursive) {
            if (this.map.get(dst.x).get(dst.y).equals("b"))
                return false;
            if (!isOutOfBox(dst_b) && this.map.get(dst_b.x).get(dst_b.y).equals("b")) {
                if (isOutOfBox(dst_b))
                    return false;
                if (this.goals.contains(dst_b))
                    return false;
            }
            return true;
        }
        if (this.map.get(dst.x).get(dst.y).equals("b")) {
            if (isOutOfBox(dst_b))
                return false;
            if (this.map.get(dst_b.x).get(dst_b.y).equals("x"))
                return false;
            if (this.map.get(dst_b.x).get(dst_b.y).equals("b"))
                return false;
            if (this.goals.contains(dst))
                return false;
        }
        return true;
    }

    private boolean isOutOfBox(Coordinate c) {
        if (c.x < 0)
            return true;
        if (c.x >= this.map.size())
            return true;
        if (c.y < 0)
            return true;
        if (c.y >= this.map.get(0).size())
            return true;
        return false;
    }

    public Coordinate getRobotLoc() {
        for (int i = 0; i < this.map.size(); i++) {
            for (int j = 0; j < this.map.get(i).size(); j++) {
                if (this.map.get(i).get(j).equals("r"))
                    return new Coordinate(i, j);
            }
        }
        return new Coordinate(-1, -1);
    }

    @Override
    public String toString() {
        return this.map.toString() + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        for (int i = 0; i < map.size(); i++) {
            for (int j = 0; j < map.get(0).size(); j++) {
                if (!map.get(i).get(j).equals(state.map.get(i).get(j)))
                    return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }
}
