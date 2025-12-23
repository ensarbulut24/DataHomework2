package src;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class GraphUI extends JFrame {
    JTextField tfPath, tfTh, tfId1, tfId2;
    JTextArea area;
    PPIGraph graph;

    public GraphUI() {
        setTitle("PPI Assignment (Edge List)");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        graph = new PPIGraph();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfPath = new JTextField("9606.protein.links.v12.0.txt", 15);
        JButton btnBrowse = new JButton("Browse");
        tfTh = new JTextField("0.9", 4);
        JButton btnLoad = new JButton("Load");
        JButton btnMet = new JButton("Metrics");

        top.add(new JLabel("File:")); top.add(tfPath); top.add(btnBrowse);
        top.add(new JLabel("Th:")); top.add(tfTh);
        top.add(btnLoad); top.add(btnMet);
        add(top, BorderLayout.NORTH);

        area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel bot = new JPanel(new GridLayout(2, 1));
        JPanel r1 = new JPanel();
        JPanel r2 = new JPanel();

        tfId1 = new JTextField(10);
        tfId2 = new JTextField(10);
        JButton btnBFS = new JButton("BFS");
        JButton btnDFS = new JButton("DFS");
        JButton btnPaths = new JButton("FIND PATHS");
        JButton btnSearch = new JButton("Search");
        JButton btnCheck = new JButton("Check");

        r1.add(new JLabel("Start:")); r1.add(tfId1);
        r1.add(btnSearch); r1.add(btnBFS); r1.add(btnDFS);

        r2.add(new JLabel("End:")); r2.add(tfId2);
        r2.add(btnCheck); r2.add(btnPaths);

        bot.add(r1); bot.add(r2);
        add(bot, BorderLayout.SOUTH);

        PrintStream out = new PrintStream(new OutputStream() {
            public void write(int b) { area.append(String.valueOf((char)b)); area.setCaretPosition(area.getDocument().getLength()); }
            public void write(byte[] b, int o, int l) { area.append(new String(b, o, l)); area.setCaretPosition(area.getDocument().getLength()); }
        });
        System.setOut(out); System.setErr(out);

        btnBrowse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(".");
            if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) tfPath.setText(fc.getSelectedFile().getAbsolutePath());
        });

        btnLoad.addActionListener(e -> new Thread(() -> {
            try { graph.load(tfPath.getText(), Double.parseDouble(tfTh.getText())); } 
            catch(Exception ex) { System.out.println("Error."); }
        }).start());

        btnMet.addActionListener(e -> new Thread(() -> graph.metrics()).start());
        btnBFS.addActionListener(e -> new Thread(() -> graph.bfs(tfId1.getText().trim())).start());
        btnDFS.addActionListener(e -> new Thread(() -> graph.dfs(tfId1.getText().trim())).start());
        btnSearch.addActionListener(e -> new Thread(() -> graph.search(tfId1.getText().trim())).start());
        btnCheck.addActionListener(e -> new Thread(() -> graph.check(tfId1.getText().trim(), tfId2.getText().trim())).start());
        
        btnPaths.addActionListener(e -> {
            String s = tfId1.getText().trim();
            String d = tfId2.getText().trim();
            if(!s.isEmpty() && !d.isEmpty()) new Thread(() -> graph.findAllPaths(s, d)).start();
        });
    }
}