/**
 * 
 */
package org.opensixen.spain.acct;

import java.io.File;
import java.math.BigDecimal;
import java.util.Properties;

import org.compiere.model.MAcctSchema;
import org.compiere.model.MFactAcct;
import org.compiere.model.MSequence;
import org.compiere.model.MTax;
import org.compiere.model.MTaxCategory;
import org.compiere.util.Env;
import org.opensixen.model.POFactory;
import org.opensixen.model.QParam;
import org.opensixen.model.STax;
import org.opensixen.osgi.interfaces.IClientSetupValidator;

/**
 * @author harlock
 *
 */
public class ClientSetupValidator implements IClientSetupValidator {

	
	private Properties ctx;
	int COUNTRY_SPAIN = 106;
	private String trxName;
	
	private String result;
	private int AD_Client_ID;
	private int AD_Org_ID;
	private MAcctSchema m_as;
	
	public String tax()	{
		// Actualizamos MTaxCategory
		MTaxCategory taxCategory = POFactory.get(ctx, MTaxCategory.class, new QParam("AD_Client_ID", AD_Client_ID), trxName);
		
		if (taxCategory == null)	{
			return "No encuentro taxCategory";
		}
			
		int C_TaxCategory_ID = taxCategory.getC_TaxCategory_ID();
		taxCategory.setName("General");
		taxCategory.save(trxName);
		
		// Actualizamos el MTax creado
		MTax tax = POFactory.get(ctx,MTax.class, new QParam("AD_Client_ID", AD_Client_ID), trxName);
		if (tax == null)	{
			return "No encunentro Tax";
		}
		tax.setName("I.V.A. 18%");
		tax.setIsDocumentLevel(false);
		tax.setRate(new BigDecimal(18));
		tax.setC_Country_ID(COUNTRY_SPAIN);
		tax.setTo_Country_ID(COUNTRY_SPAIN);
		tax.save(trxName);
		
		// Creamos nuevo tax para el 8%
		STax stax = new STax(ctx, AD_Client_ID, "I.V.A. 8%", new BigDecimal(8), C_TaxCategory_ID, trxName);
		stax.setIsDocumentLevel(false);
		stax.setC_Country_ID(COUNTRY_SPAIN);
		stax.setTo_Country_ID(COUNTRY_SPAIN);
		stax.setIsDefault(false);
		stax.setSOPOType("B");
		stax.save();
		
		stax =new STax(ctx, AD_Client_ID, "I.V.A. 4%", new BigDecimal(4), C_TaxCategory_ID, trxName);
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
	 * @return
	 */
	public String sequences()	{
		// JournalNO
		MSequence sequence = new MSequence(ctx, AD_Client_ID, GlobalFacctModelValidator.SEQUENCE_NAME, 1, trxName);
		sequence.setStartNewYear(true);
		sequence.setDateColumn(MFactAcct.COLUMNNAME_DateAcct);
		sequence.setCurrentNextSys(1);
		sequence.save();
		
		return "Ok";
	}
	

	/* (non-Javadoc)
	 * @see org.opensixen.osgi.interfaces.IClientSetupValidator#doIt(int, int, java.lang.String, java.io.File)
	 */
	@Override
	public boolean doIt(Properties ctx, int AD_Client_ID, int AD_Org_ID, MAcctSchema m_as, String clientName,	File accountsFile, String trxName) {
		this.ctx = ctx;
		this.trxName = trxName;
		this.AD_Client_ID = AD_Client_ID;
		this.AD_Org_ID = AD_Org_ID;
		this.m_as = m_as;
		
		// 1: Aplicar traducciones a los tipos de documentos
		
		// 2: Ajustes a los tipos de IVA
		result = tax();	

		// 3: Secuencias
		result = sequences();
		
		// Configuramos contabilidad de productos en AS
		m_as.setIsPostServices(true);
		m_as.save();
		
		
		return true;
		
	}
	
	
}
