/**
 * 
 */
package org.opensixen.spain.acct;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.compiere.model.MAcctSchema;
import org.compiere.model.MDocType;
import org.compiere.model.MFactAcct;
import org.compiere.model.MSequence;
import org.compiere.model.MTax;
import org.compiere.model.MTaxCategory;
import org.compiere.model.X_C_DocType;
import org.compiere.util.CLogger;
import org.compiere.util.CPreparedStatement;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.opensixen.model.POFactory;
import org.opensixen.model.QParam;
import org.opensixen.model.STax;
import org.opensixen.osgi.BundleProxyClassLoader;
import org.opensixen.osgi.interfaces.IClientSetupValidator;
import org.opensixen.osgi.interfaces.ICommand;
import org.opensixen.rules.spain.model.DocTypeTrl;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Eloy Gomez
 * 
 */
public class ClientSetupValidator implements IClientSetupValidator, ICommand {

	private CLogger log = CLogger.getCLogger(getClass());
	
	private Properties ctx;
	int COUNTRY_SPAIN = 106;
	private String trxName;

	private String result;
	private int AD_Client_ID;
	private int AD_Org_ID;
	private MAcctSchema m_as;
	
	/** Traduccion de los tipos de documento	*/
	private ArrayList<DocTypeTrl> docTypeTrl;

	public String tax() {
		// Actualizamos MTaxCategory
		MTaxCategory taxCategory = POFactory.get(ctx, MTaxCategory.class,
				new QParam("AD_Client_ID", AD_Client_ID), trxName);

		if (taxCategory == null) {
			return "No encuentro taxCategory";
		}

		int C_TaxCategory_ID = taxCategory.getC_TaxCategory_ID();
		taxCategory.setName("General");
		taxCategory.save(trxName);

		// Actualizamos el MTax creado
		MTax tax = POFactory.get(ctx, MTax.class, new QParam("AD_Client_ID",
				AD_Client_ID), trxName);
		if (tax == null) {
			return "No encunentro Tax";
		}
		tax.setName("I.V.A. 18%");
		tax.setIsDocumentLevel(false);
		tax.setRate(new BigDecimal(18));
		tax.setC_Country_ID(COUNTRY_SPAIN);
		tax.setTo_Country_ID(COUNTRY_SPAIN);
		tax.save(trxName);

		// Creamos nuevo tax para el 8%
		STax stax = new STax(ctx, AD_Client_ID, "I.V.A. 8%", new BigDecimal(8),
				C_TaxCategory_ID, trxName);
		stax.setIsDocumentLevel(false);
		stax.setC_Country_ID(COUNTRY_SPAIN);
		stax.setTo_Country_ID(COUNTRY_SPAIN);
		stax.setIsDefault(false);
		stax.setSOPOType("B");
		stax.save();

		stax = new STax(ctx, AD_Client_ID, "I.V.A. 4%", new BigDecimal(4),
				C_TaxCategory_ID, trxName);
		stax.setIsDocumentLevel(false);
		stax.setC_Country_ID(COUNTRY_SPAIN);
		stax.setTo_Country_ID(COUNTRY_SPAIN);
		stax.setIsDefault(false);
		stax.setSOPOType("B");
		stax.save();

		return "Ok";

	}

	/**
	 * Generamos secuencia para journalNo
	 * 
	 * @return
	 */
	public String sequences() {
		// JournalNO
		MSequence sequence = new MSequence(ctx, AD_Client_ID,
				GlobalFacctModelValidator.SEQUENCE_NAME, 1, trxName);
		sequence.setStartNewYear(true);
		sequence.setDateColumn(MFactAcct.COLUMNNAME_DateAcct);
		sequence.setCurrentNext(1);
		sequence.save();

		return "Ok";
	}

	
	public void translateDocType() {
		//String AD_Language = Env.getAD_Language(Env.getCtx());
		String AD_Language = "es_ES";
		docTypeTrl = getTrl(AD_Language);
		
		String sql = "update c_doctype_trl set name=?, printname=? where c_doctype_id=? and AD_Language=?";
		
		List<MDocType> doctypes= POFactory.getList(ctx, MDocType.class, new QParam(MDocType.COLUMNNAME_AD_Client_ID, AD_Client_ID), trxName);
		for (MDocType docType : doctypes)	{
			DocTypeTrl trl = getDocTypeTrl(docType.getName());
			if (trl == null)	{
				log.warning("No se encuentra traduccion para el doctype: " + docType.getName());
				continue;
			}
			
			log.info("Traduciendo el doctype: " + docType.getName());
			try {
				CPreparedStatement psmt = DB.prepareStatement(sql, trxName);
				psmt.setString(1, trl.getName_trl());
				psmt.setString(2, trl.getPrint_trl());
				psmt.setInt(3, docType.getC_DocType_ID());
				psmt.setString(4, AD_Language);				
				psmt.executeUpdate();
				psmt.close();
				psmt = null;
			}
			catch (SQLException e)	{
				e.printStackTrace();
			}
		}
		
			
	}
	
	
	/**
	 * Return the DocTypeTrl with this name
	 * @param name
	 * @return
	 */
	private DocTypeTrl getDocTypeTrl(String name)	{
		for (DocTypeTrl trl: docTypeTrl)	{
			if (name.equals(trl.getName()))	{
				return trl;
			}
		}	
		return null;
	}
	
		
	/**
	 * Parse data/doctype_trl.xml and return a 
	 * list of DocTypeTrl 
	 * @param AD_Language
	 * @return
	 */
	public ArrayList<DocTypeTrl> getTrl(String AD_Language) {
		Bundle bundle = Activator.getContext().getBundle();
		ArrayList<DocTypeTrl> docTypeTrlList = new ArrayList<DocTypeTrl>();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream in = bundle.getResource("data/doctype_trl.xml").openStream();
			Document doc = db.parse(in);
			doc.getDocumentElement().normalize();
			System.out.println("Root element "
					+ doc.getDocumentElement().getNodeName());

			NodeList doctrl = doc.getElementsByTagName("doctypeTrl");
			for (int i = 0; i < doctrl.getLength(); i++) {
				Node node = doctrl.item(i);
				String nodeLang = node.getAttributes()
						.getNamedItem("AD_Language").getNodeValue();
				if (AD_Language.equals(nodeLang)) {
					NodeList trlList = node.getChildNodes();

					for (int x = 0; x < trlList.getLength(); x++) {
						Node trl = trlList.item(x);
						String name = trl.getNodeName();
						if (!"doctype".equals(name)) {
							continue;
						}
						NamedNodeMap nodeMap = trl.getAttributes();

						DocTypeTrl docTypeTrl = new DocTypeTrl();
						docTypeTrl.setName(nodeMap.getNamedItem("name")
								.getNodeValue());
						docTypeTrl.setName_trl(nodeMap.getNamedItem("name_trl")
								.getNodeValue());
						docTypeTrl.setPrint(nodeMap.getNamedItem("print")
								.getNodeValue());
						docTypeTrl.setPrint_trl(nodeMap.getNamedItem(
								"print_trl").getNodeValue());

						docTypeTrlList.add(docTypeTrl);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return docTypeTrlList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opensixen.osgi.interfaces.IClientSetupValidator#doIt(int, int,
	 * java.lang.String, java.io.File)
	 */
	@Override
	public boolean doIt(Properties ctx, int AD_Client_ID, int AD_Org_ID,
			MAcctSchema m_as, String clientName, File accountsFile,
			String trxName) {
		this.ctx = ctx;
		this.trxName = trxName;
		this.AD_Client_ID = AD_Client_ID;
		this.AD_Org_ID = AD_Org_ID;
		this.m_as = m_as;

		// 1: Aplicar traducciones a los tipos de documentos
		translateDocType();

		// 2: Ajustes a los tipos de IVA
		result = tax();

		// 3: Secuencias
		result = sequences();

		// Configuramos contabilidad de productos en AS
		m_as.setIsPostServices(true);
		m_as.save();

		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opensixen.osgi.interfaces.ICommand#prepare()
	 */
	@Override
	public void prepare() {
		ctx = Env.getCtx();
		AD_Client_ID = Env.getAD_Client_ID(ctx);
		//AD_Org_ID = Env.getAD_Org_ID(ctx);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opensixen.osgi.interfaces.ICommand#doIt()
	 */
	@Override
	public String doIt() throws Exception {
		translateDocType();
		return "";
	}

}
