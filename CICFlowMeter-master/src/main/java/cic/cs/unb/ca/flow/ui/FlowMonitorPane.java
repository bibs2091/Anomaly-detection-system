package cic.cs.unb.ca.flow.ui;

import cic.cs.unb.ca.Sys;
import cic.cs.unb.ca.flow.FlowMgr;
import cic.cs.unb.ca.guava.Event.FlowVisualEvent;
import cic.cs.unb.ca.guava.GuavaMgr;
import cic.cs.unb.ca.jnetpcap.BasicFlow;
import cic.cs.unb.ca.jnetpcap.FlowFeature;
import cic.cs.unb.ca.jnetpcap.PcapIfWrapper;
import cic.cs.unb.ca.jnetpcap.worker.LoadPcapInterfaceWorker;
import cic.cs.unb.ca.jnetpcap.worker.TrafficFlowWorker;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jnetpcap.PcapIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cic.cs.unb.ca.jnetpcap.worker.InsertCsvRow;
import swing.common.InsertTableRow;
import swing.common.JTable2CSVWorker;
import swing.common.TextFileFilter;
import java.net.*;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
public  class FlowMonitorPane extends JPanel {
    protected static final Logger logger = LoggerFactory.getLogger(FlowMonitorPane.class);


    private JTable flowTable;
    private DefaultTableModel defaultTableModel;
    public JList<PcapIfWrapper> list;
    private DefaultListModel<PcapIfWrapper> listModel;
    private JLabel lblStatus;
    private JLabel lblFlowCnt;

    private TrafficFlowWorker mWorker;

    public JButton btnLoad;
    public JToggleButton btnStart;
    public JButton btnScan;
    public JToggleButton btnStop;
    private ButtonGroup btnGroup;

    private JButton btnSave = new JButton();
    private File lastSave;
    private JButton btnGraph = new JButton();
    private JFileChooser fileChooser;

    private ExecutorService csvWriterThread;
    public Timer timer;
    private TimerTask task;


    public FlowMonitorPane() throws SocketException, UnknownHostException,MalformedURLException,IOException{
        init();

        setLayout(new BorderLayout());
        // setBorder(new EmptyBorder(10, 10, 10, 10));

        add(initCenterPane2());

    }

    private void init() {
        csvWriterThread = Executors.newSingleThreadExecutor();
    }

    public void destory() {
        csvWriterThread.shutdown();
    }
    private JPanel initCenterPane2() throws SocketException, UnknownHostException,IOException{

        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());
        pane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        pane.setBackground(new Color(85, 98, 132));

        String rootPath = System.getProperty("user.dir");
        GridBagConstraints c = new GridBagConstraints();
        // logo icon
        
        JLabel label = new JLabel();
        // label.setText("ESI IDS");
        label.setForeground(Color.white);
        BufferedImage labelIcon = ImageIO.read(new File(rootPath+"/src/main/resources/esi_ids.png"));
        label.setIcon(new ImageIcon(labelIcon));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        
        pane.add(label,c);

        // Scan network button
        BufferedImage buttonIcon = ImageIO.read(new File(rootPath+"/src/main/resources/scan_network.png"));
        btnScan = new JButton(new ImageIcon(buttonIcon));
        btnScan.setContentAreaFilled(false);
        btnScan.setBorder(BorderFactory.createEmptyBorder());
        btnScan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnScan.setHorizontalAlignment(JButton.CENTER);
        btnScan.setVerticalAlignment(JButton.CENTER);
        c.anchor = GridBagConstraints.CENTER;
        // c.gridx = 1;
        // c.gridy = 1;
        pane.add(btnScan,c);
        
        JLabel label2 = new JLabel();
        // label.setText("ESI IDS");
        label2.setForeground(Color.white);
        BufferedImage labelIcon2 = ImageIO.read(new File(rootPath+"/src/main/resources/scan_text1.png"));
        label2.setIcon(new ImageIcon(labelIcon2));
        label2.setFont(label.getFont().deriveFont(20.0f));
        c.anchor = GridBagConstraints.PAGE_END;
        c.insets = new Insets(0,0,33,0);
        pane.add(label2,c);
        c.gridy = 2;
        return pane;
    }


    private JPanel initCenterPane() throws SocketException, UnknownHostException{
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(0, 0));
        pane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,initFlowPane(), initNWifsPane());
        splitPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(1.0);

        pane.add(splitPane,BorderLayout.CENTER);
        return pane;
    }

    private JPanel initFlowPane() throws SocketException, UnknownHostException{
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(0, 5));
        pane.setBorder(BorderFactory.createLineBorder(new Color(0x555555)));

        //pane.add(initTableBtnPane(), BorderLayout.NORTH);
        pane.add(initTablePane(), BorderLayout.CENTER);
        pane.add(initStatusPane(), BorderLayout.SOUTH);

        return pane;
    }

    private JPanel initTablePane() throws SocketException, UnknownHostException{
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(0, 0));
        pane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        JButton b9 = new JButton("9"); 
        String[] arrayHeader = StringUtils.split(FlowFeature.getHeader(), ",");
        defaultTableModel = new DefaultTableModel(arrayHeader,0);
        flowTable = new JTable(defaultTableModel);
        flowTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(b9);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10,50,10,50));


        pane.add(scrollPane,BorderLayout.CENTER);

        return pane;
    }

    private JPanel initTableBtnPane() throws SocketException, UnknownHostException{
        JPanel btnPane = new JPanel();
        btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.X_AXIS));
        btnSave = new JButton("Save as");
        btnGraph = new JButton("Graphs");
        btnSave.setFocusable(false);
        btnSave.setEnabled(false);
        btnGraph.setFocusable(false);
        btnGraph.setEnabled(false);

        fileChooser = new JFileChooser(new File(FlowMgr.getInstance().getmDataPath()));
        TextFileFilter csvChooserFilter = new TextFileFilter("csv file (*.csv)", new String[]{"csv"});
        fileChooser.setFileFilter(csvChooserFilter);

        btnSave.addActionListener(actionEvent -> {
            int action = fileChooser.showSaveDialog(FlowMonitorPane.this);
            if (action == JFileChooser.APPROVE_OPTION) {

                File selectedFile = fileChooser.getSelectedFile();
                String filename = selectedFile.getName();
                if (FilenameUtils.getExtension(filename).equalsIgnoreCase("csv")) {
                    //save name ok
                } else {
                    selectedFile = new File(selectedFile.getParentFile(), FilenameUtils.getBaseName(filename) + ".csv");
                }
                String title = "file conflict";
                String message = "Another file with the same name already exists,do you want to overwrite?";

                if (selectedFile.exists()) {

                    int reply = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);

                    if (reply == JOptionPane.YES_OPTION) {
                        JTable2CSVWorker worker = new JTable2CSVWorker(flowTable, selectedFile);
                        worker.execute();
                    } else {
                        btnSave.doClick();
                    }
                } else {
                    JTable2CSVWorker worker = new JTable2CSVWorker(flowTable, selectedFile);
                    worker.execute();
                }
                lastSave = selectedFile;
                btnGraph.setEnabled(true);
            }

        });

        btnGraph.addActionListener(actionEvent -> GuavaMgr.getInstance().getEventBus().post(new FlowVisualEvent(lastSave)));

        btnPane.add(Box.createHorizontalGlue());
        btnPane.add(btnSave);
        btnPane.add(Box.createHorizontalGlue());
        btnPane.add(btnGraph);
        btnPane.add(Box.createHorizontalGlue());

        btnPane.setBorder(BorderFactory.createRaisedSoftBevelBorder());

        return btnPane;
    }

    private JPanel initStatusPane() throws SocketException, UnknownHostException{
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        lblStatus = new JLabel("Get ready");
        lblStatus.setForeground(SystemColor.desktop);
        lblFlowCnt = new JLabel("0");

        pane.add(Box.createHorizontalStrut(5));
        pane.add(lblStatus);
        pane.add(Box.createHorizontalGlue());
        pane.add(lblFlowCnt);
        pane.add(Box.createHorizontalStrut(5));

        return pane;
    }

    private JPanel initNWifsPane() throws SocketException, UnknownHostException {
        JPanel pane = new JPanel(new BorderLayout(0, 0));
        pane.setBorder(BorderFactory.createLineBorder(new Color(0x555555)));
        pane.add(initNWifsButtonPane(), BorderLayout.WEST);
        pane.add(initNWifsListPane(), BorderLayout.CENTER);

        return pane;
    }

    private JPanel initNWifsButtonPane()   {
        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        Dimension d = new Dimension(80,48);

        btnLoad = new JButton("Load");
        btnLoad.setMinimumSize(d);
        btnLoad.setMaximumSize(d);
        btnLoad.setOpaque(false);
        btnLoad.setFocusPainted(false);
        btnLoad.setBorderPainted(false);
        btnLoad.setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder(0,0,0,0)); // Especially important
        btnLoad.addActionListener(actionEvent -> loadPcapIfs());
        btnStart = new JToggleButton("Start");
        btnStart.setMinimumSize(d);
        btnStart.setMaximumSize(d);
        btnStart.setEnabled(false);
        btnStart.addActionListener(actionEvent -> {
            // try {
            //     startTrafficFlow();
            // }catch (SocketException e){
            //     e.printStackTrace();
            // }catch (UnknownHostException e){
            //     e.printStackTrace();
            // }
            logger.info("Pcap stop listening");
        });
            
        
        btnStop = new JToggleButton("Stop");
        btnStop.setMinimumSize(d);
        btnStop.setMaximumSize(d);
        btnStop.setEnabled(false);
        btnStop.addActionListener(actionEvent -> stopTrafficFlow());

        btnGroup = new ButtonGroup();
        btnGroup.add(btnStart);
        btnGroup.add(btnStop);

        pane.add(Box.createVerticalGlue());
        pane.add(btnLoad);
        pane.add(Box.createVerticalGlue());
        pane.add(btnStart);
        pane.add(Box.createVerticalGlue());
        pane.add(btnStop);
        pane.add(Box.createVerticalGlue());
        return pane;
    }

    private JPanel initNWifsListPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(0, 0));
        pane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        listModel = new DefaultListModel<>();
        listModel.addElement(new PcapIfWrapper("Click Load button to load network interfaces"));
        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(1);
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        pane.add(scrollPane,BorderLayout.CENTER);
        return pane;
    }

    private void loadPcapIfs() {
        LoadPcapInterfaceWorker task = new LoadPcapInterfaceWorker();
        task.addPropertyChangeListener(event -> {
            if ("state".equals(event.getPropertyName())) {
                LoadPcapInterfaceWorker task1 = (LoadPcapInterfaceWorker) event.getSource();
                switch (task1.getState()) {
                    case STARTED:
                        break;
                    case DONE:
                        try {
                            java.util.List<PcapIf> ifs = task1.get();
                            List<PcapIfWrapper> pcapiflist = PcapIfWrapper.fromPcapIf(ifs);

                            listModel.removeAllElements();
                            for(PcapIfWrapper pcapif :pcapiflist) {
                                listModel.addElement(pcapif);
                            }
                            btnStart.setEnabled(true);
                            btnStart.doClick();
                            btnGroup.clearSelection();

                            lblStatus.setText("pick one network interface to listening");
                            lblStatus.validate();

                        } catch (InterruptedException | ExecutionException e) {
                            logger.debug(e.getMessage());
                        }
                        break;
                }
            }
        });

        task.execute();

    }

    private void startTrafficFlow() throws SocketException,UnknownHostException {
        list = new JList<>(listModel);
        Object o=null;
        Process p;
        String interfaceToUse = null;
        try
        {
            p = Runtime.getRuntime().exec("py interface.py ");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            interfaceToUse = stdInput.readLine();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        for(int i = 0; i< list.getModel().getSize();i++) {
            System.out.println(list.getModel().getElementAt(i));
            o=list.getModel().getElementAt(i);
            System.out.println(o.toString());
           if(o.toString().contains("any") || o.toString().contains(interfaceToUse)) {
                list.setSelectedIndex(i);
                break;
            }
        }
        String ifName = list.getSelectedValue().name();
        System.out.println(ifName);

        if (mWorker != null && !mWorker.isCancelled()) {
            return;
        }

        mWorker = new TrafficFlowWorker(ifName);
        mWorker.addPropertyChangeListener(event -> {
            TrafficFlowWorker task = (TrafficFlowWorker) event.getSource();
            if("progress".equals(event.getPropertyName())){
                lblStatus.setText((String) event.getNewValue());
                lblStatus.validate();
            }else if (TrafficFlowWorker.PROPERTY_FLOW.equalsIgnoreCase(event.getPropertyName())) {
                insertFlow((BasicFlow) event.getNewValue());
            }else if ("state".equals(event.getPropertyName())) {
                switch (task.getState()) {
                    case STARTED:
                        break;
                    case DONE:
                        try {
                            lblStatus.setText(task.get());
                            lblStatus.validate();
                        } catch(CancellationException e){

                            lblStatus.setText("stop listening");
                            lblStatus.setForeground(SystemColor.GRAY);
                            lblStatus.validate();
                            logger.info("Pcap stop listening");

                        }catch (InterruptedException | ExecutionException e) {
                            logger.debug(e.getMessage());
                        }
                        break;
                }
            }
        });
        mWorker.execute();
        lblStatus.setForeground(SystemColor.desktop);
        btnLoad.setEnabled(false);
        btnStop.setEnabled(true);
        String path = FlowMgr.getInstance().getAutoSaveFile();
        logger.info("path:{}", path);

       // this.timer();




    }

    public void timer()
    {


         timer=new Timer();
         task = new TimerTask() {
            @Override
            public void run() {
                btnStop.doClick();
                System.out.println("timer working");
                timer.cancel();
            }
        };
        timer.schedule(task, 15000,15000);

    }

    private void stopTrafficFlow() {
        if (mWorker != null) {
            mWorker.cancel(true);
        }

        //FlowMgr.getInstance().stopFetchFlow();

        btnLoad.setEnabled(true);


        String path = FlowMgr.getInstance().getAutoSaveFile();
        logger.info("path:{}", path);
        FlowMgr.i++;

       // System.exit(0);
    }
    private String removeTimeStamp(String flowDump){
        int i = 0;
        int comma = 0;
        while ( (i <= flowDump.length()) && (comma < 6) ){
            if (flowDump.charAt(i) == ',' ){
                comma++;
            }
            i++;
        }
        return flowDump.replace(flowDump.substring(i,i+23),"");
    }
    private void insertFlow(BasicFlow flow) {
        List<String> flowStringList = new ArrayList<>();
        List<String[]> flowDataList = new ArrayList<>();
        String flowDump = flow.dumpFlowBasedFeaturesEx();
        flowStringList.add(flowDump);
        flowDataList.add(StringUtils.split(flowDump, ","));
        Process p;
        try
        {
//            String cmd = "py script.py " + removeTimeStamp(flowDump);
//            logger.info(cmd,"hhh");

            p = Runtime.getRuntime().exec("py ../model.py " + removeTimeStamp(flowDump));
            String s = null;
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((s = stdInput.readLine()) != null) {
                logger.info(s,"hhh");
            }
            BufferedReader errinput = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((s = errinput.readLine()) != null) {
                logger.info(s,"hhh");
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        //write flows to csv file
        String header  = FlowFeature.getHeader();
        String path = FlowMgr.getInstance().getSavePath();
        String filename = FlowMgr.i+ FlowMgr.FLOW_SUFFIX;
        csvWriterThread.execute(new InsertCsvRow(header, flowStringList, path, filename));

        //insert flows to JTable
        SwingUtilities.invokeLater(new InsertTableRow(defaultTableModel,flowDataList,lblFlowCnt));
        btnSave.setEnabled(true);
        if(defaultTableModel.getRowCount()>=10 && new File(path).exists()) {
            btnStop.doClick();
            btnStart.doClick();
            for (int i = defaultTableModel.getRowCount() - 1; i > -1; i--) {
                defaultTableModel.removeRow(i);
            }
        }
    }
    
}
