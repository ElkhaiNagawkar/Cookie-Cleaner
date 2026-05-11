import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.*;
import java.util.HashSet;
import java.util.List;


public class CentrePanel extends JPanel {
    private final JTable activeCookies;
    private final JTable disabledCookies;
    private final JButton addToRemovedButton;
    private final JButton addToActiveButton;
    private final JButton scanButton;
    private CookieTableModel activeCookieModel;
    private CookieTableModel removedCookieModel;
    private JProgressBar progressBar = new JProgressBar();
    private HashSet<String> sharedCookieSet;
    private JCheckBox activeClean;
    private JButton clearButton;
    private JButton clearCookie;

    public CentrePanel(CookieScanEngine scanEngine){

        sharedCookieSet = new HashSet<>();
        activeCookieModel  = new CookieTableModel(new String[]{"Active Cookie Names"}, 0, sharedCookieSet);
        activeCookies = new JTable(activeCookieModel);
        activeCookies.setDefaultEditor(Object.class, null);
        activeCookies.setAutoCreateRowSorter(true);

        removedCookieModel  = new CookieTableModel(new String[]{"Removed Cookie Names"}, 0, sharedCookieSet);
        disabledCookies = new JTable(removedCookieModel);
        disabledCookies.setDefaultEditor(Object.class, null);
        disabledCookies.setAutoCreateRowSorter(true);

        activeCookies.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && activeCookies.getSelectedRow() != -1) {
                disabledCookies.clearSelection();
            }
        });

        disabledCookies.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && disabledCookies.getSelectedRow() != -1) {
                activeCookies.clearSelection();
            }
        });

        addToRemovedButton = new JButton("Add to removed →");
        addToActiveButton = new JButton("← Add to active");

        addToRemovedButton.addActionListener(e -> {
            if(activeCookies.getSelectedRow() == -1) return;
            String selectedCookie = activeCookies.getValueAt(activeCookies.getSelectedRow(), 0).toString();
            removedCookieModel.moveCookie(selectedCookie);
            activeCookieModel.removeCookie(activeCookies.getSelectedRow());
        });

        addToActiveButton.addActionListener(e -> {
            if(disabledCookies.getSelectedRow() == -1) return;

            String selectedCookie = disabledCookies.getValueAt(disabledCookies.getSelectedRow(), 0).toString();
            activeCookieModel.moveCookie(selectedCookie);
            removedCookieModel.removeCookie(disabledCookies.getSelectedRow());
        });


        activeClean = new JCheckBox("Active Clean");
        activeClean.setAlignmentX(Component.CENTER_ALIGNMENT);
        activeClean.setSelected(true);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");

        clearButton = new JButton("Clear All tables");
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        clearButton.addActionListener(e -> {
            activeCookieModel.setRowCount(0);
            removedCookieModel.setRowCount(0);
            sharedCookieSet.clear();
            activeCookieModel.cookieList.clear();
            removedCookieModel.cookieList.clear();
        });

        clearCookie = new JButton("Clear selected Cookie");
        clearCookie.setAlignmentX(Component.CENTER_ALIGNMENT);

        clearCookie.addActionListener(e -> {
            if (activeCookies.getSelectedRow() != -1) {
                String cookieName = activeCookieModel.getValueAt(activeCookies.getSelectedRow(), 0).toString();
                sharedCookieSet.remove(cookieName);
                activeCookieModel.cookieList.remove(cookieName);
                activeCookieModel.removeRow(activeCookies.getSelectedRow());
            } else if (disabledCookies.getSelectedRow() != -1) {
                String cookieName = removedCookieModel.getValueAt(disabledCookies.getSelectedRow(), 0).toString();
                sharedCookieSet.remove(cookieName);
                removedCookieModel.cookieList.remove(cookieName);
                removedCookieModel.removeRow(disabledCookies.getSelectedRow());
            }
        });

        scanButton = new JButton("scan");
        scanButton.addActionListener(e -> {
            scanButton.setEnabled(false);
            CookieScanWorker worker = new CookieScanWorker(scanEngine, activeCookieModel, scanButton, progressBar);
            worker.execute();
        });

        //******  Adding all components to screen  ******\\
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.45;
        gbc.weighty = 1.0;
        add(new JScrollPane(activeCookies), gbc);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        addToRemovedButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addToActiveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsPanel.add(Box.createVerticalGlue());
        buttonsPanel.add(addToRemovedButton);
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(addToActiveButton);
        buttonsPanel.add(Box.createVerticalStrut(30));
        buttonsPanel.add(activeClean);
        buttonsPanel.add(Box.createVerticalStrut(100));
        buttonsPanel.add(clearButton);
        buttonsPanel.add(Box.createVerticalStrut(30));
        buttonsPanel.add(clearCookie);
        buttonsPanel.add(Box.createVerticalGlue());

        gbc.gridx = 1;
        gbc.weightx = 0.1;
        gbc.fill = GridBagConstraints.NONE;
        add(buttonsPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.45;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(disabledCookies), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(scanButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(progressBar, gbc);
    }

    public List<String> getRemovedCookieList(){
        return removedCookieModel.cookieList;
    }

    public void addOrRemove(String cookieName){
        if(activeCookieModel.cookieList.contains(cookieName)){
            removedCookieModel.moveCookie(cookieName);
            activeCookieModel.removeCookieWithString(activeCookieModel, cookieName);
        }else{
            removedCookieModel.addCookie(cookieName);
            if(!removedCookieModel.cookieList.contains(cookieName)){
                removedCookieModel.cookieList.add(cookieName);
            }
        }
    }

    public boolean getActiveCleanVal(){
        return activeClean.isSelected();
    }
}
