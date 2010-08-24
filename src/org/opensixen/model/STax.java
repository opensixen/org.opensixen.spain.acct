/**
 * 
 */
package org.opensixen.model;

import java.math.BigDecimal;
import java.util.Properties;

import org.compiere.model.MTax;

/**
 * Creamos clase STax que extiende MTax para
 * poder controlar AD_Client_ID
 * 
 * @author Eloy Gomez
 *
 */
public class STax extends MTax{

	/**
	 * Contructor estandard
	 * 
	 * @param ctx
	 * @param AD_Client_ID
	 * @param Name
	 * @param Rate
	 * @param C_TaxCategory_ID
	 * @param trxName
	 */
	public STax(Properties ctx, int AD_Client_ID, String Name, BigDecimal Rate, int C_TaxCategory_ID, String trxName) {
		super(ctx, Name, Rate, C_TaxCategory_ID, trxName);
		setAD_Client_ID(AD_Client_ID);
	}	
	

	

}
