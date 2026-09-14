package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.UserDao;
import com.healthfirst.pims.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

/** Administrator-only user account CRUD panel. */
public final class UserPanel extends JPanel {
    private final UserDao dao=new UserDao();private final User signedInUser;private final JTextField username=new JTextField(15),fullName=new JTextField(18);private final JPasswordField password=new JPasswordField(15);private final JComboBox<String> role=new JComboBox<>(new String[]{"Cashier","Admin"});private int selectedId=-1;
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"ID","Username","Full name","Role"},0){public boolean isCellEditable(int r,int c){return false;}};private final JTable table=new JTable(model);
    public UserPanel(User signedInUser){super(new BorderLayout(10,10));this.signedInUser=signedInUser;setBackground(UiStyle.BG);setBorder(BorderFactory.createEmptyBorder(14,14,14,14));UiStyle.configureTable(table);table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())select();});add(new JScrollPane(table),BorderLayout.CENTER);JPanel lower=new JPanel(new BorderLayout(8,8));lower.setBackground(UiStyle.BG);JPanel form=new JPanel(new GridLayout(1,4,8,2));form.setBackground(UiStyle.BG);field(form,"Username",username);field(form,"Full name",fullName);field(form,"Role",role);field(form,"Password (blank = keep)",password);lower.add(form,BorderLayout.CENTER);JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));actions.setBackground(UiStyle.BG);JButton add=UiStyle.button("Create user",UiStyle.GREEN),edit=UiStyle.button("Update selected",UiStyle.BLUE),del=UiStyle.button("Delete selected",UiStyle.RED),clear=new JButton("Clear form");add.addActionListener(e->save(false));edit.addActionListener(e->save(true));del.addActionListener(e->delete());clear.addActionListener(e->clear());actions.add(add);actions.add(edit);actions.add(del);actions.add(clear);lower.add(actions,BorderLayout.SOUTH);add(lower,BorderLayout.SOUTH);load();}
    private void field(JPanel p,String label,JComponent f){JPanel x=new JPanel(new BorderLayout(2,2));x.setBackground(UiStyle.BG);x.add(new JLabel(label),BorderLayout.NORTH);f.setBorder(UiStyle.fieldBorder());x.add(f,BorderLayout.CENTER);p.add(x);}
    private void load(){try{model.setRowCount(0);for(User u:dao.findAll())model.addRow(new Object[]{u.id(),u.username(),u.fullName(),u.role()});}catch(SQLException e){Ui.error(this,e);}}
    private void select(){int row=table.getSelectedRow();if(row<0)return;row=table.convertRowIndexToModel(row);selectedId=(int)model.getValueAt(row,0);username.setText((String)model.getValueAt(row,1));fullName.setText((String)model.getValueAt(row,2));role.setSelectedItem(model.getValueAt(row,3));password.setText("");}
    private void save(boolean edit){try{String u=username.getText().trim(),n=fullName.getText().trim(),p=new String(password.getPassword());if(u.isBlank()||n.isBlank())throw new IllegalArgumentException("Username and full name are required.");if(!edit&&p.isBlank())throw new IllegalArgumentException("A password is required for a new user.");if(edit&&selectedId<0)throw new IllegalArgumentException("Select a user to update.");if(edit)dao.update(selectedId,u,n,(String)role.getSelectedItem(),p);else dao.create(u,n,(String)role.getSelectedItem(),p);Ui.info(this,edit?"User updated.":"User account created.");clear();load();}catch(Exception e){Ui.error(this,e);}}
    private void delete(){try{if(selectedId<0)throw new IllegalArgumentException("Select a user to delete.");if(selectedId==signedInUser.id())throw new IllegalArgumentException("The signed-in administrator cannot delete their own account.");if(Ui.confirm(this,"Delete this user account?")){dao.delete(selectedId);clear();load();Ui.info(this,"User deleted.");}}catch(Exception e){Ui.error(this,e);}}
    private void clear(){selectedId=-1;table.clearSelection();username.setText("");fullName.setText("");password.setText("");role.setSelectedIndex(0);}
}
