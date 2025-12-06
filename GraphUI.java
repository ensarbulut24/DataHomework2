package src;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

public class GraphUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField txtFilePath;
    private JTextField txtThreshold;
    private JTextField txtStartNode;
    private JTextArea txtConsole;
    private JButton btnLoad, btnMetrics, btnBFS;
    
    private PPIGraph graph;

    public GraphUI() {
        setTitle("CME 2201 - PPI Graph Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 900, 600);
        setLayout(new BorderLayout(10, 10));

        graph = new PPIGraph();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        txtFilePath = new JTextField("9606.protein.links.v12.0.txt", 20); 
        JButton btnBrowse = new JButton("Browse...");
        
        JLabel lblTh = new JLabel("Threshold (0.0-1.0):");
        txtThreshold = new JTextField("0.7", 5);
        btnLoad = new JButton("LOAD GRAPH");
        btnLoad.setBackground(new Color(60, 179, 113));
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);

        topPanel.add(new JLabel("File Path:"));
        topPanel.add(txtFilePath);
        topPanel.add(btnBrowse);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(lblTh);
        topPanel.add(txtThreshold);
        topPanel.add(btnLoad);

        add(topPanel, BorderLayout.NORTH);

        txtConsole = new JTextArea();
        txtConsole.setEditable(false);
        txtConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtConsole.setBackground(new Color(30, 30, 30));
        txtConsole.setForeground(new Color(200, 200, 200));
        
        DefaultCaret caret = (DefaultCaret)txtConsole.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(txtConsole);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Console Output"));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        btnMetrics = new JButton("Calculate Metrics");
        
        JLabel lblStart = new JLabel("Protein ID:");
        txtStartNode = new JTextField(15);
        btnBFS = new JButton("Start BFS");

        bottomPanel.add(btnMetrics);
        bottomPanel.add(Box.createHorizontalStrut(30));
        bottomPanel.add(lblStart);
        bottomPanel.add(txtStartNode);
        bottomPanel.add(btnBFS);

        add(bottomPanel, BorderLayout.SOUTH);

        redirectSystemStreams();

        btnBrowse.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                txtFilePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnLoad.addActionListener(e -> {
            String path = txtFilePath.getText();
            String thStr = txtThreshold.getText();
            
            btnLoad.setEnabled(false);
            
            new Thread(() -> {
                try {
                    double threshold = Double.parseDouble(thStr);
                    graph.loadGraph(path, threshold);
                } catch (Exception ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                } finally {
                    SwingUtilities.invokeLater(() -> btnLoad.setEnabled(true));
                }
            }).start();
        });

        btnMetrics.addActionListener(e -> {
            graph.printMetrics();
        });

        btnBFS.addActionListener(e -> {
            String startId = txtStartNode.getText().trim();
            if(startId.isEmpty()) {
                System.out.println("Please enter a Protein ID!");
                return;
            }
            new Thread(() -> graph.BFS(startId)).start();
        });
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) {
                write(b, 0, b.length);
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(() -> txtConsole.append(text));
    }
}