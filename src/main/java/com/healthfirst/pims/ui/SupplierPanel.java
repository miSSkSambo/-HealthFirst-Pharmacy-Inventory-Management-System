package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.SupplierDao;
import com.healthfirst.pims.model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

/** Full CRUD panel for supplier data. */
public final class SupplierPanel extends JPanel {
    private final SupplierDao dao = new SupplierDao(); private final JTextField name=new JTextField(16), contact=new JTextField(15), phone=new JTextField(12), email=new JTextField(16), address=new JTextField(20); private int selectedId=-1;
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"ID","Supplier","Contact person","Phone","Email","Address"},0){public boolean isCellEditable(int r,int c){return false;}}; private final JTable table=new JTable(model);
    public SupplierPanel(){super(new BorderLayout(10,10));setBackground(UiStyle.BG);setBorder(BorderFactory.createEmptyBorder(14,14,14,14));UiStyle.configureTable(table);table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())select();});add(new JScrollPane(table),BorderLayout.CENTER);JPanel lower=new JPanel(new BorderLayout(8,8));lower.setBackground(UiStyle.BG);JPanel form=new JPanel(new GridLayout(1,5,8,2));form.setBackground(UiStyle.BG);field(form,"Name",name);field(form,"Contact person",contact);field(form,"Phone",phone);field(form,"Email",email);field(form,"Address",address);lower.add(form,BorderLayout.CENTER);JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));actions.setBackground(UiStyle.BG);JButton add=UiStyle.button("Add supplier",UiStyle.GREEN),edit=UiStyle.button("Update selected",UiStyle.BLUE),del=UiStyle.button("Delete selected",UiStyle.RED),clear=new JButton("Clear form");add.addActionListener(e->save(false));edit.addActionListener(e->save(true));del.addActionListener(e->delete());clear.addActionListener(e->clear());actions.add(add);actions.add(edit);actions.add(del);actions.add(clear);lower.add(actions,BorderLayout.SOUTH);add(lower,BorderLayout.SOUTH);load();}
    private void field(JPanel p,String label,JTextField f){JPanel x=new JPanel(new BorderLayout(2,2));x.setBackground(UiStyle.BG);x.add(new JLabel(label),BorderLayout.NORTH);f.setBorder(UiStyle.fieldBorder());x.add(f,BorderLayout.CENTER);p.add(x);}
    private void load(){try{model.setRowCount(0);for(Supplier s:dao.findAll())model.addRow(new Object[]{s.id(),s.name(),s.contactPerson(),s.phone(),s.email(),s.address()});}catch(SQLException e){Ui.error(this,e);}}
    private void select(){int row=table.getSelectedRow();if(row<0)return;row=table.convertRowIndexToModel(row);selectedId=(int)model.getValueAt(row,0);name.setText((String)model.getValueAt(row,1));contact.setText((String)model.getValueAt(row,2));phone.setText((String)model.getValueAt(row,3));email.setText((String)model.getValueAt(row,4));address.setText((String)model.getValueAt(row,5));}
    private void save(boolean edit){try{if(name.getText().isBlank())throw new IllegalArgumentException("Supplier name is required.");if(edit&&selectedId<0)throw new IllegalArgumentException("Select a supplier to update.");if(edit)dao.update(selectedId,name.getText(),contact.getText(),phone.getText(),email.getText(),address.getText());else dao.create(name.getText(),contact.getText(),phone.getText(),email.getText(),address.getText());Ui.info(this,edit?"Supplier updated.":"Supplier added.");clear();load();}catch(Exception e){Ui.error(this,e);}}
    private void delete(){try{if(selectedId<0)throw new IllegalArgumentException("Select a supplier to delete.");if(Ui.confirm(this,"Delete this supplier? Associated medicines must be reassigned first.")){dao.delete(selectedId);clear();load();Ui.info(this,"Supplier deleted.");}}catch(Exception e){Ui.error(this,e);}}
    private void clear(){selectedId=-1;table.clearSelection();name.setText("");contact.setText("");phone.setText("");email.setText("");address.setText("");}
}
