/**
 * 
 */
package org.opensixen.rules.spain;

import java.math.BigDecimal;
import java.util.Properties;

import org.compiere.model.MSequence;
import org.compiere.model.MTax;
import org.compiere.model.MTaxCategory;
import org.compiere.util.Env;
import org.opensixen.model.POFactory;
import org.opensixen.model.QParam;
import org.opensixen.spain.acct.GlobalFacctModelValidator;

/**
 * @author harlock
 *
 */
public class ConfiguratorSpain {

	String result;
	Properties ctx = Env.getCtx();
	int COUNTRY_SPAIN = 106;
	
	String trxName;
	
	public String tax()	{
		// Actualizamos MTaxCategory
		MTaxCategory taxCategory = POFactory.get(MTaxCategory.class, new QParam("AD_Client_ID", Env.getAD_Client_ID(ctx)));
		
		if (taxCategory == null)	{
			return "No encuentro taxCategory";
		}
			
		taxCategory.setName("General");
		taxCategory.save(trxName);
		
		// Actualizamos el MTax creado
		MTax tax = POFactory.get(MTax.class, new QParam("AD_Client_ID", Env.getAD_Client_ID(ctx)));
		if (tax == null)	{
			return "No encunentro Tax";
		}
		tax.setName("I.V.A. 18%");
		tax.setIsDocumentLevel(false);
		tax.setRate(new BigDecimal(18));
		tax.setC_Country_ID(COUNTRY_SPAIN);
		tax.setTo_Country_ID(COUNTRY_SPAIN);
		tax.save(trxName);
		
		tax = new MTax(ctx, 0, trxName);
		tax.setC_TaxCategory_ID(taxCategory.getC_TaxCategory_ID());
		tax.setName("I.V.A. 8%");
		tax.setIsDocumentLevel(false);
		tax.setRate(new BigDecimal(8));
		tax.setC_Country_ID(COUNTRY_SPAIN);
		tax.setTo_Country_ID(COUNTRY_SPAIN);
		tax.setIsDefault(false);
		tax.setSOPOType("B");
		tax.save();
		
		tax = new MTax(ctx, 0, trxName);
		tax.setC_TaxCategory_ID(taxCategory.getC_TaxCategory_ID());
		tax.setName("I.V.A. 4%");
		tax.setIsDocumentLevel(false);
		tax.setRate(new BigDecimal(4));
		tax.setC_Country_ID(COUNTRY_SPAIN);
		tax.setTo_Country_ID(COUNTRY_SPAIN);
		tax.setIsDefault(false);
		tax.setSOPOType("B");
		tax.save();
		return "Ok";
		
	}
	
	public String sequences()	{
		// JournalNO
		MSequence secuence = new MSequence(ctx, 0, trxName);
		secuence.setName(GlobalFacctModelValidator.SEQUENCE_NAME);
		secuence.setStartNewYear(true);
		secuence.setStartNo(1);
		secuence.setCurrentNext(1);
		secuence.save();
		
		return "Ok";
	}
	
	
	public void doIt()	{
		
		// 1: Aplicar traducciones a los tipos de documentos
		
		// 2: Ajustes a los tipos de IVA
		result = tax();
		
		
	}
	
	
}
