package com.reservation.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/** Dashboard shown after login with Book, Cancel and Logout actions. */
@SuppressWarnings({"serial", "this-escape"})
public class MainMenu extends JFrame {

    private static final long serialVersionUID = 1L;

    private final String username;

    /**
     * Builds the dashboard.
     *
     * @param username the logged-in user's name
     */
    public MainMenu(String username) {
        super("Online Reservation System - Main Menu");
        this.username = username;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(UiTheme.header("Main Menu", "Logged in as " + username), BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(3, 1, 0, 14));
        buttons.setOpaque(false);
        JButton book = UiTheme.button("Book a Ticket", UiTheme.PRIMARY);
        JButton cancel = UiTheme.button("Cancel a Ticket", UiTheme.DANGER);
        JButton logout = UiTheme.button("Logout", UiTheme.SECONDARY);
        buttons.add(book);
        buttons.add(cancel);
        buttons.add(logout);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        JLabel hint = new JLabel("What would you like to do?");
        hint.setFont(UiTheme.BOLD_FONT);
        center.add(hint, BorderLayout.NORTH);
        center.add(buttons, BorderLayout.CENTER);
        center.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        add(UiTheme.pad(center), BorderLayout.CENTER);

        book.addActionListener(e -> open(new ReservationForm(this, username)));
        cancel.addActionListener(e -> open(new CancellationForm(this)));
        logout.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        UiTheme.prepare(this, 420, 420);
    }

    /** Hides the menu and shows the given form; the form re-shows the menu when it closes. */
    private void open(JFrame form) {
        setVisible(false);
        form.setVisible(true);
    }

    /** @return the logged-in username */
    public String getUsername() {
        return username;
    }
}
