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
    public JButton btnScanning;
    public JToggleButton btnStop;
    private ButtonGroup btnGroup;
    private JLabel explanatoryLabel;
    private String rootPath;
    private File lastSave;
    private JFileChooser fileChooser;

    private ExecutorService csvWriterThread;
    public Timer timer;
    private TimerTask task;


    public FlowMonitorPane() throws SocketException, UnknownHostException,MalformedURLException,IOException{
        init();
        setLayout(new BorderLayout());
        add(initCenterPane());
    }

    private void init() {
        csvWriterThread = Executors.newSingleThreadExecutor();
    }

    public void destory() {
        csvWriterThread.shutdown();
    }

    private JPanel initCenterPane() throws SocketException, UnknownHostException,IOException{
        initTablePane();
        initNWifsPane();
        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());
        pane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        pane.setBackground(new Color(85, 98, 132));
        rootPath = System.getProperty("user.dir");
        GridBagConstraints c = new GridBagConstraints();
       
        // logo
        c.gridx = c.gridy =0;
        c.weightx = c.weighty =  1.0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        pane.add(logo(),c);

        // click to scan network button (init button)
        c.anchor = GridBagConstraints.CENTER;
        pane.add(scanButton(),c);
        
        // Scanning button
        c.anchor = GridBagConstraints.CENTER;
        pane.add(scanningButton(),c);

        // text label
        c.anchor = GridBagConstraints.PAGE_END;
        c.insets = new Insets(0,0,33,0);
        pane.add(explanatoryLabel(),c);
        return pane;
    }

    private JLabel logo() throws IOException{
        JLabel label = new JLabel();
        label.setForeground(Color.white);
        BufferedImage labelIcon = ImageIO.read(new File(rootPath+"/src/main/resources/esi_ids.png"));
        label.setIcon(new ImageIcon(labelIcon));
        return label;
    }

    private JButton scanButton() throws IOException{
        BufferedImage buttonIcon = ImageIO.read(new File(rootPath+"/src/main/resources/scan_network.png"));
        btnScan = new JButton(new ImageIcon(buttonIcon));
        btnScan.setContentAreaFilled(false);
        btnScan.setBorder(BorderFactory.createEmptyBorder());
        btnScan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnScan.setHorizontalAlignment(JButton.CENTER);
        btnScan.setVerticalAlignment(JButton.CENTER);
        btnScan.setVisible(true);
        
        btnScan.addActionListener(actionEvent ->{
            btnScanning.setVisible(true);
            btnScan.setVisible(false);
            btnStart.doClick(); 
        } );
    
        return btnScan;
    }

    private JButton scanningButton() throws IOException{
        BufferedImage buttonIcon2 = ImageIO.read(new File(rootPath+"/src/main/resources/scanning.png"));
        btnScanning = new JButton(new ImageIcon(buttonIcon2));
        btnScanning.setContentAreaFilled(false);
        btnScanning.setBorder(BorderFactory.createEmptyBorder());
        btnScanning.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnScanning.setHorizontalAlignment(JButton.CENTER);
        btnScanning.setVerticalAlignment(JButton.CENTER);
        btnScanning.setVisible(false);
        btnScanning.addActionListener(actionEvent ->{
            btnScanning.setVisible(false);              
            btnScan.setVisible(true);
            btnStop.doClick(); 
         } );
        return btnScanning;
    }

    private JLabel explanatoryLabel() throws IOException{
        BufferedImage labelIcon2 = ImageIO.read(new File(rootPath+"/src/main/resources/scan_text1.png"));
        explanatoryLabel = new JLabel();
        explanatoryLabel.setIcon(new ImageIcon(labelIcon2));
        return explanatoryLabel;
    }

    private void initTablePane() throws SocketException, UnknownHostException{
        String[] arrayHeader = StringUtils.split(FlowFeature.getHeader(), ",");
        defaultTableModel = new DefaultTableModel(arrayHeader,0);
    }

    private void initTableBtnPane() throws SocketException, UnknownHostException{
        fileChooser = new JFileChooser(new File(FlowMgr.getInstance().getmDataPath()));
        TextFileFilter csvChooserFilter = new TextFileFilter("csv file (*.csv)", new String[]{"csv"});
        fileChooser.setFileFilter(csvChooserFilter);
    }


    private void initNWifsPane() throws SocketException, UnknownHostException {
        initNWifsButtonPane();
        initNWifsListPane();
    }

    private void initNWifsButtonPane()   {
        btnLoad = new JButton("Load");
        btnLoad.addActionListener(actionEvent -> loadPcapIfs());
        btnStart = new JToggleButton("Start");
        btnStart.setEnabled(false);
        btnStart.addActionListener(actionEvent -> {
            try {
                startTrafficFlow();
            }catch (SocketException e){
                e.printStackTrace();
            }catch (UnknownHostException e){
                e.printStackTrace();
            }
        });
            
        btnStop = new JToggleButton("Stop");
        btnStop.setEnabled(false);
        btnStop.addActionListener(actionEvent -> stopTrafficFlow());
    }

    private void initNWifsListPane() {
        listModel = new DefaultListModel<>();
        listModel.addElement(new PcapIfWrapper("Click Load button to load network interfaces"));
        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(1);
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
        	String os_name = System.getProperty("os.name");
        	if (os_name.toString().contains("Linux")){
           		p = Runtime.getRuntime().exec("python interface.py ");
        	}else{
            	p = Runtime.getRuntime().exec("py interface.py ");        		
        	}
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            interfaceToUse = stdInput.readLine();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        //System.out.println("os = : "+System.getProperty("os.name"));
        for(int i = 0; i< list.getModel().getSize();i++) {
            o=list.getModel().getElementAt(i);
            //System.out.println("o = : "+o.toString());
            //System.out.println("interfaceToUse = : "+interfaceToUse.toString());
           if(o.toString().contains("any") || o.toString().contains(interfaceToUse)) {
                list.setSelectedIndex(i);
                break;
            }
        }
        String ifName = list.getSelectedValue().name();
        if (mWorker != null && !mWorker.isCancelled()) {
            return;
        }

        mWorker = new TrafficFlowWorker(ifName);
        mWorker.addPropertyChangeListener(event -> {
            TrafficFlowWorker task = (TrafficFlowWorker) event.getSource();
            if (TrafficFlowWorker.PROPERTY_FLOW.equalsIgnoreCase(event.getPropertyName())) {
                insertFlow((BasicFlow) event.getNewValue());
            }
        });
        mWorker.execute();
        btnLoad.setEnabled(false);
        btnStop.setEnabled(true);
        String path = FlowMgr.getInstance().getAutoSaveFile();
        logger.info("path:{}", path);
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
        	String os_name = System.getProperty("os.name");
        	if (os_name.toString().contains("Linux")){
            	p = Runtime.getRuntime().exec("python ../model.py " + removeTimeStamp(flowDump));           	
        	}else{
           		p = Runtime.getRuntime().exec("py ../model.py " + removeTimeStamp(flowDump));	        		
        	}
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
        if(defaultTableModel.getRowCount()>=10 && new File(path).exists()) {
            btnStop.doClick();
            btnStart.doClick();
            for (int i = defaultTableModel.getRowCount() - 1; i > -1; i--) {
                defaultTableModel.removeRow(i);
            }
        }
    }
    
}