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
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.exceptions.WrongParameterTypeException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.modeler.ModelAlgorithmIO;
import es.unex.sextante.gui.settings.SextanteModelerSettings;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;


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

	    addOutputVectorLayer("RESULT_ACC_IRI", "RESULT_ACC_IRI", AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON);

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

	Rectangle2D extent2D = null;
	Rectangle2D full_extent2D = null;
	for (int i = 0; i < iri_lyrs.length; i++) {
	    IVectorLayer acc_iri = iri_lyrs[i];
	    acc_iri.open();
	    System.out.println(acc_iri.getName() + "  Num.Feats: " + acc_iri.getShapesCount());
	    extent2D = acc_iri.getFullExtent();

	    if (full_extent2D == null){
		full_extent2D = extent2D;
	    } else {
		full_extent2D = extent2D.createUnion(full_extent2D);
	    }
	    acc_iri.close();
	}

	final String modelsFolder = SextanteGUI.getSettingParameterValue(SextanteModelerSettings.MODELS_FOLDER);
	GeoAlgorithm geomodel = ModelAlgorithmIO.loadModelAsAlgorithm(modelsFolder + "/" +"test_create_graticule.model");

	AnalysisExtent ext = new AnalysisExtent();

	ext.setCellSize(cell_size);
	ext.setXRange(extent2D.getMinX(), extent2D.getMaxX(), false);
	ext.setYRange(extent2D.getMinY(), extent2D.getMaxY(), true);
	geomodel.setAnalysisExtent(ext);

	ParametersSet params = geomodel.getParameters();

	for (int j=0; j < params.getNumberOfParameters(); j++) {
	    Parameter p = params.getParameter(j);
	    if (p.getParameterDescription().equalsIgnoreCase("cell_size")){
		params.getParameter(j).setParameterValue(new Double(cell_size));
	    }
	}

	boolean bSucess = geomodel.execute(m_Task, m_OutputFactory);

	IVectorLayer graticule = null;
	if (bSucess) {
	    OutputObjectsSet outputs = geomodel.getOutputObjects();
	    for (int j = 0; j < outputs.getOutputLayersCount(); j++) {
		Output o = outputs.getOutput(j);
		System.out.println(j + " output name: " + o.getDescription() + "  " + o.getName() + " " + o.getTypeDescription()) ;
		if (o.getDescription().equalsIgnoreCase("graticule")){
		    graticule = (IVectorLayer) o.getOutputObject();
		    graticule.open();
		    System.out.println("graticule.feats: " +  graticule.getShapesCount());
		    m_OutputObjects.getOutput("RESULT_ACC_IRI").setOutputObject(graticule);
		    graticule.close();
		}
	    }
	} else {
	    System.out.println("NOT SUCCESS THE GEOMODEL.EXECUTE!!!!!!!!!!!");
	}

	return !m_Task.isCanceled();
    }

}
