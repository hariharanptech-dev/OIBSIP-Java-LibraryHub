package com.reservation.ui;

import com.reservation.dao.UserDAO;
import com.reservation.model.User;
import com.reservation.util.PasswordUtil;
import com.reservation.util.Validator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Optional;

/** Login screen. Pressing Enter in either field (or clicking Login) submits the form. */
@SuppressWarnings({"serial", "this-escape"})
public class LoginForm extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final UserDAO userDAO = new UserDAO();

    /** Builds the login window (call {@code setVisible(true)} to show it). */
    public LoginForm() {
        super("Online Reservation System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(UiTheme.header("Online Reservation System", "Please log in to continue"), BorderLayout.NORTH);

        JPanel card = UiTheme.card();
        UiTheme.style(usernameField);
        UiTheme.style(passwordField);
        UiTheme.addRow(card, 0, "Username", usernameField);
        UiTheme.addRow(card, 1, "Password", passwordField);

        JButton loginButton = UiTheme.button("Login", UiTheme.PRIMARY);
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0;
        bc.gridy = 2;
        bc.gridwidth = 2;
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.insets = new Insets(18, 0, 0, 0);
        card.add(loginButton, bc);

        add(UiTheme.pad(card), BorderLayout.CENTER);

        loginButton.addActionListener(e -> attemptLogin());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> attemptLogin());

        UiTheme.prepare(this, 440, 380);
    }

    /** Validates the input, checks the credentials and opens the main menu on success. */
    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        String error = Validator.validateLogin(username, password);
        if (error != null) {
            UiTheme.showWarning(this, error);
            (username.isEmpty() ? usernameField : passwordField).requestFocusInWindow();
            return;
        }
        try {
            Optional<User> user = userDAO.authenticate(username, PasswordUtil.sha256(password));
            if (user.isPresent()) {
                dispose();
                new MainMenu(user.get().getUsername()).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Access Denied: Invalid username or password",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocusInWindow();
            }
        } catch (SQLException ex) {
            UiTheme.showDbError(this, ex);
        }
    }
}
