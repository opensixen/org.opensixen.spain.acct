/**
 * 
 */
package org.opensixen.spain.acct;

import java.util.List;

import org.compiere.acct.Fact;
import org.compiere.acct.FactLine;
import org.compiere.model.FactsValidator;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MClient;
import org.compiere.model.MSequence;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.PO;
import org.opensixen.osgi.interfaces.IModelValidator;

/**
 * @author harlock
 *
 */
public class GlobalFacctModelValidator implements IModelValidator, FactsValidator {
	/** Account Tables	*/
	private final String[] acctTables = {"GL_Journal", "C_Order", "C_Invoice", "M_InOut", "M_Inventory", "M_Movement", "M_Production", "C_Payment", "C_BankStatement", "C_Cash", "M_MatchInv", "M_MatchPO", "C_ProjectIssue", "M_Requisition", "C_AllocationHdr", "PP_Order", "PP_Cost_Collector", "DD_Order", "HR_Process"};
	
	/** Sequence name for JournalNo	*/
	public static final String SEQUENCE_NAME="JournalNo";
	
	/* (non-Javadoc)
	 * @see org.compiere.model.ModelValidator#initialize(org.compiere.model.ModelValidationEngine, org.compiere.model.MClient)
	 */
	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		// Registramos el ModelValidator para todas las tablas.
		for (String table:acctTables)	{
			engine.addFactsValidate(table, this);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.compiere.model.ModelValidator#getAD_Client_ID()
	 */
	@Override
	public int getAD_Client_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.compiere.model.ModelValidator#login(int, int, int)
	 */
	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		// TODO Auto-generated method stub	
		return null;
	}

	/* (non-Javadoc)
	 * @see org.compiere.model.ModelValidator#modelChange(org.compiere.model.PO, int)
	 */
	@Override
	public String modelChange(PO po, int type) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.compiere.model.ModelValidator#docValidate(org.compiere.model.PO, int)
	 */
	@Override
	public String docValidate(PO po, int timing) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Validamos los asientos
	 * 
	 * 1: Añadimos JournalNo.
	 * 
	 */
	@Override
	public String factsValidate(MAcctSchema schema, List<Fact> facts, PO po) {

		// Recorremos todos los asientos para asignarles numero.
		for (Fact fact:facts)	{
			String sec = MSequence.getDocumentNo(po.getAD_Client_ID(), SEQUENCE_NAME, po.get_TrxName());
			if (sec == null)	{
				throw new RuntimeException("No se puede obtener el numero de asiento. Cancelando la contabilizacion.");
			}
			
			// Esperamos que sea un entero
			int journalNo = new Integer(sec).intValue();
			for (FactLine line:fact.getLines())	{
				
				line.setJournalNo(journalNo);
			}
		}
				 
		return null;
	}

}
