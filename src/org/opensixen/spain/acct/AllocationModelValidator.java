/**
 * 
 */
package org.opensixen.spain.acct;

import java.math.BigDecimal;
import java.util.List;

import org.compiere.acct.Doc;
import org.compiere.acct.DocLine;
import org.compiere.acct.DocLine_Allocation;
import org.compiere.acct.Fact;
import org.compiere.acct.FactLine;
import org.compiere.model.FactsValidator;
import org.compiere.model.MAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MClient;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MProduct;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.opensixen.osgi.interfaces.IModelValidator;

/**
 * 
 * 
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 *
 */
public class AllocationModelValidator extends GlobalFacctModelValidator implements IModelValidator, FactsValidator {

	public static final String table = "C_AllocationHdr";
	
	/* (non-Javadoc)
	 * @see org.compiere.model.ModelValidator#initialize(org.compiere.model.ModelValidationEngine, org.compiere.model.MClient)
	 */
	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		//engine.addFactsValidate(table, this);		
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

	/* (non-Javadoc)
	 * @see org.compiere.model.FactsValidator#factsValidate(org.compiere.model.MAcctSchema, java.util.List, org.compiere.model.PO)
	 */
	@Override
	public String factsValidate(MAcctSchema schema, List<Fact> facts, PO po) {
		// Do globalValidator code
		String superStr = super.factsValidate(schema, facts, po);
		if (superStr != null)	{
			return superStr;
		}
		
		for (Fact fact:facts)	{
			for (FactLine line: fact.getLines())	{
				// First line, set doc.

					Doc doc = line.getDoc();
					DocLine_Allocation docLine = (DocLine_Allocation) line.getDocLine();
					// Get invoice lines			
					MInvoice invoice = new MInvoice(doc.getCtx(), docLine.getC_Invoice_ID(), doc.getTrxName());
					doc.setC_BPartner_ID(invoice.getC_BPartner_ID());
					
					MAccount receivable = doc.getAccount(Doc.ACCTTYPE_C_Receivable, schema);
					MAccount liability = doc.getAccount(Doc.ACCTTYPE_V_Liability, schema);

				
				if (line.getAccount().equals(receivable) || line.getAccount().equals(liability))	{
					createLiabilityFact(doc, fact, schema, invoice, docLine, line.getC_Currency_ID(), line.getJournalNo());
					fact.remove(line);
				}
				
				
				
			}
		}
		
		return null;
	}
	
	
	
	/**
	 * @param line
	 * @param invoice
	 * @param c_Currency_ID
	 * @return
	 */
	private BigDecimal createLiabilityFact(Doc doc, Fact fact, MAcctSchema as, MInvoice invoice, DocLine_Allocation docLine, int C_Currency_ID, int journalNo) {
		
		BigDecimal serviceAmt = Env.ZERO;
		BigDecimal productAmt = Env.ZERO;
		
		MInvoiceLine[] lines = invoice.getLines();
		for (MInvoiceLine line:lines)	 {
			MProduct product = line.getProduct();
			if (product.isService())	{
				serviceAmt = serviceAmt.add(line.getLineTotalAmt());
			}
			else {
				productAmt = productAmt.add(line.getLineTotalAmt());				
			}
		}
		
	
		if (invoice.isSOTrx())	{
			
			if (!productAmt.equals(Env.ZERO))	{
				FactLine line = fact.createLine (docLine, doc.getAccount(Doc.ACCTTYPE_C_Receivable, as),	C_Currency_ID, productAmt, null);		//	payment currency
				line.setJournalNo(journalNo);
			}
			
			if (!serviceAmt.equals(Env.ZERO))	{
				FactLine line = fact.createLine (docLine, doc.getAccount(Doc.ACCTTYPE_C_Receivable_Services, as),	C_Currency_ID, serviceAmt, null);		//	payment currency
				line.setJournalNo(journalNo);
			}

			
		}
		
		else {
			
			if (!productAmt.equals(Env.ZERO))	{
				FactLine line = fact.createLine (docLine, doc.getAccount(Doc.ACCTTYPE_V_Liability, as),	C_Currency_ID, productAmt, null);		//	payment currency
				line.setJournalNo(journalNo);
			}
			
			if (!serviceAmt.equals(Env.ZERO))	{
				FactLine line = fact.createLine (docLine, doc.getAccount(Doc.ACCTTYPE_V_Liability_Services, as),	C_Currency_ID, serviceAmt, null);		//	payment currency
				line.setJournalNo(journalNo);
			}

		} 
		return productAmt.add(serviceAmt);		
	}
	

}
