
package editor.converter;

import java.awt.*;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;

import editor.nsbtx2.*;
import editor.nsbtx2.Nsbtx2;

/**
 * @author Trifindo
 */
public class ExportNsbtxResultDialog extends javax.swing.JDialog {

    /**
     * Creates new form ExportNsbtxResultDialog
     */
    public ExportNsbtxResultDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        getRootPane().setDefaultButton(jButton1);
        jButton1.requestFocus();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        nsbtxPanel1 = new NsbtxPanel();
        jLabel1 = new JLabel();
        jPanel2 = new JPanel();
        jButton1 = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("NSBTX Successfully Saved");
        setResizable(false);
        setModal(true);
        Container contentPane = getContentPane();

        //---- jLabel1 ----
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setText("NSBTX succesfully exported.");

        //======== jPanel2 ========
        {
            jPanel2.setLayout(new GridBagLayout());

            //---- jButton1 ----
            jButton1.setText("OK");
            jButton1.addActionListener(e -> jButton1ActionPerformed(e));
            jPanel2.add(jButton1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addComponent(nsbtxPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(nsbtxPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private NsbtxPanel nsbtxPanel1;
    private JLabel jLabel1;
    private JPanel jPanel2;
    private JButton jButton1;
    // End of variables declaration//GEN-END:variables

    public void init(Nsbtx2 nsbtx) {
        nsbtxPanel1.setNsbtx(nsbtx);
        nsbtxPanel1.updateViewTextureNameList(0);
        nsbtxPanel1.updateViewPaletteNameList(0);
        nsbtxPanel1.updateView();
    }
}
