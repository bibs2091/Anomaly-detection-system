package cic.cs.unb.ca.flow;

import cic.cs.unb.ca.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class FlowMgr {

    protected static final Logger logger = LoggerFactory.getLogger(FlowMgr.class);

    public static final String FLOW_SUFFIX = "_Flow.csv";

    private static FlowMgr Instance = new FlowMgr();

    public static String mFlowSavePath;
    private String mDataPath;
    public static int i=0;

    public FlowMgr() {
        super();
    }



    public static File getLatestFilefromDir(String dirPath){
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }


    public void getCount() throws IOException {


    }
    public static void  main (String args[]) throws IOException {
       FlowMgr flowMgr=new FlowMgr();
       //flowMgr.getCount();
    }
    
    public static FlowMgr getInstance() {
        return Instance;
    }

    public FlowMgr init() throws IOException {


        String rootPath = System.getProperty("user.dir");
		StringBuilder sb = new StringBuilder(rootPath);
		sb.append(Sys.FILE_SEP).append("data").append(Sys.FILE_SEP);

		mDataPath = sb.toString();

        sb.append("daily").append(Sys.FILE_SEP);
        mFlowSavePath = sb.toString();
        String s;
        if(getLatestFilefromDir(mFlowSavePath)==null)
            s = "";
        else {
            s = getLatestFilefromDir(mFlowSavePath).getName();
            s=s.replace("_Flow.csv", "");
        }
        System.out.println(s);
        System.out.println(s);
        if(s != "") {
            i = Integer.parseInt(s);
            i++;
        }
        System.out.println(i);
//this.getCount();
        return Instance;
    }

    public void destroy() {
    }

	public String getSavePath() {
		return mFlowSavePath;
	}

    public String getmDataPath() {
        return mDataPath;
    }

    public String getAutoSaveFile() {
		String filename = i+FLOW_SUFFIX;
		return mFlowSavePath+filename;
	}
}
