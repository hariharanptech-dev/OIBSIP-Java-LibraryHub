package com.reservation.ui;

import com.reservation.dao.ReservationDAO;
import com.reservation.dao.TrainDAO;
import com.reservation.model.Reservation;
import com.reservation.model.Train;
import com.reservation.util.Validator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Ticket booking screen. The train name is looked up automatically when the user
 * leaves the train-number field or presses Enter in it.
 */
@SuppressWarnings({"serial", "this-escape"})
public class ReservationForm extends JFrame {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final TrainDAO trainDAO = new TrainDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    private final JTextField passengerField = new JTextField(20);
    private final JTextField trainNumberField = new JTextField(20);
    private final JTextField trainNameField = new JTextField(20);
    private final JLabel trainStatus = new JLabel(" ");
    private final JComboBox<String> classCombo;
    private final JTextField dateField = new JTextField(20);
    private final JTextField sourceField = new JTextField(20);
    private final JTextField destinationField = new JTextField(20);

    /**
     * Builds the booking form.
     *
     * @param menu     the main menu to return to when this window closes
     * @param username the logged-in user (recorded as {@code booked_by})
     */
    public ReservationForm(MainMenu menu, String username) {
        super("Online Reservation System - Book Ticket");
        this.username = username;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                menu.setVisible(true);
            }
        });

        classCombo = new JComboBox<>(trainDAO.listClasses().toArray(new String[0]));
        classCombo.setFont(UiTheme.BODY_FONT);
        classCombo.setBackground(Color.WHITE);

        trainNameField.setEditable(false);
        trainNameField.setFocusable(false);
        ((PlainDocument) trainNumberField.getDocument()).setDocumentFilter(new DigitsOnlyFilter());
        for (JTextField f : new JTextField[]{passengerField, trainNumberField, trainNameField,
                dateField, sourceField, destinationField}) {
            UiTheme.style(f);
        }
        trainStatus.setFont(UiTheme.BODY_FONT);

        setLayout(new BorderLayout());
        add(UiTheme.header("Book a Ticket", "Fill in the journey details below"), BorderLayout.NORTH);

        JPanel card = UiTheme.card();
        UiTheme.addRow(card, 0, "Passenger Name", passengerField);
        UiTheme.addRow(card, 1, "Train Number", trainNumberField);
        UiTheme.addRow(card, 2, "Train Name", trainNameField);
        // Status line under the train fields ("Train found" / "Train not found").
        GridBagConstraints sc = new GridBagConstraints();
        sc.gridx = 1;
        sc.gridy = 3;
        sc.anchor = GridBagConstraints.WEST;
        sc.insets = new Insets(0, 0, 4, 0);
        card.add(trainStatus, sc);
        UiTheme.addRow(card, 4, "Class Type", classCombo);
        UiTheme.addRow(card, 5, "Date (dd-MM-yyyy)", dateField);
        UiTheme.addRow(card, 6, "Source Station", sourceField);
        UiTheme.addRow(card, 7, "Destination Station", destinationField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);
        JButton book = UiTheme.button("Book Ticket", UiTheme.PRIMARY);
        JButton clear = UiTheme.button("Clear", UiTheme.SECONDARY);
        JButton back = UiTheme.button("Back", UiTheme.SECONDARY);
        buttons.add(book);
        buttons.add(clear);
        buttons.add(back);
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0;
        bc.gridy = 8;
        bc.gridwidth = 2;
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.insets = new Insets(16, 0, 0, 0);
        card.add(buttons, bc);
        add(UiTheme.pad(card), BorderLayout.CENTER);

        // Train lookup: on focus lost and on Enter.
        trainNumberField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                lookupTrain();
            }
        });
        trainNumberField.addActionListener(e -> {
            lookupTrain();
            classCombo.requestFocusInWindow();
        });
        // Editing the number invalidates the previously resolved name.
        trainNumberField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { resetTrainDisplay(); }
            @Override public void removeUpdate(DocumentEvent e) { resetTrainDisplay(); }
            @Override public void changedUpdate(DocumentEvent e) { resetTrainDisplay(); }
        });

        book.addActionListener(e -> bookTicket());
        clear.addActionListener(e -> clearForm());
        back.addActionListener(e -> dispose());

        UiTheme.prepare(this, 560, 640);
    }

    /** Clears the train name and status (used when the number is edited). */
    private void resetTrainDisplay() {
        trainNameField.setText("");
        trainStatus.setText(" ");
    }

    /** Looks the entered train number up and shows its name, or a "Train not found" message. */
    private void lookupTrain() {
        String text = trainNumberField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        String error = Validator.validateTrainNumber(text);
        if (error != null) {
            showStatus(error, UiTheme.DANGER);
            return;
        }
        try {
            Optional<Train> train = trainDAO.findByNumber(Integer.parseInt(text));
            if (train.isPresent()) {
                trainNameField.setText(train.get().getName());
                showStatus("Train found", UiTheme.SUCCESS);
            } else {
                trainNameField.setText("");
                showStatus("Train not found", UiTheme.DANGER);
            }
        } catch (SQLException ex) {
            UiTheme.showDbError(this, ex);
        }
    }

    private void showStatus(String message, Color colour) {
        trainStatus.setForeground(colour);
        trainStatus.setText(message);
    }

    /** Validates all fields, creates a unique PNR, saves the booking and shows a confirmation. */
    private void bookTicket() {
        String name = Validator.normalize(passengerField.getText());
        String trainNo = trainNumberField.getText().trim();
        String dateText = dateField.getText().trim();
        String source = Validator.normalize(sourceField.getText());
        String destination = Validator.normalize(destinationField.getText());

        String error;
        if ((error = Validator.validatePassengerName(name)) != null) { reject(error, passengerField); return; }
        if ((error = Validator.validateTrainNumber(trainNo)) != null) { reject(error, trainNumberField); return; }
        if ((error = Validator.validateJourneyDate(dateText)) != null) { reject(error, dateField); return; }
        if ((error = Validator.validateStation("Source station", source)) != null) { reject(error, sourceField); return; }
        if ((error = Validator.validateStation("Destination station", destination)) != null) { reject(error, destinationField); return; }
        if ((error = Validator.validateDifferentStations(source, destination)) != null) { reject(error, destinationField); return; }

        try {
            // Re-check against the database so a booking is never saved for an unknown train.
            Optional<Train> train = trainDAO.findByNumber(Integer.parseInt(trainNo));
            if (train.isEmpty()) {
                showStatus("Train not found", UiTheme.DANGER);
                reject("Train not found. Please enter a valid train number.", trainNumberField);
                return;
            }
            Reservation reservation = new Reservation(
                    reservationDAO.generateUniquePnr(), name, train.get().getNumber(), train.get().getName(),
                    (String) classCombo.getSelectedItem(), Validator.parseJourneyDate(dateText),
                    source, destination, username, null);
            reservationDAO.insert(reservation);
            showConfirmation(reservation);
            clearForm();
        } catch (SQLException ex) {
            UiTheme.showDbError(this, ex);
        }
    }

    /** Shows a validation warning and puts the cursor in the offending field. */
    private void reject(String message, JComponent field) {
        UiTheme.showWarning(this, message);
        field.requestFocusInWindow();
    }

    /** Displays the booking confirmation dialog with all ticket details. */
    private void showConfirmation(Reservation r) {
        JTextArea text = new JTextArea(String.join("\n",
                "PNR              : " + r.getPnr(),
                "Passenger        : " + r.getPassengerName(),
                "Train            : " + r.getTrainNumber() + " - " + r.getTrainName(),
                "Class            : " + r.getClassType(),
                "Date of Journey  : " + r.getJourneyDate().format(Validator.DATE_FORMAT),
                "From             : " + r.getSource(),
                "To               : " + r.getDestination(),
                "",
                "Please note your PNR; it is needed to cancel the ticket."));
        text.setEditable(false);
        text.setOpaque(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        text.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JOptionPane.showMessageDialog(this, text, "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Resets every field to its initial state. */
    private void clearForm() {
        passengerField.setText("");
        trainNumberField.setText("");
        resetTrainDisplay();
        classCombo.setSelectedIndex(0);
        dateField.setText("");
        sourceField.setText("");
        destinationField.setText("");
        passengerField.requestFocusInWindow();
    }

    /** Document filter that only accepts digits (at most 9) in the train number field. */
    private static final class DigitsOnlyFilter extends DocumentFilter {
        private static final int MAX_LENGTH = 9;

        @Override
        public void insertString(FilterBypass fb, int offset, String s, AttributeSet attr)
                throws BadLocationException {
            replace(fb, offset, 0, s, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String s, AttributeSet attr)
                throws BadLocationException {
            String text = s == null ? "" : s;
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) < '0' || text.charAt(i) > '9') {
                    return; // reject the whole edit if any character is not an ASCII digit
                }
            }
            if (fb.getDocument().getLength() - length + text.length() > MAX_LENGTH) {
                return;
            }
            super.replace(fb, offset, length, text, attr);
        }
    }
}
