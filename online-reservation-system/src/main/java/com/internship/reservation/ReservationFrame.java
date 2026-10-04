package com.internship.reservation;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public class ReservationFrame extends JFrame {
    private final SecureRandom random = new SecureRandom();

    public ReservationFrame() {
        super("Online Train Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 440);
        setLocationRelativeTo(null);
    }

    public void showLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(35, 55, 35, 55));
        JTextField username = new JTextField(20);
        javax.swing.JPasswordField password = new javax.swing.JPasswordField(20);
        JButton login = new JButton("Login");
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(8, 8, 8, 8); c.anchor = GridBagConstraints.WEST;
        addRow(panel, c, 0, "Username", username); addRow(panel, c, 1, "Password", password);
        c.gridx = 1; c.gridy = 2; panel.add(login, c);
        login.addActionListener(e -> {
            String pass = new String(password.getPassword());
            if ("admin".equals(username.getText().trim()) && "admin123".equals(pass)) showReservationScreen();
            else JOptionPane.showMessageDialog(this, "Invalid username or password.", "Access denied", JOptionPane.ERROR_MESSAGE);
        });
        setContentPane(panel); setVisible(true);
    }

    private void showReservationScreen() {
        JPanel booking = new JPanel(new GridBagLayout()); booking.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));
        JTextField passenger = new JTextField(22), trainNo = new JTextField(22), trainName = new JTextField(22);
        JTextField travelClass = new JTextField(22), journeyDate = new JTextField(22), source = new JTextField(22), destination = new JTextField(22);
        trainName.setEditable(false); travelClass.setEditable(false); source.setEditable(false); destination.setEditable(false);
        journeyDate.setToolTipText("Use YYYY-MM-DD format"); journeyDate.setText(LocalDate.now().plusDays(1).toString());
        JButton lookup = new JButton("Load train details"), book = new JButton("Book reservation");
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5, 6, 5, 6); c.anchor = GridBagConstraints.WEST;
        addRow(booking,c,0,"Passenger name",passenger); addRow(booking,c,1,"Train number",trainNo);
        c.gridx=2; c.gridy=1; booking.add(lookup,c);
        addRow(booking,c,2,"Train name",trainName); addRow(booking,c,3,"Class",travelClass);
        addRow(booking,c,4,"Journey date (YYYY-MM-DD)",journeyDate); addRow(booking,c,5,"From",source); addRow(booking,c,6,"To",destination);
        c.gridx=1; c.gridy=7; booking.add(book,c);
        lookup.addActionListener(e -> loadTrain(trainNo, trainName, travelClass, source, destination));
        book.addActionListener(e -> book(passenger, trainNo, trainName, travelClass, journeyDate, source, destination));

        JPanel cancel = new JPanel(new GridBagLayout()); cancel.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));
        JTextField pnr = new JTextField(22); JButton fetch = new JButton("Fetch booking"), cancelBooking = new JButton("Cancel booking");
        javax.swing.JTextArea details = new javax.swing.JTextArea(7, 42); details.setEditable(false); details.setLineWrap(true); details.setWrapStyleWord(true);
        GridBagConstraints cc = new GridBagConstraints(); cc.insets=new Insets(7,7,7,7); cc.anchor=GridBagConstraints.WEST;
        cc.gridx=0;cc.gridy=0;cancel.add(new JLabel("PNR number"),cc);cc.gridx=1;cancel.add(pnr,cc);cc.gridx=2;cancel.add(fetch,cc);
        cc.gridx=0;cc.gridy=1;cc.gridwidth=3;cancel.add(new javax.swing.JScrollPane(details),cc);
        cc.gridx=1;cc.gridy=2;cc.gridwidth=1;cancel.add(cancelBooking,cc);
        final String[] loadedPnr = {null};
        fetch.addActionListener(e -> {
            try { Reservation r = findReservation(pnr.getText().trim());
                if (r == null) { loadedPnr[0]=null; details.setText(""); error("No reservation found for that PNR."); }
                else { loadedPnr[0]=r.pnr; details.setText(r.toString()); }
            } catch (SQLException ex) { showError(ex); }
        });
        cancelBooking.addActionListener(e -> {
            if (loadedPnr[0] == null || !loadedPnr[0].equals(pnr.getText().trim())) { error("Fetch a valid booking first."); return; }
            int answer=JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this booking?\n\n"+details.getText(), "Confirm cancellation", JOptionPane.YES_NO_OPTION);
            if(answer==JOptionPane.YES_OPTION) try(Connection con=Database.connect();PreparedStatement ps=con.prepareStatement("DELETE FROM reservations WHERE pnr=?")) {
                ps.setString(1,loadedPnr[0]); int deleted=ps.executeUpdate();
                if(deleted==1){JOptionPane.showMessageDialog(this,"Booking cancelled."); details.setText("");pnr.setText("");loadedPnr[0]=null;}
                else error("That booking was already removed.");
            } catch(SQLException ex){showError(ex);}
        });
        JTabbedPane tabs=new JTabbedPane();tabs.addTab("New reservation",booking);tabs.addTab("Cancel reservation",cancel);
        setContentPane(tabs); revalidate(); repaint();
    }

    private void loadTrain(JTextField no,JTextField name,JTextField travelClass,JTextField source,JTextField destination) {
        String sql="SELECT train_name,travel_class,source,destination FROM trains WHERE train_number=?";
        try(Connection con=Database.connect();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setString(1,no.getText().trim());try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){name.setText(rs.getString(1));travelClass.setText(rs.getString(2));source.setText(rs.getString(3));destination.setText(rs.getString(4));}
                else {name.setText("");travelClass.setText("");source.setText("");destination.setText("");error("Train number not found. Add it to the trains table first.");}
            }
        }catch(SQLException ex){showError(ex);}
    }

    private void book(JTextField passenger,JTextField trainNo,JTextField trainName,JTextField travelClass,JTextField date,JTextField source,JTextField destination){
        String person=passenger.getText().trim(), number=trainNo.getText().trim(), journey=date.getText().trim();
        if(person.isEmpty()||number.isEmpty()||journey.isEmpty()){error("Please fill all required fields.");return;}
        if(!number.matches("\\d+")){error("Train number must contain digits only.");return;}
        try{if(LocalDate.parse(journey).isBefore(LocalDate.now())){error("Journey date cannot be in the past.");return;}}
        catch(DateTimeParseException ex){error("Enter the date as YYYY-MM-DD.");return;}
        if(trainName.getText().isEmpty()){error("Load a valid train before booking.");return;}
        String pnr;
        try(Connection con=Database.connect()){
            do{pnr=String.format("PNR%010d",Math.abs(random.nextLong()%10000000000L));}
            while(pnrExists(con,pnr));
            String sql="INSERT INTO reservations(pnr,passenger_name,train_number,train_name,travel_class,journey_date,source,destination) VALUES(?,?,?,?,?,?,?,?)";
            try(PreparedStatement ps=con.prepareStatement(sql)){
                ps.setString(1,pnr);ps.setString(2,person);ps.setString(3,number);ps.setString(4,trainName.getText());
                ps.setString(5,travelClass.getText());ps.setString(6,journey);ps.setString(7,source.getText());ps.setString(8,destination.getText());ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this,"Reservation successful!\n\nPNR: "+pnr+"\nPassenger: "+person+"\nTrain: "+trainName.getText()+" ("+number+")\nClass: "+travelClass.getText()+"\nDate: "+journey+"\nRoute: "+source.getText()+" → "+destination.getText(),"Booking confirmed",JOptionPane.INFORMATION_MESSAGE);
            passenger.setText("");
        }catch(SQLException ex){showError(ex);}
    }

    private boolean pnrExists(Connection con,String pnr)throws SQLException{
        try(PreparedStatement ps=con.prepareStatement("SELECT 1 FROM reservations WHERE pnr=?")){ps.setString(1,pnr);try(ResultSet rs=ps.executeQuery()){return rs.next();}}
    }
    private Reservation findReservation(String pnr)throws SQLException{
        if(pnr.isEmpty())return null;
        try(Connection con=Database.connect();PreparedStatement ps=con.prepareStatement("SELECT * FROM reservations WHERE pnr=?")){
            ps.setString(1,pnr);try(ResultSet rs=ps.executeQuery()){if(!rs.next())return null;
                return new Reservation(rs.getString("pnr"),rs.getString("passenger_name"),rs.getString("train_number"),rs.getString("train_name"),rs.getString("travel_class"),rs.getString("journey_date"),rs.getString("source"),rs.getString("destination"));}
        }
    }
    private void addRow(JPanel panel,GridBagConstraints c,int row,String label,javax.swing.JComponent field){
        c.gridwidth=1;c.gridx=0;c.gridy=row;panel.add(new JLabel(label),c);c.gridx=1;panel.add(field,c);
    }
    private void error(String message){JOptionPane.showMessageDialog(this,message,"Please check",JOptionPane.WARNING_MESSAGE);}
    private void showError(Exception ex){JOptionPane.showMessageDialog(this,"Operation failed: "+ex.getMessage(),"Database error",JOptionPane.ERROR_MESSAGE);}

    private static class Reservation{
        final String pnr,person,number,name,travelClass,date,source,destination;
        Reservation(String p,String passenger,String n,String train,String cls,String d,String from,String to){pnr=p;person=passenger;number=n;name=train;travelClass=cls;date=d;source=from;destination=to;}
        @Override public String toString(){return "PNR: "+pnr+"\nPassenger: "+person+"\nTrain: "+name+" ("+number+")\nClass: "+travelClass+"\nDate: "+date+"\nRoute: "+source+" → "+destination;}
    }
}