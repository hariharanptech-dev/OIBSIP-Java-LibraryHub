package com.reservation.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

/** Shared colours, fonts and small widget factories so every screen looks consistent. */
final class UiTheme {

    static final Color PRIMARY = new Color(0x1F4E79);
    static final Color SECONDARY = new Color(0x5F6B7A);
    static final Color DANGER = new Color(0xB3261E);
    static final Color SUCCESS = new Color(0x2E7D32);
    static final Color BACKGROUND = new Color(0xF4F6F9);
    static final Color CARD = Color.WHITE;

    static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 14);

    private UiTheme() {
    }

    /** Creates the coloured title banner shown at the top of every window. */
    static JPanel header(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        JLabel t = new JLabel(title);
        t.setFont(TITLE_FONT);
        t.setForeground(Color.WHITE);
        panel.add(t, BorderLayout.NORTH);
        JLabel s = new JLabel(subtitle);
        s.setFont(BODY_FONT);
        s.setForeground(new Color(0xD6E4F0));
        panel.add(s, BorderLayout.SOUTH);
        return panel;
    }

    /** Creates a flat, coloured action button. */
    static JButton button(String text, Color background) {
        JButton b = new JButton(text);
        b.setFont(BOLD_FONT);
        b.setBackground(background);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        return b;
    }

    /** Creates a form label. */
    static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BOLD_FONT);
        l.setForeground(new Color(0x2B3440));
        return l;
    }

    /** Creates a label used to show read-only values. */
    static JLabel valueLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(BODY_FONT);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    /** Applies the standard font and padded border to an input component. */
    static void style(JComponent c) {
        c.setFont(BODY_FONT);
        Border line = BorderFactory.createLineBorder(new Color(0xB8C2CE));
        c.setBorder(BorderFactory.createCompoundBorder(line, BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        if (c instanceof JTextComponent && !((JTextComponent) c).isEditable()) {
            c.setBackground(new Color(0xEEF1F5));
        }
    }

    /** Creates a white padded "card" panel with a GridBagLayout. */
    static JPanel card() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDE3EA)),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        return p;
    }

    /** Adds a "label : component" row to a GridBagLayout panel. */
    static void addRow(JPanel panel, int row, String labelText, JComponent field) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0;
        lc.gridy = row;
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 0, 6, 16);
        panel.add(label(labelText), lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1;
        fc.gridy = row;
        fc.weightx = 1;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.insets = new Insets(6, 0, 6, 0);
        panel.add(field, fc);
    }

    /** Wraps a card in a padded, background-coloured container for use as a frame's centre. */
    static JPanel pad(JComponent inner) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BACKGROUND);
        outer.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        outer.add(inner, BorderLayout.CENTER);
        return outer;
    }

    /** Sets size, centres the frame on screen and applies the background colour. */
    static void prepare(JFrame frame, int width, int height) {
        frame.setSize(new Dimension(width, height));
        frame.setMinimumSize(new Dimension(width - 80, height - 60));
        frame.getContentPane().setBackground(BACKGROUND);
        frame.setLocationRelativeTo(null);
    }

    /** Shows a warning dialog for an input problem. */
    static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Please check your input", JOptionPane.WARNING_MESSAGE);
    }

    /** Shows a friendly database error dialog (no stack trace) and logs the cause to the console. */
    static void showDbError(Component parent, SQLException ex) {
        System.err.println("Database error: " + ex.getMessage());
        JOptionPane.showMessageDialog(parent,
                "Something went wrong while accessing the database.\n" + ex.getMessage()
                        + "\n\nPlease try again.",
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
