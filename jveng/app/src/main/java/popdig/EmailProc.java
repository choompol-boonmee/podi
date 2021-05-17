package popdig;

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
import org.apache.log4j.Logger;

public class EmailProc {

	static Logger log = Logger.getLogger(EmailProc.class);

	SimpleDateFormat fmfn = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", new Locale("uk","UK"));

	Hashtable<String,byte[]> hsHead;
	int nMsgChkTm = 60; // get message count every ? second
	int nMsgChkMax = 20; // stay use count

	int msgcnt = 0;

	volatile boolean bImap = false;
	volatile int nCntFromLastMsg = 0;
	int nMaxFromLastMsg = 1000;
	IMAPFolder folder = null;

	File fBaseDir, fUser;
	Object objSync1 = new Object();
	String replyProc = null;


	public void startEmailThread() {
			fBaseDir = new File(PopiangDigital.workDir+"/.eml");
			if(!fBaseDir.exists()) fBaseDir.mkdirs();
			fUser = new File(fBaseDir+"/memb");
System.out.println("base:"+ fBaseDir.getAbsolutePath());
			new Thread() { public void run() {
				while(true) {
					bImap = true;
					Thread fet = new Thread() { public void run() { imapFetchThread(); } };
					Thread idl = new Thread() { public void run() { imapIdleThread(); } };
					Thread prc = new Thread() { public void run() { procParseThread(); } };
					fet.start();
					idl.start();
					prc.start();
					try {
						fet.join();
						idl.join();
						prc.join();
					} catch(Exception z) {}
				}
			} }.start();
	}
	public void imapFetchThread() {
		while(bImap) {
			folder = getEmailFolder();
			if(folder==null || !folder.isOpen()) {
				try { Thread.sleep(5000); } catch(Exception y) {}
				continue;
			}
			int msgCnt = 0;
			try {
				msgCnt = folder.getMessageCount();
				nCntFromLastMsg = 0;
			} catch(Exception z) {
				try { Thread.sleep(5000); } catch(Exception y) {}
				continue;
			}
			if(msgCnt>0) procMess();
			folder.addMessageCountListener(new MessageCountListener() {
				public void messagesAdded(MessageCountEvent ev) {
					nCntFromLastMsg = 0;
					procMess();
				}
				public void messagesRemoved(MessageCountEvent ev) {
					nCntFromLastMsg = 0;
				}
			});
			for(int c=0; c<nMsgChkMax; c++) {
				try {
					//Thread.sleep(1 * 60 * 1000);
					Thread.sleep(nMsgChkTm * 1000);
					msgCnt = folder.getMessageCount();
					nCntFromLastMsg = 0;
//					System.out.println("MSG: "+ msgCnt);
					//command.execute(command.line2exec("alive"));
				} catch(Exception z) {
					bImap = false;
					break;
				}
			}
	
			try {
				folder.close(true);
				System.out.println("==== CLOSE EMAIL FOLDER ====");
			} catch(Exception z) {
				System.exit(10);
			}
		}
	}

	public void imapIdleThread() {
		while(bImap) {
			try { Thread.sleep(1000); } catch(Exception y) {}
			nCntFromLastMsg++;
			if(nCntFromLastMsg>=nMaxFromLastMsg) {
				bImap = false;
				System.out.println("MAX COUNT FROM LAST MAIL MESSAGE "+ nCntFromLastMsg);
			}
			//if (!folder.isOpen()) folder = getEmailFolder();
			try { folder.idle(); } catch(Exception x) {}
		}
	}

	public void imapfetch() {
System.out.println("=======  IMAP FETCH ");
		new Thread() { public void run() {

			while(true) {
//System.out.println("IMAP FETCH: 1");
				folder = getEmailFolder();
//System.out.println("IMAP FETCH: 1.5 "+folder);
				if(folder==null || !folder.isOpen()) {
					try { Thread.sleep(5000); } catch(Exception y) {}
					continue;
				}
//System.out.println("IMAP FETCH: 2");
				int msgCnt = 0;
				try {
					msgCnt = folder.getMessageCount();
					nCntFromLastMsg = 0;
				} catch(Exception z) {
					try { Thread.sleep(5000); } catch(Exception y) {}
					continue;
				}
System.out.println("IMAP FETCH: 3 "+msgCnt);
				if(msgCnt>0) procMess();
System.out.println("folder: "+ folder);
				folder.addMessageCountListener(new MessageCountListener() {
					public void messagesAdded(MessageCountEvent ev) {
						nCntFromLastMsg = 0;
System.out.println("4.1 msg added");
						procMess();
					}
					public void messagesRemoved(MessageCountEvent ev) {
						nCntFromLastMsg = 0;
					}
				});
System.out.println("4.2 read msg");
				//for(int c=0; c<20; c++) {
				for(int c=0; c<nMsgChkMax; c++) {
					try {
						//Thread.sleep(1 * 60 * 1000);
						Thread.sleep(nMsgChkTm * 1000);
						msgCnt = folder.getMessageCount();
						nCntFromLastMsg = 0;
						System.out.println("MSG: "+ msgCnt);
						//command.execute(command.line2exec("alive"));
					} catch(Exception z) {
						System.exit(10);
						break;
					}
				}
		
				try {
					folder.close(true);
					System.out.println("==== CLOSE EMAIL FOLDER ====");
				} catch(Exception z) {
					System.exit(10);
				}
			}
		} }.start();
		for (;;) {
			try { Thread.sleep(1000); } catch(Exception y) {}
			nCntFromLastMsg++;
			if(nCntFromLastMsg>=nMaxFromLastMsg) {
				System.out.println("MAX COUNT FROM LAST MAIL MESSAGE "+ nCntFromLastMsg);
				System.exit(10);
			}
			//if (!folder.isOpen()) folder = getEmailFolder();
			try { folder.idle(); } catch(Exception x) {}
		}
	}

	public void procMess() {
		if(folder==null) return;
		try {
			Message[] msgs = folder.getMessages();
			for(int i=0; i<msgs.length; i++) {
				nCntFromLastMsg = 0;
				try {
					Message msg = msgs[i];
					Date recvDate = msg.getReceivedDate();
					String sRecvDate = fmfn.format(recvDate);
					Address[] from = msg.getFrom();
					String sFrom = from[0].toString();
					int i1 = sFrom.indexOf("<");
					int i2 = sFrom.indexOf(">");
					if(i1>0 && i2>i1) sFrom = sFrom.substring(i1+1, i2);
					InetAddress addr = InetAddress.getByName(PopiangDigital.sImap);
					String sFN = sRecvDate + "_"+sFrom+".jlm";

					File fFP = new File(fBaseDir + "/inb/"+ sRecvDate.substring(0,8));
					File fFNt = new File(fBaseDir+ "/temp.jml");
					File fFN = new File(fFP+ "/"+ sFN);
	
					Hashtable<String,String> hsProp;
					Hashtable<String,List<String>> hsText;
					Hashtable<String,List<byte[]>> hbStrm;
					hsProp = new Hashtable<>();
					hsText = new Hashtable<>();
					hbStrm = new Hashtable<>();
					// EMAIL PARSING
					hsProp.put("RECV", sRecvDate);
					parseEmail(msg, hsProp, hsText, hbStrm);
					FileOutputStream fos = new FileOutputStream(fFNt);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(hsProp);
					oos.writeObject(hsText);
					oos.writeObject(hbStrm);
					oos.close();
	
					if(!fFP.exists()) fFP.mkdirs();
					fFNt.renameTo(fFN);
	
					//System.out.println(msg.getMessageNumber()+" : "+ msg.getSubject()+" d:"+fFN
					//	+" : "+ fFN.exists()+" : "+ fFNt.exists());
	
					try {
						msg.setFlag(Flags.Flag.DELETED, true);
					} catch(javax.mail.MessageRemovedException x) {
					}
				} catch(Exception z) {
					log.error("001",z);
				}
			}
			folder.expunge();
		} catch(MessagingException mex) {
		}
	}

	IMAPFolder getEmailFolder()  {
		try {
			Properties propImap = System.getProperties();
			propImap.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			propImap.put("mail.imap.socketFactory.fallback", "false");
			propImap.put("mail.imap.socketFactory.port", "993");
			propImap.put("mail.imaps.ssl.trust", "*");
			Session session = Session.getInstance(propImap, null);
			Store store = session.getStore("imap");
//System.out.println("imap:"+imap+" em:"+email+" pw:"+pass);
//			store.connect(imap, email, pass);
			store.connect(PopiangDigital.sImap, PopiangDigital.sRecvEmail, PopiangDigital.sPassWord);
			folder = (IMAPFolder) store.getFolder("Inbox");
			if (folder == null || !folder.exists()) {
				System.out.println("Invalid folder");
				return null;
			}
//System.out.println("IMAP ..2");
			folder.open(Folder.READ_WRITE);
		} catch(Exception x) {
//			log.info(x);
//			x.printStackTrace();
			folder = null;
		}
		return folder;
	}

	void parseEmail(Message msg, Hashtable<String,String> hsProp
		, Hashtable<String,List<String>> hsText
		, Hashtable<String,List<byte[]>> hbStrm) {
	
		try {
				
			hsText.put("TXT", new ArrayList<>());
			hsText.put("RTXT", new ArrayList<>());
			hsText.put("RPDF", new ArrayList<>());
			hsText.put("RJPG", new ArrayList<>());
			hsText.put("RXLS", new ArrayList<>());
			hsText.put("HTM", new ArrayList<>());
			hsText.put("RRDF", new ArrayList<>());
			hsText.put("RZIP", new ArrayList<>());
	
			hbStrm.put("JPG", new ArrayList<>());
			hbStrm.put("PNG", new ArrayList<>());
			hbStrm.put("GIF", new ArrayList<>());
			hbStrm.put("PDF", new ArrayList<>());
			hbStrm.put("XLS", new ArrayList<>());
			hbStrm.put("XML", new ArrayList<>());
			hbStrm.put("TXT", new ArrayList<>());
			hbStrm.put("HTM", new ArrayList<>());
			hbStrm.put("JAR", new ArrayList<>());
			hbStrm.put("ZIP", new ArrayList<>());
	
			String sSubj = msg.getSubject();
			Address[] from = msg.getFrom();
			String sFrom = from[0].toString();
			Date recvDate = msg.getReceivedDate();
			String sRecv = fmfn.format(recvDate);
			Address[] replyTo = msg.getReplyTo();
			String sReply = replyTo[0].toString();
			hsProp.put("SUBJ", sSubj);
			hsProp.put("FROM", sFrom);
			hsProp.put("RECV", sRecv);
			hsProp.put("REPLY", sReply);
	
			Object content = msg.getContent();
			if(content instanceof Multipart) {
				Multipart mp = (Multipart) content;
				parseMultipart(mp, hsProp, hsText, hbStrm);
			}
			else if(content instanceof String) {
				String txt = (String) content;
				hsText.get("TXT").add(txt);
			} else if(content instanceof BASE64DecoderStream) {
				BASE64DecoderStream base64 = (BASE64DecoderStream) content;
				parseBase64(base64, hbStrm);
			} else {
			}
		} catch(Exception x) {
		}
	}
	
	void parseBase64(BASE64DecoderStream strm, Hashtable<String,List<byte[]>> hbStrm) {
		try {
			//byte[] buff = new byte[4096];
			int len;
			byte[] buf = new byte[4096];
	
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while((len=strm.read(buf,0,buf.length))>0) {
				baos.write(buf, 0, len);
			}
			byte[] buff = baos.toByteArray();
			if(hsHead==null) {
				hsHead = new Hashtable<>();
				byte[] hdPNG = { (byte)0x89, (byte)0x50, (byte)0x4e, (byte)0x47, (byte)0x0d, (byte)0x0a };
				byte[] hdJPG = { (byte)0xff, (byte)0xd8 };
				byte[] hdJPg = { (byte)0xd8, (byte)0xff };
				byte[] hdGIF7 = "GIF87a".getBytes("UTF-8");
				byte[] hdGIF9 = "GIF87a".getBytes("UTF-8");
				byte[] hdPDF7 = "%PDF-1.7".getBytes("UTF-8");
				hsHead.put("PNG", hdPNG);
				hsHead.put("JPG", hdJPG);
				hsHead.put("JPG.2", hdJPg);
				hsHead.put("GIF.7", hdGIF7);
				hsHead.put("GIF.9", hdGIF9);
				hsHead.put("PDF.7", hdPDF7);
			}
			Enumeration<String> keys = hsHead.keys();
			int difCnt = 0;
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				byte[] head = hsHead.get(key);
				if(buff.length<head.length) continue;
				for(; difCnt<head.length; difCnt++)
					if(head[difCnt]!=buff[difCnt]) break;
				if(difCnt==head.length) {
					int i1 = key.indexOf(".");
					if(i1>0) key = key.substring(0,i1);
					List<byte[]> baAtt = hbStrm.get(key);
					if(baAtt!=null) {
						baAtt.add(buff);
						//System.out.println("===== FOUND ADD: "+ key+" : "+ baAtt);
					}
				}
			}
		} catch(Exception x) {
		}
	}

	String getCharSet(String type) {
		String charset = "UTF-8";
		int i1;
		if((i1=type.indexOf("CHARSET="))>=0) {
			charset = type.substring(i1+8);
			if(charset.equals("ISO-8859-1")) {
				charset = "TIS-620";
			}
			else if(charset.equals("WINDOWS-874")) {
				charset = "TIS-620";
			}
		}
		return charset;
	}
	
	void parseMultipart(Multipart mp
		, Hashtable<String,String> hsProp
		, Hashtable<String,List<String>> hsText
		, Hashtable<String,List<byte[]>> hbStrm) {
	
		String line;
		ByteArrayOutputStream baos;
		int len;
		byte[] buff = new byte[4096];
		try {
			for (int j=0; j < mp.getCount(); j++) {
				Part part = mp.getBodyPart(j);
				String disposition = part.getDisposition();
				String fname = part.getFileName();
				String fname0 = fname==null? null : fname.toUpperCase();
				if(part instanceof MimeBodyPart) {
					MimeBodyPart mime = (MimeBodyPart) part;
					String type = mime.getContentType().toUpperCase();
					if(type.startsWith("TEXT/PLAIN")) {
						InputStream is = mime.getInputStream();
						String charset = getCharSet(type);
						baos = new ByteArrayOutputStream();
						while((len=is.read(buff,0,buff.length))>0) {
							baos.write(buff,0,len);
						}
						byte[] bres = baos.toByteArray();
						String txt = new String(bres, charset);
						txt = txt.trim();
						if(txt.length()>0) {
							if(txt.startsWith("<div dir=")) {
								//log.warning("===== TXT2: "+ txt);
							}
							//aText.add(txt);
							hsText.get("TXT").add(txt);
						}
						hbStrm.get("TXT").add(bres);
					}
					else if(type.startsWith("TEXT/HTML")) {
						String charset = getCharSet(type);
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) {
							baos.write(buff,0,len);
						}
						byte[] bres = baos.toByteArray();
						hbStrm.get("HTM").add(bres);
						String html = new String(bres, charset);
						html = html.trim();
						hsText.get("HTM").add(html);
						String txt = Jsoup.parse(html).text();
						if(txt.length()>0) {
							if(txt.startsWith("<div dir=")) {
								//log.warning("===== TXT2: "+ txt);
							}
							hsText.get("TXT").add(txt);
						}
						hbStrm.get("TXT").add(bres);
					}
					else if( ( type.startsWith("APPLICATION/PDF")
						|| type.startsWith("APPLICATION/X-PDF"))) {
						//&& disposition!=null && fname!=null && fname0.endsWith(".PDF")) {
						//System.out.println("PDF fname:"+fname);
						//System.out.println("PDF fname0:"+fname0);
						//System.out.println("PDF dipo:"+disposition);
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("PDF").add(bres);
					}
					else if( type.startsWith("IMAGE/JPEG") ) {
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("JPG").add(bres);
					}
					else if( type.startsWith("IMAGE/PNG") ) {
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("PNG").add(bres);
					}
					else if( type.startsWith("IMAGE/GIF") ) {
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("GIF").add(bres);
					}
					else if( type.startsWith("IMAGE/JPG") ) {
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("JPG").add(bres);
					}
					else if( type.startsWith("APPLICATION/XML")
							&& disposition!=null && fname!=null && fname0.endsWith(".XML")) {
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("XML").add(bres);
					}
					else if(type.startsWith("MULTIPART/ALTERNATIVE")) {
						Multipart mp2 = (Multipart) mime.getContent();
						parseMultipart(mp2, hsProp, hsText, hbStrm);
					}
					else if(type.startsWith("MULTIPART/RELATED")) {
						Multipart mp2 = (Multipart) mime.getContent();
						parseMultipart(mp2, hsProp, hsText, hbStrm);
					}
					else if(type.startsWith("MULTIPART/RELATIVE")) {
						Multipart mp2 = (Multipart) mime.getContent();
						parseMultipart(mp2, hsProp, hsText, hbStrm);
					}
					else if(type.startsWith("APPLICATION/PKCS7-SIGNATURE")) {
						//log.info("Digital Signature");
					}
					else if(fname!=null && fname.toUpperCase().endsWith("JPG")) {
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("JPG").add(bres);
					}
					else if(fname!=null && fname.toUpperCase().endsWith("PNG")) {
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("PNG").add(bres);
					}
					else if(fname!=null && fname.toUpperCase().endsWith("GIF")) {
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("GIF").add(bres);
					}
					else if(fname!=null && fname.toUpperCase().endsWith("PDF")) {
						System.out.println("PDF fname2:"+fname);
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("PDF").add(bres);
					}
					else if(fname!=null && fname.toUpperCase().endsWith("XLSX")) {
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("XLS").add(bres);
					}
					else if(fname!=null && fname.toUpperCase().endsWith("JAR")) {
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("JAR").add(bres);
					}
					else if(fname!=null && fname.toUpperCase().endsWith("ZIP")) {
						baos = new ByteArrayOutputStream();
						InputStream is = mime.getInputStream();
						while((len=is.read(buff,0,buff.length))>0) baos.write(buff,0,len);
						byte[] bres = baos.toByteArray();
						hbStrm.get("ZIP").add(bres);
					}
					else {
					}
				}
			}
		} catch(Exception x) {
			log.error("002",x);
		}
	}

	void procParseThread() {
		File fInbDir = new File(fBaseDir+"/inb");
		while(bImap) {
			try {
				if(!fInbDir.exists()) { Thread.sleep(2000); continue; }
				String[] dates = fInbDir.list();
				Arrays.sort(dates);
				for(int i=0; i<dates.length; i++) {
					if(dates[i].length()!=8) {
						continue;
					}
					File fDate = new File(fInbDir + "/"+ dates[i]);
					String[] times = fDate.list();
					Arrays.sort(times);
					for(int j=0; j<times.length; j++) {
						File fEml = new File(fDate+"/"+times[j]);
						String emltm = times[j];
						if(emltm.length()>24) {
							emltm = emltm.substring(0,19);
							try (InputStream is = new FileInputStream(fEml)) {
								ObjectInputStream ois = new ObjectInputStream(is);
	
								@SuppressWarnings("unchecked")
								Hashtable<String,String> hsProp =
								  (Hashtable<String,String>) ois.readObject();
								@SuppressWarnings("unchecked")
								Hashtable<String,List<String>> hsText =
								  (Hashtable<String,List<String>>) ois.readObject();
								@SuppressWarnings("unchecked")
								Hashtable<String,List<byte[]>> hbStrm =
								  (Hashtable<String,List<byte[]>>) ois.readObject();
	
								String from = getFrom(hsProp);
								if(!fUser.exists()) fUser.mkdirs();
								File fEmIn = new File(fUser+"/"+from+"/inbox");
								if(!fEmIn.exists()) fEmIn.mkdirs();
								File fEmOut = new File(fUser+"/"+from+"/outbox");
								if(!fEmOut.exists()) fEmOut.mkdirs();
								File fEmInf = new File(fUser+"/"+from+"/info");
								if(!fEmInf.exists()) fEmInf.mkdirs();
	
								File fEmTo = new File(fEmIn+"/"+fEml.getName());
								fEml.renameTo(fEmTo);
	
								procEmail(emltm, hsProp, hsText, hbStrm);
							} catch(Exception z) {
								log.error("003", z);
							}
						}
						//System.out.println("DELETE ======================================");
						fEml.delete();
					}
					String[] files = fDate.list();
					if(files!=null && files.length==0) {
					//if(fDate.list().length==0) {
						//System.out.println("DELETE FOLDER");
						fDate.delete();
					}
				}
			} catch(Exception z) {
				log.error("004", z);
			}
		}
	}

	String getSubj(Hashtable<String,String> hsProp) {
		String subj = hsProp.get("SUBJ");
		if(subj==null) subj = "";
		subj = subj.toUpperCase();
		while(subj.length()>0) {
			subj = subj.trim();
			if(subj.startsWith("RE: ")) { subj = subj.substring(4); continue; }
			if(subj.startsWith("RE:")) { subj = subj.substring(3); continue; }
			break;
		}
		return subj;
	}
	String getFrom(Hashtable<String,String> hsProp) {
		String sFrom = hsProp.get("FROM");
		sFrom = sFrom.toUpperCase();
		int i1 = sFrom.indexOf("<");
		int i2 = sFrom.lastIndexOf(">");
		if(i1>0 && i2>i1) { sFrom = sFrom.substring(i1+1, i2); }
		return sFrom;
	}

	void procEmail(String emltm, Hashtable<String,String> hsProp
					   , Hashtable<String,List<String>> hsText
					   , Hashtable<String,List<byte[]>> hbStrm) {
		try {
			String subj = getSubj(hsProp);
			String from = getFrom(hsProp);
			log.info("PROC EMAIL: "+ subj+" "+from);
			String subj0 = hsProp.get("SUBJ");
	
			execEmail(subj, from, hsProp, hsText, hbStrm);
			List<String> aReply = hsText.get("RTXT");
			String replyMsg = null;
			if(aReply==null || aReply.size()==0) {
				replyMsg = "NO REPLY MESSAGE";
			} else {
				StringBuilder stb = new StringBuilder();
				for(String rep : aReply) { stb.append(rep); stb.append("\r\n"); }
				replyMsg = stb.toString();
			}
			String replyTo = hsProp.get("REPLY");
			log.info("REPLY "+replyMsg + " to "+ replyTo);

			//if(!replyTo.equals(email)) {
			if(!replyTo.equals(PopiangDigital.sRecvEmail)) {
				sendMail(replyTo, subj, replyMsg, hsText);
			}
		} catch(Exception z) {
			log.error("005", z);
		}
	}
	
	void sendMail(String replyTo, String subj, String replyMsg, Hashtable<String,List<String>> hsText) {
		try {
			String rto = ""+replyTo;
			if(rto.indexOf("Delivery")>=0) return;

			System.out.println("SEND TO "+replyTo+" with:"+subj);
			Properties propSmtp = System.getProperties();
			propSmtp.put("mail.smtp.host", PopiangDigital.sSmtp);
			propSmtp.put("mail.smtp.socketFactory.port", "465");
			propSmtp.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			propSmtp.put("mail.smtp.ssl.trust", "*");
			propSmtp.put("mail.smtp.auth", "true");
			propSmtp.put("mail.smtp.port", "465");
	
			Session session = Session.getInstance(propSmtp);
			MimeMessage replyMessage = new MimeMessage(session);
			replyMessage.setFrom(new InternetAddress(PopiangDigital.sRecvEmail));
			InternetAddress[] aReplyTo = { new InternetAddress(replyTo) };
			replyMessage.setSubject("RE: "+subj);
			replyMessage.setReplyTo(aReplyTo);
	
			List<String> aReplyRdf = hsText.get("RRDF");
			List<String> aReplyZip = hsText.get("RZIP");
			List<String> aReplyPdf = hsText.get("RPDF");
			List<String> aReplyXls = hsText.get("RXLS");
			List<String> aReplyJpg = hsText.get("RJPG");
			int cnt = aReplyRdf.size() + aReplyZip.size() + aReplyPdf.size() + aReplyXls.size()
					+ aReplyJpg.size();
			if(cnt==0) {
				//System.out.println("   MSG: "+ replyMsg);
				replyMessage.setContent(replyMsg, "text/plain; charset=utf-8");
			} else {
	
				Multipart mp1 = new MimeMultipart();
				MimeBodyPart textPart = new MimeBodyPart();
				textPart.setContent(replyMsg, "text/plain; charset=utf-8");
				mp1.addBodyPart(textPart);
				replyMessage.setContent(mp1);
	
				for(int j=0; aReplyRdf!=null && j<aReplyRdf.size(); j++) {
					MimeBodyPart attachment1 = new MimeBodyPart();
					String file = aReplyRdf.get(j);
					attachment1.attachFile(file);
					//System.out.println("ATTACH:"+ new File(file).length());
					mp1.addBodyPart(attachment1);
				}
				for(int j=0; aReplyZip!=null && j<aReplyZip.size(); j++) {
					MimeBodyPart attachment1 = new MimeBodyPart();
					String file = aReplyZip.get(j);
					attachment1.attachFile(file);
					//System.out.println("ATTACH:"+ new File(file).length());
					mp1.addBodyPart(attachment1);
				}
				for(int j=0; aReplyPdf!=null && j<aReplyPdf.size(); j++) {
					MimeBodyPart attachment1 = new MimeBodyPart();
					String file = aReplyPdf.get(j);
					attachment1.attachFile(file);
					//System.out.println("ATTACH:"+ new File(file).length());
					mp1.addBodyPart(attachment1);
				}
				for(int j=0; aReplyXls!=null && j<aReplyXls.size(); j++) {
					MimeBodyPart attachment1 = new MimeBodyPart();
					String file = aReplyXls.get(j);
					attachment1.attachFile(file);
					mp1.addBodyPart(attachment1);
				}
				for(int j=0; aReplyJpg!=null && j<aReplyJpg.size(); j++) {
					MimeBodyPart attachment1 = new MimeBodyPart();
					String file = aReplyJpg.get(j);
					attachment1.attachFile(file);
					mp1.addBodyPart(attachment1);
				}
			}
			Transport t = session.getTransport("smtp");
			try {
				t.connect(PopiangDigital.sRecvEmail, PopiangDigital.sPassWord);
				t.sendMessage(replyMessage, aReplyTo);
			} catch(Exception x) {
				log.error("006", x);
			} finally {
				t.close();
			}

		} catch(Exception z) {
			log.error("007", z);
		}
	}

	String getCommand(Hashtable<String,List<String>> hsText) {
		String txt = null;
		List<String> aText = hsText.get("TXT");
		for(int i=0; i<aText.size(); i++) {
			int ii;
			String txt0 = aText.get(i);
			if((ii=txt0.indexOf("..."))>=0) {
				txt = txt0.substring(0, ii).trim();
				break;
			}
		}
		return txt;
	}

	void saveinfo(String from, String para, String txt) {
		try {
			if(from.indexOf("MAILER")>=0) return;
			File f = new File(fUser+"/"+from+"/info/"+para+".txt");
			//System.out.println("user:"+ f.getAbsolutePath());
			//System.out.println("text:"+txt.length()+"["+txt+"]");
			FileOutputStream fos = new FileOutputStream(f);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			bw.write(txt);
			bw.close();
		} catch(Exception z) {
			log.error("007", z);
		}
	}
	
	String readinfo(String from, String para) {
		try {
			Path pth = Paths.get(fUser+"/"+from+"/info/"+para+".txt");
			byte[] buf = Files.readAllBytes(pth);
			String ss = new String(buf, "UTF-8");
			return ss;
		} catch(Exception z) {
			log.error("008", z);
		}
		return "";
	}

	void record(String sig, Hashtable<String,String> hsProp) throws Exception {
		System.out.println(hsProp);
/*
		String rcv = hsProp.get("RECV");
		rcv = rcv.substring(0,15);
		String frm = hsProp.get("FROM");
	
		File dttl = new File("G:\\dad\\wk02\\dip-repair\\msg\\"+rcv);
		if(!dttl.exists()) dttl.mkdirs();
		File fttl = new File("G:\\dad\\wk02\\dip-repair\\msg\\"+rcv+"\\repair-"+rcv+".ttl");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fttl)));
		bw.write("<:repair-"+rcv+"> <v1:a> <v1:"+sig+"> .");
		bw.newLine();
		bw.write("<:repair-"+rcv+"> <v1:image> 'image.jpg' .");
		bw.newLine();
		bw.write("<:repair-"+rcv+"> <v1:from> '"+frm+"' .");
		bw.newLine();
		bw.close();
	
		Path directory = Paths.get("G:\\dad\\wk02\\dip-repair");
		gitPull(directory);
		gitStage(directory);
		try {
			gitCommit(directory, "Add "+sig);
			gitPush(directory);	
		} catch(Exception z) {}
*/
	}

	void execEmail(String subj, String from, Hashtable<String,String> hsProp
					   , Hashtable<String,List<String>> hsText
					   , Hashtable<String,List<byte[]>> hbStrm) {
		//bShowLog = true;
		String txt = getCommand(hsText);
		List<String> aReply = hsText.get("RTXT");
		List<String> aPdfReply = hsText.get("RPDF");
		List<String> aRdfReply = hsText.get("RRDF");
		List<String> aZipReply = hsText.get("RZIP");
		List<String> aJpgReply = hsText.get("RJPG");
		List<byte[]> baaPdf = hbStrm.get("PDF");
	
		if("NAME".equals(subj)) {
			saveinfo(from, subj, txt);
			aReply.add("ได้ชื่อแล้วค่ะ '"+txt+"'");
//		} else if("REGISTER".equals(subj)) {
//			PopiangUtil.regWithEmail(subj, from, hsProp, hsText, hbStrm);
		} else if(subj.startsWith("REGBYID:")) {
			PopiangUtil.regWithEmailById(subj, from, hsProp, hsText, hbStrm);
		} else if(subj.startsWith("REGIST:")) {
			PopiangUtil.regWithEmailByUser(subj, from, hsProp, hsText, hbStrm);
		} else if("BROKE".equals(subj)) {
			try {
				record("broke", hsProp);
			} catch(Exception z) {}
			aReply.add("BROKE");
		} else if("TAKE".equals(subj)) {
			try {
				record("take", hsProp);
			} catch(Exception z) {}
			aReply.add("TAKE");
		} else if("RETURN".equals(subj)) {
			try {
				record("return", hsProp);
			} catch(Exception z) {}
			aReply.add("RETURN");
		} else if("FAIL".equals(subj)) {
			try {
				record("fail", hsProp);
			} catch(Exception z) {}
			aReply.add("FAIL");
		} else if("CLIP1".equals(subj) || "CLIP2".equals(subj) || "CLIP3".equals(subj)
			|| "CLIP4".equals(subj) || "CLIP5".equals(subj) || "CLIP6".equals(subj)
			|| "CLIP7".equals(subj) || "CLIP8".equals(subj) || "CLIP9".equals(subj)) {
			saveinfo(from, subj, txt);
			aReply.add(subj+" '"+txt+"'");
		} else if("INFOALL".equals(subj)) {
			String[] memb = fUser.list();
			StringBuilder buf = new StringBuilder();
			for(int i=0; i<memb.length; i++) {
				String em = memb[i];
				String nm = readinfo(em, "NAME");
				String tx = readinfo(em, txt);
				File fInf = new File(fUser+"/"+em+"/info/"+txt+".txt");
				buf.append(em+" "+nm+":"+tx+"\n");
			}
			aReply.add(subj+"-"+txt+"\n"+buf);
		} else if("ENAME".equals(subj)) {
			saveinfo(from, subj, txt);
			aReply.add("ได้ชื่ออาษาอังกฤษแล้วค่ะ '"+txt+"'");
		} else if("STDID".equals(subj)) {
			saveinfo(from, subj, txt);
			aReply.add("ได้รหัสนักศึกษาแล้วค่ะ '"+txt+"'");
		} else if("MOBILE".equals(subj)) {
			saveinfo(from, subj, txt);
			aReply.add("ได้หมายเลขโทรศัพท์แล้วค่ะ '"+txt+"'");
		} else if("INFO".equals(subj)) {
			String nm = readinfo(from, "NAME");
			String en = readinfo(from, "ENAME");
			String st = readinfo(from, "STDID");
			String ret = "NAME: '"+nm+"'\nENAME: "+en+"\nSTDID: '"+st+"'\n";
			aReply.add(ret);
		} else {
			System.out.println("SUBJ:"+subj);
			aReply.add("หัวเรื่องไม่ถูกต้องค่ะ");
		}
	}
}

