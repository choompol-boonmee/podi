package popdig;

import java.io.*;
import java.util.Hashtable;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import javax.swing.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xssf.usermodel.*;
import org.apache.pdfbox.pdmodel.PDDocument;

public class PopiangDigital {

	public static File fNode, fWork;
	public static String workDir;
	public static File fPrv, fPub;
	public static String sPrv="id_rsa", sPub="id_rsa.pub";
	public static String sEmail="", sPrefix="", sName;
	public static boolean bGui = false;
	public static String sMainRepo="", sWorkRepo="", sBaseIRI="";
	public static String sRecvEmail="", sPassWord="", sImap="", sSmtp="";
	public static String sFontName="";
	public static int iFontSize=10;
	public static File[] aRdfFold;
	public static JFrame frame;
	public static Hashtable<String,RdfModelInfo> hRdfModelInfo;

	public static String sNodeType;
	public static final String mainNode = "mainNode";
	public static final String workNode = "workNode";
	public static String sOsName, sUserName, sUserHome, sTimeZone;
	public static boolean bAttend = false;
	public static String sAttendID = "";
	public static String sCom = "com";

	// os.name=Windows 10
	// os.name=Mac OS X
	// user.home=/Users/apple
	// user.timezone=Asia/Bangkok

	public static boolean bWindows = false, bMacos = false, bLinux = false;

	public void proc(String[] args) {
		PopiangUtil.nodeInit();
		PopiangUtil.walkRtfTtl();
		if(sNodeType==mainNode) {
			new EmailProc().startEmailThread();
		}
		if(PopiangDigital.bGui) {
			new PopiangWindow().proc(new File(workDir));
		}
		if(PopiangDigital.bAttend) {
			PopiangUtil.runAttend();
		}
	}
}

