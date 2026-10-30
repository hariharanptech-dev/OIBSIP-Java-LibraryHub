package com.reservation.ui;

import com.reservation.dao.ReservationDAO;
import com.reservation.model.Reservation;
import com.reservation.util.Validator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Optional;

/** Cancellation screen: fetch a booking by PNR, review it, then confirm the cancellation. */
@SuppressWarnings({"serial", "this-escape"})
public class CancellationForm extends JFrame {

    private static final long serialVersionUID = 1L;

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final JTextField pnrField = new JTextField(20);
    private final JLabel passengerValue = UiTheme.valueLabel();
    private final JLabel trainValue = UiTheme.valueLabel();
    private final JLabel classValue = UiTheme.valueLabel();
    private final JLabel dateValue = UiTheme.valueLabel();
    private final JLabel routeValue = UiTheme.valueLabel();
    private final JLabel bookedByValue = UiTheme.valueLabel();
    private final JLabel bookedAtValue = UiTheme.valueLabel();
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton confirmButton = UiTheme.button("Confirm Cancellation", UiTheme.DANGER);

    /** The booking currently displayed, or null if nothing has been fetched. */
    private Reservation fetched;

    /**
     * Builds the cancellation form.
     *
     * @param menu the main menu to return to when this window closes
     */
    public CancellationForm(MainMenu menu) {
        super("Online Reservation System - Cancel Ticket");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                menu.setVisible(true);
            }
        });

        setLayout(new BorderLayout());
        add(UiTheme.header("Cancel a Ticket", "Enter your PNR to look up the booking"), BorderLayout.NORTH);

        UiTheme.style(pnrField);
        statusLabel.setFont(UiTheme.BODY_FONT);
        JButton fetchButton = UiTheme.button("Fetch Details", UiTheme.PRIMARY);

        JPanel card = UiTheme.card();
        UiTheme.addRow(card, 0, "PNR Number", pnrField);
        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1;
        fc.gridy = 1;
        fc.anchor = GridBagConstraints.WEST;
        fc.insets = new Insets(4, 0, 4, 0);
        card.add(fetchButton, fc);
        GridBagConstraints sc = new GridBagConstraints();
        sc.gridx = 1;
        sc.gridy = 2;
        sc.anchor = GridBagConstraints.WEST;
        sc.insets = new Insets(2, 0, 8, 0);
        card.add(statusLabel, sc);

        UiTheme.addRow(card, 3, "Passenger", passengerValue);
        UiTheme.addRow(card, 4, "Train", trainValue);
        UiTheme.addRow(card, 5, "Class", classValue);
        UiTheme.addRow(card, 6, "Date of Journey", dateValue);
        UiTheme.addRow(card, 7, "Route", routeValue);
        UiTheme.addRow(card, 8, "Booked By", bookedByValue);
        UiTheme.addRow(card, 9, "Booked At", bookedAtValue);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);
        JButton back = UiTheme.button("Back", UiTheme.SECONDARY);
        buttons.add(confirmButton);
        buttons.add(back);
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0;
        bc.gridy = 10;
        bc.gridwidth = 2;
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.insets = new Insets(16, 0, 0, 0);
        card.add(buttons, bc);
        add(UiTheme.pad(card), BorderLayout.CENTER);

        confirmButton.setEnabled(false);

        fetchButton.addActionListener(e -> fetchDetails());
        pnrField.addActionListener(e -> fetchDetails());
        confirmButton.addActionListener(e -> confirmCancellation());
        back.addActionListener(e -> dispose());
        // Editing the PNR after a fetch invalidates the displayed booking.
        pnrField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { clearDetails(); }
            @Override public void removeUpdate(DocumentEvent e) { clearDetails(); }
            @Override public void changedUpdate(DocumentEvent e) { clearDetails(); }
        });

        UiTheme.prepare(this, 560, 640);
    }

    /** Looks the PNR up and shows the booking details, or a "No booking found" message. */
    private void fetchDetails() {
        String error = Validator.validatePnr(pnrField.getText());
        if (error != null) {
            UiTheme.showWarning(this, error);
            pnrField.requestFocusInWindow();
            return;
        }
        String pnr = pnrField.getText().trim().toUpperCase();
        try {
            Optional<Reservation> found = reservationDAO.findByPnr(pnr);
            if (found.isEmpty()) {
                clearDetails();
                statusLabel.setForeground(UiTheme.DANGER);
                statusLabel.setText("No booking found for this PNR");
                JOptionPane.showMessageDialog(this, "No booking found for this PNR",
                        "Not Found", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            fetched = found.get();
            passengerValue.setText(fetched.getPassengerName());
            trainValue.setText(fetched.getTrainNumber() + " - " + fetched.getTrainName());
            classValue.setText(fetched.getClassType());
            dateValue.setText(fetched.getJourneyDate().format(Validator.DATE_FORMAT));
            routeValue.setText(fetched.getSource() + "  \u2192  " + fetched.getDestination());
            bookedByValue.setText(fetched.getBookedBy());
            bookedAtValue.setText(fetched.getBookedAt() == null ? "-" : fetched.getBookedAt() + " (UTC)");
            statusLabel.setForeground(UiTheme.SUCCESS);
            statusLabel.setText("Booking found");
            confirmButton.setEnabled(true);
        } catch (SQLException ex) {
            UiTheme.showDbError(this, ex);
        }
    }

    /** Asks for confirmation, then deletes the fetched booking. */
    private void confirmCancellation() {
        if (fetched == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this booking?\n\nPNR: " + fetched.getPnr()
                        + "\nPassenger: " + fetched.getPassengerName(),
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            boolean deleted = reservationDAO.deleteByPnr(fetched.getPnr());
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Cancellation successful",
                        "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "This booking no longer exists.",
                        "Not Found", JOptionPane.INFORMATION_MESSAGE);
            }
            resetForm();
        } catch (SQLException ex) {
            UiTheme.showDbError(this, ex);
        }
    }

    /** Hides the displayed booking and disables the confirm button. */
    private void clearDetails() {
        fetched = null;
        for (JLabel l : new JLabel[]{passengerValue, trainValue, classValue, dateValue,
                routeValue, bookedByValue, bookedAtValue}) {
            l.setText(" ");
        }
        statusLabel.setText(" ");
        confirmButton.setEnabled(false);
    }

    /** Returns the form to its initial state. */
    private void resetForm() {
        pnrField.setText(""); // also triggers clearDetails() through the document listener
        clearDetails();
        pnrField.requestFocusInWindow();
    }
}
