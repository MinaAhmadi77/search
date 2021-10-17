package com.interpreter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class GUI {
    private static State state;
    private static ArrayList<ArrayList<JLabel>> GUIMap;
    private static JFrame frame;
    private static JPanel legend;
    private static JFrame legendFrame;

    public static void main(String[] args) {
        readMap("gui_out.txt");
        frame = new JFrame();
        JPanel panel = createMap();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
        frame.addKeyListener(new CustomKeyListener());
        legendFrame = new JFrame();
        legendFrame.getContentPane().add(legend);
        legendFrame.pack();
        legendFrame.setFocusable(false);
        frame.setVisible(true);
    }

    public static void GUIOutput(State state, String filename) {
        try {
            File file = new File(filename);
            file.delete();
            if (file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(filename);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(state);
                oos.close();
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static JPanel createMap() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(state.map.size(), state.map.get(0).size()));
        GUIMap = new ArrayList<>();
        for (int i = 0; i < state.map.size(); i++) {
            ArrayList<JLabel> row = new ArrayList<>();
            for (int j = 0; j < state.map.get(0).size(); j++) {
                JLabel label;
                label = new JLabel(state.map.get(i).get(j), SwingConstants.CENTER);
                label.setPreferredSize(new Dimension(50, 50));
                Border border = BorderFactory.createLineBorder(Color.black);
                label.setBorder(border);
                label.setFont(label.getFont().deriveFont(30.0f));
                label.setOpaque(true);
                label.setForeground(Color.yellow);
                panel.add(label);
                row.add(label);
            }
            GUIMap.add(row);
        }
        for (State.Coordinate p : state.goals) {
            String txt = GUIMap.get(p.x).get(p.y).getText();
            GUIMap.get(p.x).get(p.y).setText(txt + "p");
        }
        setColors();
        return panel;
    }

    private static void setColors() {
        int maxCost = 0;
        for (int i = 0; i < state.costs.size(); i++) {
            for (int j = 0; j < state.costs.get(0).size(); j++) {
                if (maxCost < state.costs.get(i).get(j))
                    maxCost = state.costs.get(i).get(j);
            }
        }

        int Rmin = 51, Gmin = 8, Bmin = 0;
        int Rmax = 179, Gmax = 30, Bmax = 0;
        for (int i = 0; i < state.costs.size(); i++) {
            for (int j = 0; j < state.costs.get(0).size(); j++) {
                GUIMap.get(i).get(j).setBackground(linearColor(Rmin, Rmax, Gmin, Gmax, Bmin, Bmax, maxCost + 1, state.costs.get(i).get(j)));
            }
        }
        legend = new JPanel();
        legend.setLayout(new GridLayout(maxCost + 1, 2));
        for (int i = 0; i <= maxCost; i++) {
            JLabel label;
            label = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            label.setPreferredSize(new Dimension(50, 50));
            label.setFont(label.getFont().deriveFont(30.0f));
            label.setOpaque(true);
            label.setBackground(linearColor(Rmin, Rmax, Gmin, Gmax, Bmin, Bmax, maxCost + 1, i));
            label.setForeground(Color.yellow);
            legend.add(label);
        }
    }

    private static int linear(int min, int max, int n, int x) {
        return (max - min) * x / n + min;
    }

    private static Color linearColor(int Rmin, int Rmax, int Gmin, int Gmax, int Bmin, int Bmax, int n, int cost) {
        return new Color(linear(Rmin, Rmax, n, cost), linear(Gmin, Gmax, n, cost), linear(Bmin, Bmax, n, cost));
    }

    private static void readMap(String filename) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            state = (State) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error in reading file");
            e.printStackTrace();
        }
    }


    static class CustomKeyListener implements KeyListener {
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                if (state.child != null) {
                    state = state.child;
                    JPanel panel = createMap();
                    frame.getContentPane().removeAll();
                    frame.getContentPane().add(panel);
                    frame.revalidate();
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                if (state.father != null) {
                    state = state.father;
                    JPanel panel = createMap();
                    frame.getContentPane().removeAll();
                    frame.getContentPane().add(panel);
                    frame.revalidate();
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_L) {
                legendFrame.getContentPane().removeAll();
                legendFrame.getContentPane().add(legend);
                legendFrame.revalidate();
                legendFrame.setVisible(!legendFrame.isVisible());
                frame.requestFocus();
            }
        }


        public void keyReleased(KeyEvent e) {
        }
    }
}
