package com.terheyden.xdemos;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sf.saxon.s9api.XdmValue;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class XPathUI {

    private JFrame frmXpathUi;
    private JTextField inputTextField;
    private JTextArea xmlTextArea;
    private JTextArea resultsTextArea;

    private void processXml() {
        try {

            String searchInput = inputTextField.getText();
            String xmlInput = xmlTextArea.getText();
            XdmValue xpathResults = XPathUtils.xpathSearch(xmlInput, searchInput);
            resultsTextArea.setText("");

            if (searchInput == null || searchInput.trim().isEmpty() || xmlInput == null || xmlInput.trim().isEmpty() || xpathResults == null) {
                return;
            }

            // Parse the results as XML:
            String xmlOutput = XPathUtils.getXdmValueAsXML(xpathResults);

            // Also parse the results as a list of Strings, for the devs:
            StringBuilder builder = new StringBuilder();
            List<String> listResults = XPathUtils.getXdmValueAsList(xpathResults);
            for (String item : listResults) {
                builder.append(String.format("\"%s\"\n", item));
            }

            if (builder.length() == 0) {
                builder.append("(no results)\n");
            }

            resultsTextArea.setText(String.format("%s\n\ngetXdmValueAsList() results (for devs):\n%s",
                xmlOutput, builder.toString()));

        } catch (Exception e) {
            resultsTextArea.setText(e.getLocalizedMessage());
        }
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    XPathUI window = new XPathUI();
                    window.frmXpathUi.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public XPathUI() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmXpathUi = new JFrame();
        frmXpathUi.setTitle("XPath UI");
        frmXpathUi.setBounds(100, 100, 800, 600);
        frmXpathUi.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmXpathUi.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormFactory.RELATED_GAP_COLSPEC,},
            new RowSpec[] {
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("pref:grow"),
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,}));

        JLabel lblXml = new JLabel("XML:");
        frmXpathUi.getContentPane().add(lblXml, "2, 2");

                JLabel lblResults = new JLabel("Results:");
                frmXpathUi.getContentPane().add(lblResults, "6, 2");

        JScrollPane xmlScrollPane = new JScrollPane();
        frmXpathUi.getContentPane().add(xmlScrollPane, "2, 4, fill, fill");

        xmlTextArea = new JTextArea();
        xmlTextArea.setText("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n\r\n<bookstore>\r\n\r\n<book>\r\n  <title lang=\"eng\">Harry Potter</title>\r\n  <price>29.99</price>\r\n</book>\r\n\r\n<book>\r\n  <title lang=\"eng\">Learning XML</title>\r\n  <price>39.95</price>\r\n</book>\r\n\r\n<novel>\r\n  <title lang=\"fr\">Learning French</title>\r\n  <price>30.95</price>\r\n</novel>\r\n\r\n</bookstore>\r\n");
        xmlTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        xmlScrollPane.setViewportView(xmlTextArea);

        JScrollPane resultsScrollPane = new JScrollPane();
        frmXpathUi.getContentPane().add(resultsScrollPane, "6, 4, fill, fill");

        resultsTextArea = new JTextArea();
        resultsTextArea.setText("Things to try:\r\n\r\n/bookstore/book - get all books\r\n//book - get all books\r\n//novel - get all novels\r\n\r\n//@lang - get all \"lang\" attributes\r\n//title/@lang - get all title \"lang\" attributes\r\n//book//@lang - get book ... \"lang\" attributes\r\n\r\n//title - get all book titles\r\n//book/title - get only book titles\r\n");
        resultsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultsScrollPane.setViewportView(resultsTextArea);

        inputTextField = new JTextField();
        inputTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    processXml();
                }
            }
        });
        inputTextField.setText("//book");
        inputTextField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        frmXpathUi.getContentPane().add(inputTextField, "2, 6, fill, default");
        inputTextField.setColumns(10);

        JButton btnRun = new JButton("Run!");
        btnRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processXml();
            }
        });
        frmXpathUi.getContentPane().add(btnRun, "6, 6");
        frmXpathUi.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{inputTextField, xmlTextArea, resultsTextArea}));
        frmXpathUi.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{inputTextField, btnRun, xmlTextArea, resultsTextArea}));
    }
    public JTextArea getXmlTextArea() {
        return xmlTextArea;
    }
    public JTextArea getResultsTextArea() {
        return resultsTextArea;
    }
}
