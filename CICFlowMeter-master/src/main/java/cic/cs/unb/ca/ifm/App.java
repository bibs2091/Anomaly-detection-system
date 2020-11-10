package cic.cs.unb.ca.ifm;

import cic.cs.unb.ca.Sys;
import cic.cs.unb.ca.flow.FlowMgr;
import cic.cs.unb.ca.flow.ui.FlowMonitorPane;
import cic.cs.unb.ca.guava.GuavaMgr;
import cic.cs.unb.ca.ifm.ui.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.io.IOException;
import java.net.NetworkInterface;
import py4j.GatewayServer;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.net.InetAddress;

public class App {
	public static final Logger logger = LoggerFactory.getLogger(App.class);
	private static FlowMonitorPane flowMonitorPane;

	public static void init() throws IOException {
		FlowMgr.getInstance().init();
		GuavaMgr.getInstance().init();
	}

	/**
	 * Launch the application.
	 */

	public static void main(String[] args) throws IOException {
		/*try {
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		*/
		FlowMonitorPane app = new FlowMonitorPane();
		// app is now the gateway.entry_point
		GatewayServer server = new GatewayServer.GatewayServerBuilder()
					.entryPoint(app)
					.javaPort(25333)
					.javaAddress(InetAddress.getByName("0.0.0.0"))
					.build();
		server.start();
		FlowMgr flowMgr=new FlowMgr();
		String rootPath = System.getProperty("user.dir");
		StringBuilder sb = new StringBuilder(rootPath);
		sb.append(Sys.FILE_SEP).append("data").append(Sys.FILE_SEP);

		String mDataPath = sb.toString();

		sb.append("daily").append(Sys.FILE_SEP);
		String mFlowSavePath = sb.toString();



		EventQueue.invokeLater(() -> {
            try {


                init();
                new MainFrame();

				flowMonitorPane=MainFrame.monitorPane;
				flowMonitorPane.timer.purge();

				System.out.println("im dead");


            } catch (Exception e) {
				logger.debug(e.getMessage());
            }
        });


	}
}
