/*******************************************************************************
AccumulationIRIAlgorithm.java
Copyright (C) Nacho Uve

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *******************************************************************************/
package es.udc.sextante.gridAnalysis.IRI;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.exceptions.WrongParameterTypeException;


/**
 * Algoritmo para procesar todas las capas resultado de AcumulationIRI
 * Se genera una malla de polígonos con los resultados
 * 
 * Observaciones: - Las unidades del SIG deben ser "METROS" - ACCFLOW debe contar celdas (automï¿½ticamente el algoritmo obtendrï¿½ la
 * cuenca en km2)
 * 
 * @author uve, jorgelf
 * 
 */
public class UnionAccumulationIRIAlgorithm
extends
GeoAlgorithm {

    public static final String ACC_IRI_LYRs = "ACC_IRI_LYRs";
    public static final String CELL_SIZE = "CELL_SIZE";
    public static final String IRI_TYPE = "IRI_TYPE";

    public IVectorLayer[] iri_lyrs = null;
    public int cell_size = 50;
    public String iri_column_name = "";

    @Override
    public void defineCharacteristics() {

	setName("Union Accumulation IRI");
	setGroup("AA_IRI Algorithms");
	setUserCanDefineAnalysisExtent(true);

	try {
	    m_Parameters.addMultipleInput(ACC_IRI_LYRs, "Accumulation IRI Layers",
		    AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT, true);

	    m_Parameters.addNumericalValue(CELL_SIZE, "Tamaño celda", 50, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);

	    String[] sValues = {"IRI", "IRI_DMA", "IRI_FACT"};
	    m_Parameters.addSelection(IRI_TYPE, "Tipo de IRI a calcular", sValues );

	} catch (RepeatedParameterNameException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }


    private void initVariables() {

	try {
	    ArrayList input = m_Parameters.getParameterValueAsArrayList(ACC_IRI_LYRs);

	    iri_lyrs = new IVectorLayer[input.size()];

	    for (int i = 0; i < input.size(); i++){
		IVectorLayer acc_iri = (IVectorLayer)input.get(i);
		iri_lyrs[i] = acc_iri;
		acc_iri.open();
		System.out.println(acc_iri.getName() + "  Num.Feats: " + acc_iri.getShapesCount());
		acc_iri.close();
	    }

	    cell_size = m_Parameters.getParameterValueAsInt(CELL_SIZE);
	    iri_column_name = m_Parameters.getParameterValueAsString(IRI_TYPE);

	} catch (WrongParameterTypeException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (WrongParameterIDException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (NullParameterValueException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (NullParameterAdditionalInfoException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    @Override
    public boolean processAlgorithm() throws GeoAlgorithmExecutionException {
	initVariables();

	Rectangle2D extent = null;
	Rectangle2D full_extent = null;
	for (int i = 0; i < iri_lyrs.length; i++) {
	    IVectorLayer acc_iri = iri_lyrs[i];
	    acc_iri.open();
	    System.out.println(acc_iri.getName() + "  Num.Feats: " + acc_iri.getShapesCount());
	    extent = acc_iri.getFullExtent();

	    if (full_extent == null){
		full_extent = extent;
	    } else {
		full_extent = extent.createUnion(full_extent);
	    }

	    acc_iri.close();
	}

	return !m_Task.isCanceled();
    }

}
