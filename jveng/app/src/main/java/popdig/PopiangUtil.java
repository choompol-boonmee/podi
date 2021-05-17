package popdig;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import com.sun.mail.util.BASE64DecoderStream;
import java.net.InetAddress;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
//import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.Key;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.Objects;

import com.google.common.base.Splitter;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import org.apache.jena.query.*;

import java.util.*;
import javax.mail.*;
import javax.mail.event.*;
import javax.mail.internet.*;
import java.io.*;
import com.sun.mail.imap.*;
import org.jsoup.Jsoup;

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
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.log4j.Logger;
import org.dom4j.Document;  
import org.dom4j.DocumentHelper;  
import org.dom4j.io.OutputFormat;  
import org.dom4j.io.XMLWriter;
import java.util.Collections;
import java.util.regex.*;
import java.util.stream.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.util.Units;
//import org.apache.poi.xwpf.usermodel.Document;

public class PopiangUtil {

	static Logger log;

	public static void nodeInit() {
		setWorkDir();
		setLogging();

		PopiangDigital.sOsName = System.getProperty("os.name");
		PopiangDigital.sUserName = System.getProperty("user.name");
		PopiangDigital.sUserHome = System.getProperty("user.home");
		PopiangDigital.sTimeZone = System.getProperty("user.timezone");

		log.info("os.name: "+ PopiangDigital.sOsName);
		log.info("user.name: "+ PopiangDigital.sUserName);
		log.info("user.home: "+ PopiangDigital.sUserHome);
		log.info("user.timezone: "+ PopiangDigital.sTimeZone);
		if(PopiangDigital.sOsName.startsWith("Windows")) PopiangDigital.bWindows = true;
		if(PopiangDigital.sOsName.startsWith("Mac")) PopiangDigital.bMacos = true;
		if(PopiangDigital.sOsName.startsWith("Linux")) PopiangDigital.bLinux = true;

		if(!PopiangDigital.fNode.exists()) {
			log.info("CONFIG DIALOG");
			PopiangConfig conf = new PopiangConfig();
			conf.config();
			log.info("============== CONFIG ==============");
			readNodeTtl();
			return;
		} else {
			readNodeTtl();
		}
	}

	public static void setWorkDir() {
		try {
			File fd = new File(PopiangDigital.class.getProtectionDomain()
				.getCodeSource().getLocation().toURI().getPath());
			fd = fd.getParentFile();
			String jpath = fd.getAbsolutePath();
			String cond1 = "app/build/libs";
			String cond2 = "bin";
			String cond3 = "app/build/classes/java";
			System.out.println(fd);
			if(jpath.endsWith(cond1)) { // developing
				PopiangDigital.workDir = jpath.substring(0, jpath.length()-cond1.length())+"work";
			} else if(jpath.endsWith(cond2)) { // runtime
				String wd = jpath.substring(0, jpath.length()-cond2.length());
				if(wd.endsWith("/")) wd = wd.substring(0,wd.length()-1);
				PopiangDigital.workDir = wd;
			} else if(jpath.endsWith(cond3)) { // build
				PopiangDigital.workDir = jpath.substring(0, jpath.length()-cond3.length())+"work";
			} else { // unknown
				PopiangDigital.workDir = new File(".").getAbsolutePath();
			}
			PopiangDigital.fWork = new File(PopiangDigital.workDir);
			File cfg = new File(PopiangDigital.workDir+"/.cfg/");
			if(!cfg.exists()) cfg.mkdirs();
			PopiangDigital.fPrv = new File(PopiangDigital.workDir+"/.cfg/"+PopiangDigital.sPrv);
			PopiangDigital.fPub = new File(PopiangDigital.workDir+"/.cfg/"+PopiangDigital.sPub);
			PopiangDigital.fNode = new File(PopiangDigital.workDir+"/.cfg/node.ttl");

			File fGit = new File(PopiangDigital.workDir+"/.git");
			if(!fGit.exists()) {
				System.out.println("GIT INIT at "+fGit.getAbsolutePath());
				runGit("git", "init");
			}
			
		} catch(Exception z) {
			z.printStackTrace();
		}
	}

	public static void setLogging() {
		try {

//System.out.println("log ..1");
			ConfigurationBuilder<BuiltConfiguration> builder
				 = ConfigurationBuilderFactory.newConfigurationBuilder();
//			builder.setStatusLevel(Level.INFO);
			builder.setStatusLevel(Level.DEBUG);
			builder.addProperty("basePath", PopiangDigital.workDir+"/.log");
			AppenderComponentBuilder console = builder.newAppender("console", "Console"); 
			console.addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
			LayoutComponentBuilder lay = builder.newLayout("PatternLayout");
			lay.addAttribute("pattern", "[%-5level] %d{yyyyMMdd-HHmmss}-%c{1}: %msg%n");
			console.add(lay);
			builder.add(console);

//System.out.println("log ..2");
			LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout")
			    .addAttribute("pattern", "%d [%t] %-5level: %msg%n");
			ComponentBuilder policy = builder.newComponent("Policies")
				.addComponent(builder.newComponent("TimeBasedTriggeringPolicy")
					.addAttribute("interval", "1")
					.addAttribute("modulate", "true"));

//System.out.println("log ..3");
			AppenderComponentBuilder appenderBuilder = builder.newAppender("rolling", "RollingFile")
			    .addAttribute("fileName", "${basePath}/rolling.log")
			    .addAttribute("filePattern", "${basePath}/rolling-%d{MM-dd-yy}.log.gz")
			    .add(layoutBuilder)
			    .addComponent(policy);
			builder.add(appenderBuilder);

//System.out.println("log ..4");
			RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.INFO);
			rootLogger.add(builder.newAppenderRef("console"));
			rootLogger.add(builder.newAppenderRef("rolling"));
			rootLogger.addAttribute("additivity", false);
			builder.add(rootLogger);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			builder.writeXmlConfiguration(baos);
			String xml = new String(baos.toByteArray());
			//builder.writeXmlConfiguration(System.out);
			Document doc = DocumentHelper.parseText(xml);  
			StringWriter sw = new StringWriter();  
			OutputFormat format = OutputFormat.createPrettyPrint();  
			XMLWriter xw = new XMLWriter(sw, format);  
			xw.write(doc);  
			System.out.println(sw.toString());

			LoggerContext ctx = Configurator.initialize(builder.build());

//			LoggerContext context = (LoggerContext) LogManager.getContext(false);
//			context.setConfigLocation(new File(workDir+"/.cfg/log4j2.xml").toURI());

			ctx.updateLoggers();

			log = Logger.getLogger(PopiangUtil.class);

		} catch(Exception z) {
			z.printStackTrace();
		}
	}

	public static void readNodeTtl() {
		try {

log.info("NODE.TTL"+PopiangDigital.fNode.getAbsolutePath());
			String ct = new String(Files.readAllBytes(
				Paths.get(PopiangDigital.fNode.getAbsolutePath())),"UTF-8");
log.info(ct);

			org.apache.jena.query.ARQ.init();
			Model mo = ModelFactory.createDefaultModel();
			mo.read(new FileInputStream(PopiangDigital.fNode), null, "TTL");

			String sqry = ""
				+ "PREFIX vp: 	<http://ipthailand.go.th/rdf/voc-pred#>"
				+ "PREFIX vo: 	<http://ipthailand.go.th/rdf/voc-obj#>"
				+ "PREFIX nd:	<https://ipthailand.go.th/rdf/nd#>"
				+ " SELECT ?c ?d ?e ?g ?h ?i ?k ?l ?m ?n ?o ?p ?j ?q ?r WHERE { "
				+ " ?a a vo:PopiNode . "
				+ " ?a vp:type vo:MainNode . "
				+ " ?a vp:channel ?b . "
				+ " ?a vp:owner ?f . "
				+ " ?a vp:prefix ?h . "
				+ " ?a vp:mainrep ?n . "
				+ " ?a vp:workrep ?o . "
				+ " ?a vp:baseiri ?p . "
				+ " ?b vp:email ?c . "
				+ " ?b vp:passwd ?d . "
				+ " ?b vp:imap ?e . "
				+ " ?b vp:smtp ?m . "
				+ " ?f vp:email ?g . "
				+ " ?f vp:name ?i . "

				+ " OPTIONAL { ?a vp:gui ?j . "
				+ " ?j vp:fontName ?k . "
				+ " ?j vp:fontSize ?l . "
  				+ "	} "


				+ " OPTIONAL { ?a vp:attend ?q . "
				+ " ?q vp:id ?r . "
  				+ "	} "

  				+ "	} "
				;
			List<String[]> aRet = sparql(mo, sqry);
			if(aRet.size()==1) {
				PopiangDigital.sNodeType = PopiangDigital.mainNode;
				String[] reg = aRet.get(0);
				System.out.println(">>>> MAIN NODE: "+reg[0]+" b:"+reg[1] +" c:"+reg[2]);
				PopiangDigital.sRecvEmail = reg[0];
				PopiangDigital.sPassWord = reg[1];
				PopiangDigital.sImap = reg[2];
				PopiangDigital.sEmail = reg[3];
				PopiangDigital.sPrefix = reg[4];
				PopiangDigital.sName = reg[5];
				PopiangDigital.sSmtp = reg[8];
				PopiangDigital.sMainRepo = reg[9];
				PopiangDigital.sWorkRepo = reg[10];
				PopiangDigital.sBaseIRI = reg[11];
				System.out.println("recv: "+ PopiangDigital.sRecvEmail);
				System.out.println("pass: "+ PopiangDigital.sPassWord);
				System.out.println("imap: "+ PopiangDigital.sImap);
				System.out.println("smtp: "+ PopiangDigital.sSmtp);
				System.out.println("mail: "+ PopiangDigital.sEmail);
				System.out.println("pref: "+ PopiangDigital.sPrefix);
				System.out.println("name: "+ PopiangDigital.sName);
				System.out.println("font name: "+ PopiangDigital.sFontName);
				System.out.println("font size: "+ PopiangDigital.iFontSize);
				System.out.println("mainrep: "+ PopiangDigital.sMainRepo);
				System.out.println("workrep: "+ PopiangDigital.sWorkRepo);
				System.out.println("baseiri: "+ PopiangDigital.sBaseIRI);
				if(reg[12]==null) {
					PopiangDigital.bGui = false;
					log.info("NO FONT SPECIFIED !!!!!!");
				} else {
					PopiangDigital.bGui = true;
					log.info("FONT SPECIFIED 12 : "+ reg[12]+" = "+reg[6]);
				}
				if(reg[13]!=null) {
					PopiangDigital.bAttend = true;
					PopiangDigital.sAttendID = reg[14];
				}
log.info("ATTEND: "+ reg[13]);
log.info("ATTID: "+ reg[14]);
				PopiangDigital.sFontName = reg[6];
				try { PopiangDigital.iFontSize = Integer.parseInt(reg[7]); } catch(Exception x) {}
			} else {
				String qrSub = ""
					+ "PREFIX vp: 	<http://ipthailand.go.th/rdf/voc-pred#>"
					+ "PREFIX vo: 	<http://ipthailand.go.th/rdf/voc-obj#>"
					+ "PREFIX nd:	<https://ipthailand.go.th/rdf/nd#>"
					+ " SELECT ?c ?d ?f ?g ?h ?n ?o ?p ?e ?q ?r WHERE { "
					+ " ?a a vo:PopiNode . "
					+ " ?a vp:type vo:WorkNode . "
					+ " ?a vp:owner ?b . "
					+ " ?a vp:prefix ?d . "
					+ " ?a vp:mainrep ?n . "
					+ " ?a vp:workrep ?o . "
					+ " ?a vp:baseiri ?p . "
					+ " ?b vp:email ?c . "
					+ " ?b vp:name ?h . "

					+ " OPTIONAL { ?a vp:gui ?e . "
					+ " ?j vp:fontName ?f . "
					+ " ?j vp:fontSize ?g . "
  					+ "	} "

					+ " OPTIONAL { ?a vp:attend ?q . "
					+ " ?q vp:id ?r . "
  					+ "	} "

					+ " } ";
				aRet = sparql(mo, qrSub);
				if(aRet.size()==1) {
					PopiangDigital.sNodeType = PopiangDigital.workNode;
					String[] reg = aRet.get(0);
					PopiangDigital.sEmail = reg[0];
					PopiangDigital.sPrefix = reg[1];
					PopiangDigital.sFontName = reg[2];
					try { PopiangDigital.iFontSize = Integer.parseInt(reg[3]); } catch(Exception x) {}
					if(reg[8]==null) {
						PopiangDigital.bGui = false;
						log.info("NO FONT SPECIFIED !!!!!!");
					} else {
						PopiangDigital.bGui = true;
						log.info("FONT SPECIFIED 6 : "+ reg[6]+" = "+reg[2]);
					}
					if(reg[9]!=null) {
						PopiangDigital.bAttend = true;
						PopiangDigital.sAttendID = reg[14];
					}
					PopiangDigital.sName = reg[4];
					PopiangDigital.sMainRepo = reg[5];
					PopiangDigital.sWorkRepo = reg[6];
					PopiangDigital.sBaseIRI = reg[6];
					System.out.println(">>>> WORK NODE ==== email: "+reg[0]);
					System.out.println("mail: "+ PopiangDigital.sEmail);
					System.out.println("pref: "+ PopiangDigital.sPrefix);
					System.out.println("name: "+ PopiangDigital.sName);
					System.out.println("font name: "+ PopiangDigital.sFontName);
					System.out.println("font size: "+ PopiangDigital.iFontSize);
					System.out.println("mainrep: "+ PopiangDigital.sMainRepo);
					System.out.println("workrep: "+ PopiangDigital.sWorkRepo);
					System.out.println("baseiri: "+ PopiangDigital.sBaseIRI);
				} else {
					System.out.println("==== NOT DEFINED NODE ====");
				}
			}
			log.info("prefix: "+ PopiangDigital.sPrefix);
			File own = new File(PopiangDigital.workDir+"/rdf/"+PopiangDigital.sPrefix);
			log.info("own dir: "+ own.getAbsolutePath()+" : "+ own.exists());
			if(!own.exists()) own.mkdirs();

		} catch(Exception z) {
			z.printStackTrace();
		}
	}

	public static void gitInit(Path directory) throws IOException, InterruptedException {
		runCommand(directory, "git", "init");
	}

	public static void gitFetch(Path directory) throws IOException, InterruptedException {
		runCommand(directory, "git", "fetch");
	}

	public static void gitPull(Path directory) throws IOException, InterruptedException {
		runCommand(directory, "git", "pull");
	}

	public static void gitStage(Path directory) throws IOException, InterruptedException {
		runCommand(directory, "git", "add", "-A");
	}

	public static void gitCommit(Path directory, String message) throws IOException, InterruptedException {
		runCommand(directory, "git", "commit", "-m", message);
	}

	public static void gitClone(Path directory, String originUrl) throws IOException, InterruptedException {
		runCommand(directory.getParent(), "git", "clone", originUrl, directory.getFileName().toString());
	}


	public static void gitPush(Path directory) throws IOException, InterruptedException {
		runCommand(directory, "git", "push", "origin", "main");
	}

	public static void gitPushMain0(String url) throws IOException, InterruptedException {
		runGit("git", "config", "pull.rebase", "true");
		runGit("git", "config", "rebase.autoStash", "true");

		runGit("git", "add", ".");
		runGit("git", "commit", "-m", "\"verx\"");

		runGit("git", "branch", "main");
		runGit("git", "checkout", "main");
		runGit("git", "remote", "remove", "upstream");
		runGit("git", "remote", "add", "upstream", url);
		runGit("git", "pull", "upstream", "main");

		runGit("git", "remote", "remove", "origin");
		runGit("git", "remote", "add", "origin", url);
		runGit("git", "push", "origin", "main");
	}

	public static void gitPushMain(String url) throws IOException, InterruptedException {
		runGit("git", "config", "pull.rebase", "true");
		runGit("git", "config", "rebase.autoStash", "true");

		runGit("git", "add", ".");
		runGit("git", "commit", "-m", "\"verx\"");

		runGit("git", "checkout", "main");
		runGit("git", "remote", "remove", "upstream");
		runGit("git", "remote", "add", "upstream", url);
		runGit("git", "pull", "upstream", "main");

		runGit("git", "remote", "remove", "origin");
		runGit("git", "remote", "add", "origin", url);
		runGit("git", "push", "origin", "main");
	}

	public static void gitJoinWork2Main(String mnrepo, String wkrepo, String branch) 
		 throws IOException, InterruptedException {

		runGit("git", "config", "pull.rebase", "true");
		runGit("git", "config", "rebase.autoStash", "true");

log.info("JOIN: pull from "+ branch);
		runGit("git", "branch", branch);
		runGit("git", "checkout", branch);
		runGit("git", "remote", "remove", "upstream");
		runGit("git", "remote", "add", "upstream", wkrepo);
//		runGit("git", "fetch", "upstream", branch);
//		runGit("git", "branch", "-f", branch, "upstream", branch);
		runGit("git", "pull", "upstream", branch);

log.info("JOIN: remove upstream "+ branch);
		runGit("git", "remote", "remove", "upstream");

log.info("JOIN: pull from main : "+mnrepo);
//		runGit("git", "branch", "main");
		runGit("git", "checkout", "main");
		runGit("git", "remote", "remove", "origin");
		runGit("git", "remote", "add", "origin", mnrepo);
		runGit("git", "pull", "origin", "main");

		runGit("git", "rebase", branch);

	}

	public static void gitPushWork0(String url, String own, String prf)
		 throws IOException, InterruptedException {

log.info("PUSH-UPSTREAM: "+ url);
		runGit("git", "config", "pull.rebase", "true");
		runGit("git", "config", "rebase.autoStash", "true");
		runGit("git", "branch", "main");
		runGit("git", "checkout", "main");
		runGit("git", "remote", "remove", "upstream");
		runGit("git", "remote", "add", "upstream", url);
		runGit("git", "pull", "upstream", "main");

log.info("OWN-ORIGIN: "+ own);
		runGit("git", "remote", "remove", "origin");
		runGit("git", "remote", "add", "origin", own);
		runGit("git", "push", "origin", "main");

		runGit("git", "branch", prf);
		runGit("git", "checkout", prf);
		runGit("git", "pull", "origin", prf);
		runGit("git", "push", "origin", prf);
	}

	public static void gitPushWork(String mn, String own, String prf)
		 throws IOException, InterruptedException {

log.info("PUSH-PREFIX: "+ prf);
		runGit("git", "checkout", prf);
		runGit("git", "config", "pull.rebase", "true");
		runGit("git", "config", "rebase.autoStash", "true");
		runGit("git", "add", ".");
		runGit("git", "commit", "-m", "\"verx\"");

log.info("PUSH-UPSTREAM: "+ mn);
		runGit("git", "checkout", "main");
		runGit("git", "remote", "remove", "upstream");
		runGit("git", "remote", "add", "upstream", mn);
		runGit("git", "pull", "upstream", "main");

log.info("OWN-ORIGIN: "+ own);
		runGit("git", "remote", "remove", "origin");
		runGit("git", "remote", "add", "origin", own);
		runGit("git", "push", "origin", "main");

		runGit("git", "checkout", prf);
		runGit("git", "pull", "origin", prf);
		runGit("git", "rebase", "origin/main");
		runGit("git", "push", "origin", prf);
	}

	public static void runGit(String... cmd) {
		try {
			ProcessBuilder pb = new ProcessBuilder().
				command(cmd).directory(PopiangDigital.fWork);
			Map<String,String> env = pb.environment();
			env.put("GIT_SSH_COMMAND", "ssh -o IdentitiesOnly=yes -i "
				+ PopiangDigital.workDir+"/.cfg/id_rsa");
			Process p = pb.start();
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
			outputGobbler.start();
			errorGobbler.start();
			int exit = p.waitFor();
			errorGobbler.join();
			outputGobbler.join();
		} catch(Exception a) {
			log.error("007", a);
		}
	}

	public static void runCommand(Path directory, String... command) throws IOException, InterruptedException {
		Objects.requireNonNull(directory, "directory");
		if (!Files.exists(directory)) {
			throw new RuntimeException("can't run command in non-existing directory '" + directory + "'");
		}
		ProcessBuilder pb = new ProcessBuilder()
				.command(command)
				.directory(directory.toFile());
		Map<String, String> env = pb.environment();
		//env.put("GIT_SSH_COMMAND", "ssh -o IdentitiesOnly=yes -i id_rsa -F /dev/null");
		env.put("GIT_SSH_COMMAND", "ssh -o IdentitiesOnly=yes -i id_rsa");
		Process p = pb.start();
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
		outputGobbler.start();
		errorGobbler.start();
		int exit = p.waitFor();
		errorGobbler.join();
		outputGobbler.join();
	}

	private static class StreamGobbler extends Thread {
		private final InputStream is;
		private final String type;
		private StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}
		@Override
		public void run() {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is));) {
				String line;
				while ((line = br.readLine()) != null) {
//					System.out.println(type + "> " + line);
					log.info(type + "> " + line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static void generateKeyPair(String email) {
		try {

			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			KeyPair keyPair = generator.generateKeyPair();
			RSAPrivateKey priv = (RSAPrivateKey) keyPair.getPrivate();
			RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();

			Base64.Encoder encoder = Base64.getEncoder();
			String code = "-----BEGIN RSA PRIVATE KEY-----\n";          
			String codenew = encoder.encodeToString(priv.getEncoded());
			//String codenew =Base64.encodeBase64String(priv.getEncoded());
			String myOutput = ""; 
			for (String substring : Splitter.fixedLength(64).split(codenew)) { 
				myOutput += substring + "\n"; 
			}
			code += myOutput.substring(0, myOutput.length() - 1);
			code += "\n-----END RSA PRIVATE KEY-----";                          
//			System.out.println(code);
//			JOptionPane.showMessageDialog(null, code); 
			try {
	            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-------");
				FileAttribute<Set<PosixFilePermission>> fileAttributes = 
					PosixFilePermissions.asFileAttribute(permissions);
				PopiangDigital.fPrv.delete();
				Files.createFile(Paths.get(PopiangDigital.fPrv.getAbsolutePath()), fileAttributes);
			} catch(UnsupportedOperationException z) {
				System.out.println("POSIX ERROR");
			}
//			JOptionPane.showMessageDialog(null, PopiangDigital.fPrv.getAbsolutePath()); 
			Files.write(Paths.get(PopiangDigital.fPrv.getAbsolutePath()), code.getBytes());
			PopiangDigital.fPrv.setWritable(false);
 
			RSAPublicKey rsaPublicKey = (RSAPublicKey) pub;
			ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(byteOs);
			dos.writeInt("ssh-rsa".getBytes().length);
			dos.write("ssh-rsa".getBytes());
			dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
			dos.write(rsaPublicKey.getPublicExponent().toByteArray());
			dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
			dos.write(rsaPublicKey.getModulus().toByteArray());
			//String publicKeyEncoded = new String( Base64.encodeBase64(byteOs.toByteArray()));
			//Base64.Encoder encoder = Base64.getEncoder();
			String publicKeyEncoded = encoder.encodeToString(byteOs.toByteArray());
            String pubStr = "ssh-rsa " + publicKeyEncoded + " "+email;
			Files.write(Paths.get(PopiangDigital.fPub.getAbsolutePath()), pubStr.getBytes(StandardCharsets.UTF_8));

		} catch(Exception z) {
			z.printStackTrace();
		}
	}

	public static void setKeyPair() {
		try {

			PopiangDigital.fPrv = new File(PopiangDigital.workDir+"/.cfg/"+PopiangDigital.sPrv);
			PopiangDigital.fPub = new File(PopiangDigital.workDir+"/.cfg/"+PopiangDigital.sPub);
			if(PopiangDigital.fPrv.exists()) return;

			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			KeyPair keyPair = generator.generateKeyPair();
			RSAPrivateKey priv = (RSAPrivateKey) keyPair.getPrivate();
			RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();

			Base64.Encoder encoder = Base64.getEncoder();
			String code = "-----BEGIN RSA PRIVATE KEY-----\n";          
			String codenew = encoder.encodeToString(priv.getEncoded());
			//String codenew =Base64.encodeBase64String(priv.getEncoded());
			String myOutput = ""; 
			for (String substring : Splitter.fixedLength(64).split(codenew)) { 
				myOutput += substring + "\n"; 
			}
			code += myOutput.substring(0, myOutput.length() - 1);
			code += "\n-----END RSA PRIVATE KEY-----";                          
			System.out.println(code);
			JOptionPane.showMessageDialog(null, code); 
			try {
	            Set<PosixFilePermission> permissions 
					= PosixFilePermissions.fromString("rw-------");
				FileAttribute<Set<PosixFilePermission>> fileAttributes 
					= PosixFilePermissions.asFileAttribute(permissions);
				Files.createFile(Paths.get(
					PopiangDigital.fPrv.getAbsolutePath()), fileAttributes);
			} catch(UnsupportedOperationException z) {
				System.out.println("POSIX ERROR");
			}
			JOptionPane.showMessageDialog(null, PopiangDigital.fPrv.getAbsolutePath()); 
			Files.write(Paths.get(PopiangDigital.fPrv.getAbsolutePath()), code.getBytes());
			PopiangDigital.fPrv.setWritable(false);
 
			RSAPublicKey rsaPublicKey = (RSAPublicKey) pub;
			ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(byteOs);
			dos.writeInt("ssh-rsa".getBytes().length);
			dos.write("ssh-rsa".getBytes());
			dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
			dos.write(rsaPublicKey.getPublicExponent().toByteArray());
			dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
			dos.write(rsaPublicKey.getModulus().toByteArray());
			//String publicKeyEncoded = new String( Base64.encodeBase64(byteOs.toByteArray()));
			//Base64.Encoder encoder = Base64.getEncoder();
			String publicKeyEncoded = encoder.encodeToString(byteOs.toByteArray());
            String pubStr = "ssh-rsa " + publicKeyEncoded + " user";
			Files.write(Paths.get(PopiangDigital.fPub.getAbsolutePath()), pubStr.getBytes(StandardCharsets.UTF_8));

		} catch(Exception z) {
			z.printStackTrace();
		}
	}

	static List<String[]> sparql(Model mo, String sqry) {
		List<String[]> aRet = new ArrayList<>();
		int i1 = sqry.indexOf("SELECT");
		int i2 = sqry.indexOf("WHERE");
		if(i1<0 || i2<=i1) { return aRet; }
		String vars = sqry.substring(i1+6, i2).trim();
		String[] vrs = vars.split(" ");
		if(vrs.length<=0) { return aRet; }
		String[] fds = new String[vrs.length];
		for(int i=0; i<vrs.length; i++) 
			if(!vrs[i].startsWith("?")) return aRet; 
			else fds[i] = vrs[i].substring(1);
		//System.out.println("vars: "+ vars);

		Query qry = QueryFactory.create(sqry);
		QueryExecution qex = QueryExecutionFactory.create(qry, mo);
		//System.out.print("VAR: "); for(String v: fds) System.out.print(" "+v); System.out.println();
		try {
			ResultSet rs = qex.execSelect();
			while( rs.hasNext() ) {
				QuerySolution soln = rs.nextSolution();
				String[] res = new String[fds.length];
				for(int i=0; i<fds.length; i++) {
					try {
						res[i] = soln.get(fds[i]).toString();
						if((i1=res[i].indexOf("^^"))>0) res[i] = res[i].substring(0,i1);
					} catch(Exception x) {}
				}
				aRet.add(res);
			}
		} catch(Exception y) {
			y.printStackTrace();
		}
		return aRet;
	}

	public static void walkRtfTtl() {
		File curDir = PopiangDigital.fWork;
		File rdf = new File(curDir.getAbsolutePath()+"/rdf");
		if(!rdf.exists()) rdf.mkdirs();
		List<File> aRt = new ArrayList<>();
		for(File f : rdf.listFiles()) if(f.isDirectory()) aRt.add(f);
		File[] roots = aRt.toArray(new File[aRt.size()]);
		
//		File[] roots = rdf.listFiles();
		Arrays.sort(roots);
		File[] fss = new File[2];
		for(int i=0; i<roots.length; i++) {
			if(roots[i].getName().equals(PopiangDigital.sCom)) fss[0] = roots[i];
//			if(roots[i].getName().equals("main")) fss[0] = roots[i];
			else if(roots[i].getName().equals(PopiangDigital.sPrefix)) fss[1] = roots[i];
		}
		int df = 0;
		int j=roots.length-1;
		for(int i=j; i>=0; i--) {
			if(roots[i]==fss[0] || roots[i]==fss[1]) df++;
			else { roots[j--] = roots[i]; }
		}
		if(df==2) {
			roots[0] = fss[0];
			roots[1] = fss[1];
		} else if(df==1) {
			if(fss[0]!=null) roots[0] = fss[0];
			if(fss[1]!=null) roots[1] = fss[1];
		}
		PopiangDigital.aRdfFold = roots;
		PopiangDigital.hRdfModelInfo = new Hashtable<>();
		readAllModel();
	}
	public static void readRdfModel(File fMod) {
		RdfModelInfo rdfif = new RdfModelInfo();
		rdfif.analyze(fMod);
		PopiangDigital.hRdfModelInfo.put(rdfif.sPrefix, rdfif);
		log.info("read model: "+rdfif.sPrefix);
	}
	public static void readAllModel() {
		File[] roots = PopiangDigital.aRdfFold;
		for(int i=0; i<roots.length; i++) {
			File[] faRdf = roots[i].listFiles();
System.out.println("CHECK FARDF: "+ roots[i]);
			for(int r=0; r<faRdf.length; r++) {
				readRdfModel(faRdf[r]);
			}
		}
	}

	public static void runAttend() {
		try {
			String[] cmd = null;
			if(PopiangDigital.bWindows) {
				cmd = new String[] {PopiangDigital.workDir+"/bin/windows/gorecv.exe",""};
			} else if(PopiangDigital.bMacos) {
				cmd = new String[] {PopiangDigital.workDir+"/bin/darwin/gorecv",""};
			} else if(PopiangDigital.bLinux) {
				cmd = new String[] {PopiangDigital.workDir+"/bin/linux/gorecv",""};
			} else {
				log.info("UNKNOW OS PLATFORM");
				return;
			}

			while(PopiangDigital.bAttend) {

				File fID = new File(PopiangDigital.workDir+"/.cfg/ATTENDID");
				Files.write(Paths.get(fID.getAbsolutePath()), cmd[1].getBytes());

				cmd[1] = PopiangDigital.sAttendID;
				log.info("COMMAND LINE: "+ cmd[0]);
				ProcessBuilder pb = new ProcessBuilder().
					command(cmd).directory(PopiangDigital.fWork);
				Map<String,String> env = pb.environment();
				env.put("ATTENDID", PopiangDigital.sAttendID);
				Process p = pb.start();
				StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
				StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
				outputGobbler.start();
				errorGobbler.start();
				log.info("JAVA START GO RECV...");
				int exit = p.waitFor();
				log.info("JAVA END GO RECV...");
				errorGobbler.join();
				outputGobbler.join();
			}

		} catch(Exception a) {
			a.printStackTrace();
		}
	}

	public static Model allModel = null;
	public static String[][] saPref;

    public static List<String[]> query(String sel) {
            readAllRdfModel();
			return query(allModel, sel, "");
	}

	public static List<String[]> query(Model mod, String sel, String op) {
        try {

            sel = sel.replace("(*)","/rdf:rest*/rdf:first");

            Map<String,String> tokens = new HashMap<String,String>();
            for(String[] wds : saPref) tokens.put(wds[0]+":",wds[1]);
            tokens.put("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

            String sPattPrf = "("+StringUtils.join(tokens.keySet(),"|")+")";
log.info("patt: "+ sPattPrf);

            Pattern patPrf = Pattern.compile(sPattPrf);
            Pattern patVar = Pattern.compile("(\\?[A-Za-z0-9]+)");

            Matcher varMat = patVar.matcher(sel);
            List<String> aVar = new ArrayList<>();
            while(varMat.find()) { aVar.add(varMat.group(1)); }

            Matcher prfMat = patPrf.matcher(sel);
            List<String> aPrf = new ArrayList<>();
            while(prfMat.find()) { aPrf.add(prfMat.group(1)); }
            List<String> var2 = aVar.stream()
                .distinct().collect(Collectors.toList());
            List<String> prf2 = aPrf.stream()
                .distinct().collect(Collectors.toList());

            StringBuilder sb = new StringBuilder();
            for(String prf : prf2) {
                sb.append(" PREFIX "+ prf + "<"+tokens.get(prf)+"> ");
            }
            sb.append(" SELECT ");
            for(String var : var2) {
                sb.append(" "+var);
            }
            sb.append(" WHERE { ");
            sb.append(sel);
            sb.append(" } ");
			sb.append(" "+op+" ");

            int l = PopiangDigital.sBaseIRI.length();
            List<String[]> res = PopiangUtil.sparql(mod, sb.toString());
            for(String[] r : res) {
                for(int i=0; i<r.length; i++) {
                    if(r[i].startsWith(PopiangDigital.sBaseIRI)) {
                        r[i] = r[i].substring(l).replace("#",":");
					}
                }
            }
            return res;

        } catch(Exception z) {
            log.error("001",z);
        }
        return null;
    }

    public static void readAllRdfModel() {
        try {
log.info("BASE: "+ PopiangDigital.sBaseIRI);
            Enumeration<String> ePrf = PopiangDigital.hRdfModelInfo.keys();
            allModel = ModelFactory.createDefaultModel();
            List<String[]> sapref = new ArrayList<>();
            while(ePrf.hasMoreElements()) {
                String key = ePrf.nextElement();
                RdfModelInfo rmi = PopiangDigital.hRdfModelInfo.get(key);
				log.info("rmi: "+ rmi.fRdf);
                sapref.add(new String[] {rmi.sPrefix, rmi.sBaseIRI});
                allModel.add(rmi.model);
            }    
            saPref = new String[sapref.size()][];
            saPref = sapref.toArray(saPref);
        } catch(Exception z) { 
            log.error("002",z);
        }    
    }    

    public static void readAllRdfModel0() {
        try {
log.info("BASE: "+ PopiangDigital.sBaseIRI);
            Enumeration<String> ePrf = PopiangDigital.hRdfModelInfo.keys();
            allModel = ModelFactory.createDefaultModel();
            List<String[]> sapref = new ArrayList<>();
            while(ePrf.hasMoreElements()) {
                String key = ePrf.nextElement();
                RdfModelInfo rmi = PopiangDigital.hRdfModelInfo.get(key);
                sapref.add(new String[] {rmi.sPrefix, rmi.sBaseIRI});
            }    
            saPref = new String[sapref.size()][];
            saPref = sapref.toArray(saPref);
        } catch(Exception z) { 
            log.error("003",z);
        }    
    }    

	public static void regWithEmailById(String subj, String from
						, Hashtable<String,String> hsProp
                        , Hashtable<String,List<String>> hsText
                        , Hashtable<String,List<byte[]>> hbStrm) {

		List<String> aReply = hsText.get("RTXT");

		log.info("REGISTER WITH EMAIL BY ID: "+subj);
		int i1;
		if((i1=subj.indexOf(":"))<0) {
			aReply.add("โปรดใส่เลขประจำตัวหลัง:");
			return;
		}
		String id = subj.substring(i1+1);
		List<String> aPdfReply = hsText.get("RPDF");
		List<String> aRdfReply = hsText.get("RRDF");
		List<String> aZipReply = hsText.get("RZIP");
		List<String> aJpgReply = hsText.get("RJPG");
		List<byte[]> baaPdf = hbStrm.get("PDF");
		String eml = from.toLowerCase();
        
        String spq = ""
            + " ?a vp:คือ  vo:เจ้าหน้าที่ . "
            + " ?a vp:EmpCard  '"+ id +"' . "
			+ " ?a vp:ThFName ?b . "
			+ " ?a vp:ThLName ?c . "
        ;
		log.info("ID SPQ: "+ spq);
        List<String[]> vals = query(spq);
		log.info("ID RES: "+ vals);

		List<String> aText = hsText.get("TXT");
		log.info("TEXT: "+ aText.size());
		log.info("QUERY: "+ spq);

		String attID = PopiangDigital.sAttendID;

		if(vals.size()==1) {
			String[] wds = vals.get(0);
			String rdfID = wds[0];
			String fname = wds[1];
			String lname = wds[2];
			rdfID = rdfID.replace("#",":");
			String msg = ""
				+ "ขอบคุณสำหรับการลงทะเบียน\n"
				+ "คุณ "+fname+" "+lname+"\n"
				+ "https://dip.popiang.com/attend/"+attID+"/"+rdfID+"\n"
				+ "โปรดคลิกเพื่อเข้างาน\n"
			;
			aReply.add(msg);
		} else if(vals.size()==0) {
			String msg = ""
				+ "ไม่พบอีเมล์ของท่านในฐานข้อมูล\n"
				+ "โปรดติดต่อเจ้าหน้าที่\n"
			;
			aReply.add(msg);
		} else {
			String msg = ""
				+ "อาจมีความผิดพลาดในการลงทะเบียนของท่าน\n"
				+ "โปรดติดต่อเจ้าหน้าที่\n"
			;
			aReply.add(msg);
		}

	}

	public static void saveRegist(String eml, String sbj, String rdfid) {
		try {
			Calendar cal = Calendar.getInstance();
			Date date = cal.getTime();
			SimpleDateFormat date8 = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat datefm = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat timefm = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat stmpfm = new SimpleDateFormat("HHmmss");
			String today = date8.format(date);
			String sdate = datefm.format(date);
			String stime = timefm.format(date);
			String sstmp = stmpfm.format(date);
			File fReg = new File(PopiangDigital.workDir+"/rdf/rg/rg"+today+".ttl");
			log.info("TODAY: "+ today);
			log.info("FILE: "+ fReg.getAbsolutePath());
			log.info("EMAIL REG: "+eml);
			log.info("SUBJ REG: "+sbj);
			File fDir = fReg.getParentFile();
			log.info("FILE: "+ fDir.getAbsolutePath());
			if(!fDir.exists()) fDir.mkdirs();

			if(!fReg.exists()) {
				String pref = ""
				+ "@prefix vp: <http://ipthailand.go.th/rdf/itdep/voc-pred#> .\n"
				+ "@prefix vo: <http://ipthailand.go.th/rdf/itdep/voc-obj#> .\n"
				+ "@prefix com01: <http://ipthailand.go.th/rdf/itdep/com01#> .\n"
				+ "@prefix rg"+today+": <http://ipthailand.go.th/rdf/itdep/rg"+today+"#> .\n\n";

				Files.write(Paths.get(fReg.getAbsolutePath()), pref.getBytes());
			}
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(fReg, true), "UTF-8");
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
			pw.println("rg"+today+":"+sstmp+"   vp:คือ    vo:ลงทะเบียน  ;");
			pw.println("                    vp:ใคร     "+rdfid+" ;");
			pw.println("                    vp:วันที่     '"+sdate+"' ;");
			pw.println("                    vp:เวลา     '"+stime+"' ;");
			pw.println("                    vp:อีเมล์     '"+eml+"' ;");
			pw.println("                    vp:หัวเรื่อง     '"+sbj+"' ");
			pw.println(".");

			pw.close();
		} catch(Exception z) {
			log.error("เกิดข้อผิดพลากขณะลงทะเบียน", z);
		}
	}
	
	public static void regWithEmailByUser(String subj, String from
						, Hashtable<String,String> hsProp
                        , Hashtable<String,List<String>> hsText
                        , Hashtable<String,List<byte[]>> hbStrm) {

		List<String> aReply = hsText.get("RTXT");

		log.info("REGISTER WITH EMAIL BY ID: "+subj);
		int i1;
		if((i1=subj.indexOf(":"))<0) {
			aReply.add("โปรดใส่เลขประจำตัวหลัง:");
			return;
		}
		
		String user = subj.substring(i1+1).toLowerCase();
		user = user.trim();
		user = user.replace("<","").replace(">","");
		String eml = from.toLowerCase();
		
		List<String> aPdfReply = hsText.get("RPDF");
		List<String> aRdfReply = hsText.get("RRDF");
		List<String> aZipReply = hsText.get("RZIP");
		List<String> aJpgReply = hsText.get("RJPG");
		List<byte[]> baaPdf = hbStrm.get("PDF");
		
        
        String spq = ""
            + " ?a vp:คือ  vo:เจ้าหน้าที่ . "
            + " ?a vp:UserName  '"+ user +"' . "
			+ " ?a vp:ThFName ?b . "
			+ " ?a vp:ThLName ?c . "
        ;
		log.info("ID SPQ: "+ spq);
        List<String[]> vals = query(spq);
		log.info("ID RES: "+ vals);

		List<String> aText = hsText.get("TXT");
		log.info("TEXT: "+ aText.size());
		log.info("QUERY: "+ spq);

		String attID = PopiangDigital.sAttendID;

		if(vals.size()==1) {
			String[] wds = vals.get(0);
			String rdfID = wds[0];
			String fname = wds[1];
			String lname = wds[2];
			rdfID = rdfID.replace("#",":");
			String msg = ""
				+ "ยินดีต้อนรับคุณ  "+fname+" "+lname+"\n"
				+ "เข้าสู่ระบบบันทึกเวลาออนไลน์ของกรมทรัพย์สินทางปัญญา\n"
				+ "โปรดคลิกลิ้งเพื่อยืนยันการลงทะเบียน\n\n"
				+ "https://dip.popiang.com/attend/"+attID+"/"+rdfID+"/"+fname+"/"+lname+"\n"
			;
			aReply.add(msg);
			saveRegist(eml, subj, rdfID);

		} else if(vals.size()==0) {
			String msg = ""
				+ "ไม่พบ user ของท่านในฐานข้อมูล\n"
				+ "โปรดติดต่อเจ้าหน้าที่\n"
			;
			aReply.add(msg);
		} else {
			String msg = ""
				+ "อาจมีความผิดพลาดในการลงทะเบียนของท่าน\n"
				+ "โปรดติดต่อเจ้าหน้าที่\n"
			;
			aReply.add(msg);
		}

	}

	public static void regWithEmail(String subj, String from, Hashtable<String,String> hsProp
                        , Hashtable<String,List<String>> hsText
                        , Hashtable<String,List<byte[]>> hbStrm) {

		log.info("REGISTER WITH EMAIL");
		List<String> aReply = hsText.get("RTXT");
		List<String> aPdfReply = hsText.get("RPDF");
		List<String> aRdfReply = hsText.get("RRDF");
		List<String> aZipReply = hsText.get("RZIP");
		List<String> aJpgReply = hsText.get("RJPG");
		List<byte[]> baaPdf = hbStrm.get("PDF");
		String eml = from.toLowerCase();
        
        String spq = ""
            + " ?a vp:คือ  vo:เจ้าหน้าที่ . "
            + " ?a vp:EMail  '"+ eml +"' . "
        ;
        List<String[]> vals = query(spq);

		List<String> aText = hsText.get("TXT");
		log.info("TEXT: "+ aText.size());
		log.info("QUERY: "+ spq);
		log.info("RESNO: "+ vals.size());

		String attID = PopiangDigital.sAttendID;

		if(vals.size()==1) {
			String rdfID = vals.get(0)[0];
			rdfID = rdfID.replace("#",":");
			String msg = ""
				+ "https://dip.popiang.com/attend/"+attID+"/"+rdfID+"\n"
				+ "โปรดคลิกเพื่อเข้างาน\n"
			;
			aReply.add(msg);
		} else if(vals.size()==0) {
			String msg = ""
				+ "ไม่พบอีเมล์ของท่านในฐานข้อมูล\n"
				+ "โปรดติดต่อเจ้าหน้าที่\n"
			;
			aReply.add(msg);
		} else {
			String msg = ""
				+ "อาจมีความผิดพลาดในการลงทะเบียนของท่าน\n"
				+ "โปรดติดต่อเจ้าหน้าที่\n"
			;
			aReply.add(msg);
		}

	}

	public static String makeStaffList() {
		File fTxt = new File(PopiangDigital.workDir+"/res/dip-emp.txt");
		try {
			log.info("EMP:"+fTxt.getAbsolutePath()+" : "+ fTxt.exists());
			BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(fTxt)));
			String line;
			int i=0;
			
			String tx = "";

			List<String> srvs = new ArrayList<>();
			while((line=br.readLine())!=null) {
				String[] wds = line.split("\t");
				if(wds.length<10) continue;
				String empid = wds[0];
				String fngid = wds[1];
				String title = wds[2];
				String thfst = wds[3];
				String thlst = wds[4];
				String enfst = wds[5];
				String enlst = wds[6];
				String cid = wds[7];
				String email = wds[8];
				String user = wds[9];
				cid = cid.replace(" ","").trim();
				email = email.toLowerCase();
				String emsrv = email.substring(email.indexOf("@")+1);

				i++;
				tx += "com01:"+i+"   vp:คือ   vo:เจ้าหน้าที่ ; \n";
				tx += "              vp:EmpCode  '"+empid+"' ;\n";
				tx += "              vp:FingerPrintID '"+fngid+"' ;\n";
				tx += "              vp:TitleID  '"+title+"' ;\n";
				tx += "              vp:ThFName  '"+thfst+"' ;\n";
				tx += "              vp:ThLName  '"+thlst+"' ;\n";
				tx += "              vp:EnFName  '"+enfst+"' ;\n";
				tx += "              vp:EnLName  '"+enlst+"' ;\n";
				tx += "              vp:EmpCard  '"+cid+"' ;\n";
				tx += "              vp:EMail    '"+email+"' ;\n";
				tx += "              vp:UserName '"+user+"' .\n\n";
				
				System.out.println(i+": "+ cid+"   name: "+ thfst+" "+thlst);
				srvs.add(emsrv);
			}
			List<String> srv2 = srvs.stream().sorted().collect(Collectors.toList());     
			Map<String, Long> map = new HashMap<>();
			srvs.forEach(e -> map.put(e, map.getOrDefault(e, 0L) + 1L));
			map.entrySet().stream().forEach(e-> System.out.println(e));
			return tx;
		} catch(Exception z) {
		}
		return null;
	}

	public static void attendReset() {
		try {
			File fAtt = new File(PopiangDigital.workDir+"/.cfg/ATTENDID");
			boolean a = fAtt.delete();
			log.info("ATTEND RESET: "+ a);
		} catch(Exception z) {
			log.error("004", z);
		}
	}

	public static void attendReport(File f) {
		try {
			log.info("ATTEND REPORT");
            File tmp = new File(PopiangDigital.workDir+"/res/template-cover-toc1.docx");
            File out = new File(PopiangDigital.workDir+"/.out/doc/repo2.docx");
            File par = out.getParentFile();
            if(!par.exists()) par.mkdirs();

			log.info("ATTEND REPORT 2");

            XWPFDocument doc = new XWPFDocument(new FileInputStream(tmp));
//            doc.removeBodyElement(0);

			readAllRdfModel();
			Model mo = ModelFactory.createDefaultModel();
			mo.read(new FileInputStream(f), null, "TTL");
			String spq = ""
				+ " ?a vp:คือ ?b . "
				+ " ?a vp:ใคร ?c . "
				+ " ?a vp:วันที่ ?d . "
				+ " ?a vp:เวลา ?e . "
				+ " ?a vp:พิกัด ?f  . "
				+ " ?a vp:ภาพ ?g  . "
			;

			List<String[]> vals = query(mo, spq, " ORDER BY ?e ");
			for(String[] val : vals) {
				String spq2 = ""
					+ " "+ val[2] + " vp:FingerPrintID ?a . "
					+ " "+ val[2] + " vp:ThFName ?b . "
					+ " "+ val[2] + " vp:ThLName ?c . "
				;
				List<String[]> subs = query(allModel, spq2, "");
				if(subs.size()!=1) continue;
System.out.println("ROW: "+ val[0]+" ev:"+val[1]);
				String[] sub = subs.get(0);
				String rdf = val[0];
				String evt = val[1];
				String who = val[2]; // rdfid
				String day = val[3];
				String tim = val[4];
				String gps = val[5];
				String pic = val[6];
				String fng = sub[0];
				String nam = sub[1]+" "+sub[2];
System.out.println(rdf+" "+evt+" "+who+" "+day+" "+pic);
				pic = pic.substring(1);
				File fImg = new File(PopiangDigital.workDir+"/"+pic);

				XWPFParagraph pp;
				XWPFRun rr;

				pp = doc.createParagraph();
				pp.setStyle("Normal");
				rr = pp.createRun();
				rr.setText("รหัสเหตุการ "+rdf);
				rr.addBreak();
				rr.setText("เหุตการ "+evt);
				rr.addBreak();
				rr.setText("เวลา "+ tim);
				rr.addBreak();
				rr.setText("ตำแหน่งพิกัด "+ gps);
				rr.addBreak();
				rr.setText("ไอดีลายนิ้วมือ "+fng);
				rr.addBreak();
				rr.setText("ชื่อ "+nam);
				rr.addBreak();
				rr.addPicture(new FileInputStream(fImg)
					, XWPFDocument.PICTURE_TYPE_JPEG, fImg+""
					, Units.toEMU(400), Units.toEMU(250));
				rr.addBreak();
				

			}

            log.info("temp file: "+ tmp);
            log.info("..."+tmp.exists());

            try (FileOutputStream fos = new FileOutputStream(out)) {
                doc.write(fos);
            }    

		} catch(Exception z) {
			log.error("005", z);
		}
	}

	public static void makeScanFile(File f) {
		try {

			String date = "000000";
			System.out.println("==========================================================");
			String fn = f.getName();
			fn = fn.substring(0,fn.indexOf(".ttl"))+".txt";
			
			System.out.println("file:"+fn);
			
			File fScan = new File(".out/"+fn);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fScan)));
			
			StringBuffer scanfile = new StringBuffer();
			
			readAllRdfModel();
			Model mo = ModelFactory.createDefaultModel();
			mo.read(new FileInputStream(f), null, "TTL");
			String spq = ""
				+ " ?a vp:คือ ?b . "
				+ " ?a vp:ใคร ?c . "
				+ " ?a vp:วันที่ ?d . "
				+ " ?a vp:เวลา ?e . "
				+ " ?a vp:พิกัด ?f  . "
				+ " ?a vp:ภาพ ?g  . "
			;

			List<String[]> vals = query(mo, spq, " ORDER BY ?e ");
			for(String[] val : vals) {
				String spq2 = ""
					+ " "+ val[2] + " vp:FingerPrintID ?a . "
					+ " "+ val[2] + " vp:ThFName ?b . "
					+ " "+ val[2] + " vp:ThLName ?c . "
				;
				date = val[3];
				date = date.replace("-","").substring(2);
				String time = val[4];
				time = time.replace(":","");
				if(date.length()==6) {
					date = date.substring(4,6)+date.substring(2,4)+date.substring(0,2);
				}
				if(time.length()==6) {
					time = time.substring(0,4);
				}
				
				List<String[]> subs = query(allModel, spq2, "");
				/*
				System.out.print(
					"ID:"+val[0]
					+" : เหตุการ "+ val[1]
					+" RDFID "+ val[2]
					+" DATE:"+val[3]
					+" TIME:"+val[4]
					+" GPS: "+val[5]
				);
				*/
				String fingerID = "";
				for(String[] sub : subs) {
					fingerID = sub[0];
					/*
					System.out.print(" : "+sub[0]+" : "+sub[1] + " : "+ sub[2]);
					*/
				}
				String line = fingerID+" "+date+" "+time;
				System.out.println("LINE:"+line);
				bw.write(line);
				bw.newLine();
				scanfile.append(line+"\n");
			}
			bw.close();
		} catch(Exception z) {
			log.error("006", z);
		}
	}
}

