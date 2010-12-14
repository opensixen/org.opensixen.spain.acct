 /******* BEGIN LICENSE BLOCK *****
 * Versión: GPL 2.0/CDDL 1.0/EPL 1.0
 *
 * Los contenidos de este fichero están sujetos a la Licencia
 * Pública General de GNU versión 2.0 (la "Licencia"); no podrá
 * usar este fichero, excepto bajo las condiciones que otorga dicha 
 * Licencia y siempre de acuerdo con el contenido de la presente. 
 * Una copia completa de las condiciones de de dicha licencia,
 * traducida en castellano, deberá estar incluida con el presente
 * programa.
 * 
 * Adicionalmente, puede obtener una copia de la licencia en
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Este fichero es parte del programa opensiXen.
 *
 * OpensiXen es software libre: se puede usar, redistribuir, o
 * modificar; pero siempre bajo los términos de la Licencia 
 * Pública General de GNU, tal y como es publicada por la Free 
 * Software Foundation en su versión 2.0, o a su elección, en 
 * cualquier versión posterior.
 *
 * Este programa se distribuye con la esperanza de que sea útil,
 * pero SIN GARANTÍA ALGUNA; ni siquiera la garantía implícita 
 * MERCANTIL o de APTITUD PARA UN PROPÓSITO DETERMINADO. Consulte 
 * los detalles de la Licencia Pública General GNU para obtener una
 * información más detallada. 
 *
 * TODO EL CÓDIGO PUBLICADO JUNTO CON ESTE FICHERO FORMA PARTE DEL 
 * PROYECTO OPENSIXEN, PUDIENDO O NO ESTAR GOBERNADO POR ESTE MISMO
 * TIPO DE LICENCIA O UNA VARIANTE DE LA MISMA.
 *
 * El desarrollador/es inicial/es del código es
 *  FUNDESLE (Fundación para el desarrollo del Software Libre Empresarial).
 *  Indeos Consultoria S.L. - http://www.indeos.es
 *
 * Contribuyente(s):
 *  Eloy Gómez García <eloy@opensixen.org> 
 *
 * Alternativamente, y a elección del usuario, los contenidos de este
 * fichero podrán ser usados bajo los términos de la Licencia Común del
 * Desarrollo y la Distribución (CDDL) versión 1.0 o posterior; o bajo
 * los términos de la Licencia Pública Eclipse (EPL) versión 1.0. Una 
 * copia completa de las condiciones de dichas licencias, traducida en 
 * castellano, deberán de estar incluidas con el presente programa.
 * Adicionalmente, es posible obtener una copia original de dichas 
 * licencias en su versión original en
 *  http://www.opensource.org/licenses/cddl1.php  y en  
 *  http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * Si el usuario desea el uso de SU versión modificada de este fichero 
 * sólo bajo los términos de una o más de las licencias, y no bajo los 
 * de las otra/s, puede indicar su decisión borrando las menciones a la/s
 * licencia/s sobrantes o no utilizadas por SU versión modificada.
 *
 * Si la presente licencia triple se mantiene íntegra, cualquier usuario 
 * puede utilizar este fichero bajo cualquiera de las tres licencias que 
 * lo gobiernan,  GPL 2.0/CDDL 1.0/EPL 1.0.
 *
 * ***** END LICENSE BLOCK ***** */

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
