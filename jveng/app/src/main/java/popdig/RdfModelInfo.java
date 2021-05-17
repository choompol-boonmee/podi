package popdig;

import java.io.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import org.apache.jena.query.*;

public class RdfModelInfo {
	public String sPrefix;
	public File fDir,fRdf;
	public Model model;
	public String sBaseIRI;
	public void analyze(File f) {
		try {
			fRdf = f;
			fDir = f.getParentFile();
			sPrefix = f.getName();
			sPrefix = sPrefix.substring(0,sPrefix.indexOf("."));
			if("vp".equals(sPrefix)) {
				sBaseIRI = PopiangDigital.sBaseIRI + "voc-pred#";
			} else if("vo".equals(sPrefix)) {
				sBaseIRI = PopiangDigital.sBaseIRI + "voc-obj#";
			} else {
				sBaseIRI = PopiangDigital.sBaseIRI + sPrefix + "#";
			}
			model = ModelFactory.createDefaultModel();
			model.read(new FileInputStream(fRdf), null, "TTL");
		} catch(Exception x) {
		}
	}
}

