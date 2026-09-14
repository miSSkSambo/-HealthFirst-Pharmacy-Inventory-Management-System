package com.healthfirst.pims.ui;

import com.healthfirst.pims.model.CartItem;
import com.healthfirst.pims.model.SaleResult;
import com.healthfirst.pims.util.FormatUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;

/** Printable bill window displayed following a successful cashier checkout. */
public final class ReceiptDialog extends JDialog {
    private final JTextArea receipt = new JTextArea();
    public ReceiptDialog(Window owner, SaleResult sale) {
        super(owner,"HealthFirst PIMS — Bill #"+sale.saleId(),ModalityType.APPLICATION_MODAL);setSize(520,600);setLocationRelativeTo(owner);build(sale);
    }
    private void build(SaleResult sale){receipt.setEditable(false);receipt.setFont(new Font(Font.MONOSPACED,Font.PLAIN,13));receipt.setText(render(sale));add(new JScrollPane(receipt),BorderLayout.CENTER);JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));JButton print=UiStyle.button("Print bill",UiStyle.NAVY),close=new JButton("Close");print.addActionListener(e->print());close.addActionListener(e->dispose());actions.add(print);actions.add(close);add(actions,BorderLayout.SOUTH);}
    private String render(SaleResult sale){StringBuilder s=new StringBuilder();s.append("             HEALTHFIRST PHARMACY\n").append("          Inventory Management System\n").append("------------------------------------------------\n").append(String.format("Bill number: %d%n",sale.saleId())).append("Date: ").append(FormatUtil.dateTime(sale.saleDate())).append("\nCashier: ").append(sale.cashierName()).append("\n------------------------------------------------\n");for(CartItem i:sale.items())s.append(String.format("%-20s %3d x %9s%n",i.medicine().name(),i.quantity(),FormatUtil.money(i.medicine().price()))).append(String.format("%-24s %15s%n","",FormatUtil.money(i.lineTotal())));s.append("------------------------------------------------\n").append(String.format("%-25s %14s%n","TOTAL",FormatUtil.money(sale.totalAmount()))).append("\nThank you for choosing HealthFirst Pharmacy.\n");return s.toString();}
    private void print(){try{if(receipt.print())Ui.info(this,"Bill sent to the selected printer.");}catch(PrinterException e){Ui.error(this,e);}}
}
