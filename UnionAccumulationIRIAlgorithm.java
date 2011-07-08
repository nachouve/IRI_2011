/*******************************************************************************
UnionAccumulationIRIAlgorithm.java
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
import java.util.HashMap;

import com.vividsolutions.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRecord;
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
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.Parameter;


/**
 * Algoritmo para procesar todas las capas resultado de AcumulationIRI
 * Se genera una malla de pol�gonos con los resultados
 * 
 * Observaciones: - Las unidades del SIG deben ser "METROS" - ACCFLOW debe contar celdas (autom�ticamente el algoritmo obtendr� la
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

	    m_Parameters.addNumericalValue(CELL_SIZE, "Tama�o celda", 50, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);

	    String[] sValues = {"IRI", "IRI_DMA", "IRI_FACT"};
	    m_Parameters.addSelection(IRI_TYPE, "Tipo de IRI a calcular", sValues );

	    addOutputVectorLayer("RESULT_ACC_IRI", "RESULT_ACC_IRI", OutputVectorLayer.SHAPE_TYPE_POLYGON);

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
	GeoAlgorithm geomodel = ModelAlgorithmIO.loadModelAsAlgorithm(modelsFolder + "/" +"create_graticule.model");

	AnalysisExtent ext = getEnlargedExtend(full_extent2D, cell_size);
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

	//IVectorLayer.SHAPE_TYPE_POLYGON;
	//GENERATE FIELD/COLUMN ON THE RESULT
	int num_columns = (iri_lyrs.length * 5) + 1;
	String[] fieldNames = new String[num_columns];
	Class[]  fieldTypes = new Class[num_columns];

	fieldNames[0] = "IRI";
	fieldTypes[0] = Double.class;
	char c_ascii = 'A';
	for (int i = 1, j = 0; j < iri_lyrs.length; j++){
	    fieldNames[i] = String.valueOf(c_ascii)+"_vertido";
	    fieldTypes[i++] = String.class;
	    fieldNames[i] = String.valueOf(c_ascii)+"_xp";
	    fieldTypes[i++] = Integer.class;
	    fieldNames[i] = String.valueOf(c_ascii)+"_IRI";
	    fieldTypes[i++] = Double.class;
	    fieldNames[i] = String.valueOf(c_ascii)+"_IRI_fact";
	    fieldTypes[i++] = Double.class;
	    fieldNames[i] = String.valueOf(c_ascii)+"_IRI_dma";
	    fieldTypes[i++] = Double.class;

	    c_ascii++;
	}

	IVectorLayer aux = getNewVectorLayer("RESULT_ACC_IRI", Sextante.getText("RESULT_ACC_IRI"),
		IVectorLayer.SHAPE_TYPE_POLYGON, fieldTypes, fieldNames);

	System.out.println("==============  START DETECTING IRI ACCUMUTATION POINTS ON THE GRID =========");

	HashMap<Integer, Object[]> cell_map = new HashMap<Integer, Object[]>();
	for (int i = 0; i < iri_lyrs.length; i++) {
	    IVectorLayer acc_iri = iri_lyrs[i];

	    System.out.println(" ---- "+ acc_iri.getName() +" ----");
	    Object[] values = new Object[num_columns];
	    //acc_iri.open();
	    IFeatureIterator iter = acc_iri.iterator();
	    for (;iter.hasNext();){
		IFeature iri_feat = iter.next();
		Geometry iri_geom = iri_feat.getGeometry();
		IRecord iri_record = iri_feat.getRecord();
		graticule.open();
		IFeatureIterator g_iter = graticule.iterator();
		boolean found = false;
		int cell_num = 0;
		for (;g_iter.hasNext() && !found; cell_num++){
		    IFeature cell = g_iter.next();
		    Geometry cell_geom = cell.getGeometry();
		    found = cell_geom.covers(iri_geom);
		}
		g_iter.close();
		graticule.close();
		if (found){
		    addCellWithValues(cell_map, cell_num, acc_iri, i, acc_iri.getName(), iri_feat, iri_lyrs.length);
		}
	    }
	    iter.close();
	    System.out.println("NUMBER OF CELLS WITH DATA: " +cell_map.size());
	    //acc_iri.close();
	}

	final IVectorLayer result = getNewVectorLayer("RESULT_ACC_IRI", Sextante.getText("RESULT_ACC_IRI"),
		AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON, fieldTypes, fieldNames);


	IFeatureIterator g_iter = graticule.iterator();
	for (int i = 0; g_iter.hasNext(); i++){
	    System.out.println(i +"/" +graticule.getShapesCount());
	    if (cell_map.containsKey(i)){
		System.out.println("Contains: " + i);
		IFeature cell = g_iter.next();
		Geometry geom = cell.getGeometry();
		Object[] values = cell_map.get(i);
		result.addFeature(geom, values);
		cell_map.remove(i);
	    } else {
		g_iter.next();
	    }
	}
	graticule.close();
	g_iter.close();


	return !m_Task.isCanceled();
    }

    private void addCellWithValues(HashMap<Integer, Object[]> cellMap, int cellNum, IVectorLayer accLayer, int lyrIdx, String name_lyr, IFeature iriFeat, int num_lyrs) {

	Object[] values;
	if (cellMap.containsKey(cellNum)){
	    values = cellMap.get(cellNum);
	} else {
	    values = new Object[1+(num_lyrs*5)];
	}
	int i = 1 + (lyrIdx * 5);
	if (values[i] != null){
	    System.out.println("22222222222222222222222222222222222     OH OH Parece que hay 2 puntos en la misma celda     2222222222222222222222222222222222");
	}
	values[i++] = name_lyr;
	values[i++] = getValue("Xp", accLayer, iriFeat);
	values[i++] = getValue("IRI", accLayer, iriFeat);
	values[i++] = getValue("IRI_fact", accLayer, iriFeat);
	values[i++] = getValue("IRI_dma", accLayer, iriFeat);

	cellMap.put(cellNum, values);
    }


    private Object getValue(String field_name, IVectorLayer accLayer, IFeature iriFeat) {
	IRecord record = iriFeat.getRecord();
	int idx = accLayer.getFieldIndexByName(field_name);
	if (idx == -1){
	    System.out.println(field_name + "does not exists!!!!");
	    return null;
	}
	return record.getValue(idx);
    }


    private AnalysisExtent getEnlargedExtend(Rectangle2D extent, int size){

	int diff = 2;
	AnalysisExtent ext = new AnalysisExtent();

	ext.setCellSize(size);
	ext.setXRange(enlarge(extent.getMinX(), size, -diff),
		enlarge(extent.getMaxX(), size, diff),
		false);
	ext.setYRange(enlarge(extent.getMinY(), size, -diff),
		enlarge(extent.getMaxY(), size, diff),
		true);

	return ext;
    }

    private double enlarge(double value, int div, int diff){
	double v = 0;
	int p = (int) (value/div);
	p = p + diff;
	v = div * p;
	return v;
    }

}