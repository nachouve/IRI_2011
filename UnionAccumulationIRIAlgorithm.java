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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.vividsolutions.jts.geom.Coordinate;
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
import es.unex.sextante.exceptions.IteratorException;
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
 * Se genera una malla de poligonos con los resultados. En esta malla se recalcula
 *    la dilucion dado que no es una operacion lineal, es decir, se suma los habitantes
 *    equivalentes totales en ese punto (recordar que ese numero HAB_EQU va descendiendo y se refleja en IRI_HE)
 *    y se calcula el cuenca_km2 y el IRI_DIL total.
 * 
 * 
 * 
 * Observaciones: - Las unidades del SIG deben ser "METROS" - ACCFLOW debe contar celdas (automaticamente el algoritmo obtendria la
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
    public static final String WEIGTH_HE = "WEIGTH_HE";
    public static final String WEIGTH_DIL = "WEIGTH_DIL";

    public IVectorLayer[] iri_lyrs = null;
    public int cell_size = 50;

    private int he_weight;
    private int dil_weight;

    private int num_first_cols;
    private int num_columns;

    private String[] fieldNames;
    private Class[]  fieldTypes;

    BufferedWriter outCSV = null;

    @Override
    public void defineCharacteristics() {

	setName("Union Accumulation IRI");
	setGroup("AA_IRI Algorithms");
	setUserCanDefineAnalysisExtent(false);

	try {
	    m_Parameters.addMultipleInput(ACC_IRI_LYRs, "Accumulation IRI Layers",
		    AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT, true);

	    m_Parameters.addNumericalValue(CELL_SIZE, "Tama�o celda", 83, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);

	    m_Parameters.addNumericalValue(WEIGTH_HE, "PESO_HE", 25, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
	    m_Parameters.addNumericalValue(WEIGTH_DIL, "PESO_DIL", 10, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);

	    addOutputVectorLayer("GRID_UNION", "GRID_UNION", OutputVectorLayer.SHAPE_TYPE_POLYGON);
	    addOutputVectorLayer("AUX_RESULT_ACC_IRI", "AUX_RESULT_ACC_IRI", OutputVectorLayer.SHAPE_TYPE_POLYGON);
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
	    he_weight = m_Parameters.getParameterValueAsInt(WEIGTH_HE);
	    dil_weight = m_Parameters.getParameterValueAsInt(WEIGTH_DIL);


	} catch (WrongParameterTypeException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (WrongParameterIDException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (NullParameterValueException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }


    private void printAllLayers(){
	System.out.println("\n     " );
	for (int i = 0; i < iri_lyrs.length; i++) {
	    String name_lyr = iri_lyrs[i].getName();
	    iri_lyrs[i].open();
	    System.out.println("\n    [["+ i+ "]] "+ name_lyr + ": " + iri_lyrs[i].getShapesCount());
	    IFeatureIterator iter = iri_lyrs[i].iterator();
	    for (;iter.hasNext();){
		IFeature iri_feat;
		try {
		    iri_feat = iter.next();
		    IRecord r = iri_feat.getRecord();
		    System.out.println(iri_lyrs[i].getFieldName(0) + ": " + r.getValue(0));
		    System.out.println(iri_lyrs[i].getFieldName(1) + ": " + r.getValue(1));
		    System.out.println(iri_lyrs[i].getFieldName(2) + ": " + r.getValue(2));
		} catch (IteratorException e) {
		    e.printStackTrace();
		}
	    }
	    iri_lyrs[i].close();
	}
	System.out.println("\n     " );
    }

    @Override
    public boolean processAlgorithm() throws GeoAlgorithmExecutionException {
	initVariables();

	Rectangle2D extent2D = null;
	Rectangle2D full_extent2D = null;
	for (int i = 0; i < iri_lyrs.length; i++) {
	    IVectorLayer acc_iri = iri_lyrs[i];
	    acc_iri.open();
	    int num_feats = acc_iri.getShapesCount();
	    System.out.println(acc_iri.getName() + " -- Num.Feats: " + num_feats);
	    if (num_feats == 0){
		System.out.println(">>>>>>>>>>> ERROR: " + acc_iri.getName() + " -- Num.Feats: " + num_feats);
		continue;
	    }
	    extent2D = acc_iri.getFullExtent();

	    if (full_extent2D == null){
		full_extent2D = extent2D;
	    } else {
		full_extent2D = extent2D.createUnion(full_extent2D);
	    }
	    System.out.println("EXTENSION: " + full_extent2D.getHeight() +"  "+full_extent2D.getWidth() + ": " + full_extent2D.getMinX());
	    acc_iri.close();
	}


	AnalysisExtent ext = getEnlargedExtend(full_extent2D, cell_size);

	System.out.println("EXTENSION2: " + ext.getHeight() +"  "+ext.getWidth());
	ext.enlargeOneCell();
	ext.enlargeOneCell();
	System.out.println("EXTENSION2: " + ext.getHeight() +"  "+ext.getWidth());

	//System.out.println("------ ANTES DEL SNAP");
	//printAllLayers();

	System.out.println(ext.getHeight() +"  "+ext.getWidth());

	final String modelsFolder = SextanteGUI.getSettingParameterValue(SextanteModelerSettings.MODELS_FOLDER);
	GeoAlgorithm geomodel = ModelAlgorithmIO.loadModelAsAlgorithm(modelsFolder + "/" +"create_graticule.model");

	geomodel.setAnalysisExtent(ext);

	ParametersSet params = geomodel.getParameters();

	for (int j=0; j < params.getNumberOfParameters(); j++) {
	    Parameter p = params.getParameter(j);
	    if (p.getParameterDescription().equalsIgnoreCase("cell_size")){
		params.getParameter(j).setParameterValue(new Double(cell_size));
	    }
	}

	Class[] g_ft = {Integer.class};
	String[] g_fn = {"ID"};

	IVectorLayer aux1 = getNewVectorLayer("GRID_UNION", Sextante.getText("GRID_UNION"),
		IVectorLayer.SHAPE_TYPE_POLYGON, g_ft, g_fn);

	boolean bSucess = geomodel.execute(m_Task, m_OutputFactory);

	IVectorLayer graticule = null;
	if (bSucess) {
	    OutputObjectsSet outputs = geomodel.getOutputObjects();
	    for (int j = 0; j < outputs.getOutputLayersCount(); j++) {
		Output o = outputs.getOutput(j);
		//System.out.println(j + " output name: " + o.getDescription() + "  " + o.getName() + " " + o.getTypeDescription()) ;
		if (o.getDescription().equalsIgnoreCase("graticule")){
		    graticule = (IVectorLayer) o.getOutputObject();
		    graticule.open();
		    //System.out.println("graticule.feats: " +  graticule.getShapesCount());
		    graticule.close();

		    IFeatureIterator it = graticule.iterator();
		    for (int k=0;it.hasNext();k++){
			aux1.addFeature(it.next().getGeometry(), new Object[]{k});
		    }
		    it.close();
		    m_OutputObjects.getOutput("GRID_UNION").setOutputObject(graticule);
		}
	    }
	} else {
	    System.out.println("NOT SUCCESS THE GEOMODEL.EXECUTE!!!!!!!!!!!");
	}

	try {
	    aux1.postProcess();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}


	//IVectorLayer.SHAPE_TYPE_POLYGON;
	//GENERATE FIELD/COLUMN ON THE RESULT

	num_first_cols = IRIAccumulatedLayer.fieldNames.length-3;

	num_columns = (iri_lyrs.length * (num_first_cols+2)) + num_first_cols;
	fieldNames = new String[num_columns];
	fieldTypes = new Class[num_columns];

	int num = 0;
	for (int i = 0; i < IRIAccumulatedLayer.fieldNames.length; i++){
	    String name = IRIAccumulatedLayer.fieldNames[i];
	    Class type = IRIAccumulatedLayer.fieldTypes[i];
	    if (isResultField(name) && !name.equalsIgnoreCase("name_vert") && !name.equalsIgnoreCase("Xp")) {
		fieldNames[num] = name;
		fieldTypes[num] = type;
		num++;
	    }
	}
	num_first_cols = num;

	char c_ascii1 = 'A';
	char c_ascii2 = 'A';
	char c_ascii3 = 'A';
	for (int i = num_first_cols, j = 0; j < iri_lyrs.length; j++){
	    //String CODE = String.valueOf(c_ascii1)+String.valueOf(c_ascii2)+String.valueOf(c_ascii3);
	    String CODE = String.valueOf(c_ascii2)+String.valueOf(c_ascii3);
	    for (int k = 0; k < IRIAccumulatedLayer.fieldNames.length; k++){
		String name = IRIAccumulatedLayer.fieldNames[k];
		Class type = IRIAccumulatedLayer.fieldTypes[k];
		if (isResultField(name)) {
		    //name = name.replace("IRI_", "IRI");
		    fieldNames[num] = CODE+"_"+name;
		    fieldTypes[num] = type;
		    num++;
		}
	    }

	    c_ascii3++;
	    if ('Z'<(c_ascii3)){
		c_ascii2++;
		c_ascii3 = 'A';
		if ('Z'<(c_ascii2)){
		    c_ascii1++;
		    c_ascii2 = 'A';
		}
	    }
	}

	//	IVectorLayer aux = getNewVectorLayer("RESULT_ACC_IRI", Sextante.getText("RESULT_ACC_IRI"),
	//		IVectorLayer.SHAPE_TYPE_POLYGON, fieldTypes, fieldNames);

	System.out.println("==============  START DETECTING IRI ACCUMUTATION POINTS ON THE GRID =========");

	HashMap<Integer, Object[]> cell_map = new HashMap<Integer, Object[]>();
	setProgressText("Union of accIRI");
	for (int i = 0; i < iri_lyrs.length && setProgress(i, iri_lyrs.length); i++) {
	    IVectorLayer acc_iri = iri_lyrs[i];

	    System.out.println(" ---- "+ acc_iri.getName() +" ----");

	    IFeatureIterator iter = acc_iri.iterator();
	    for (;iter.hasNext();){
		IFeature iri_feat = iter.next();
		Geometry iri_geom = iri_feat.getGeometry();

		aux1.open();
		IFeatureIterator g_iter = aux1.iterator();
		boolean found = false;
		int cell_num = 0;
		for (;g_iter.hasNext() && !found; cell_num++){
		    IFeature cell = g_iter.next();
		    Geometry cell_geom = cell.getGeometry();
		    found = cell_geom.intersects(iri_geom);
		}
		g_iter.close();
		aux1.close();
		if (found){
		    System.out.println("cell_num: " + cell_num+"  addCellWithValues: " + acc_iri.getName() + " -- " + getValue("Xp", acc_iri, iri_feat));
		    addCellWithValues(cell_map, cell_num, acc_iri, i, acc_iri.getName(), iri_feat, iri_lyrs.length);
		}
	    }
	    iter.close();
	    System.out.println("NUMBER OF CELLS WITH DATA: " +cell_map.size());
	}


	//////////////////////////////// AUX VECTOR LAYER BEFORE SUMARIZE

	final IVectorLayer aux_result = getNewVectorLayer("AUX_RESULT_ACC_IRI", Sextante.getText("AUX_RESULT_ACC_IRI"),
		AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON, fieldTypes, fieldNames);

	openCSV(fieldNames, "/tmp/aux_outCSV.csv");

	HashMap aux_cell_map = (HashMap) cell_map.clone();
	IFeatureIterator g_iter = graticule.iterator();
	for (int i = 0; g_iter.hasNext(); i++){
	    //System.out.println(i +"/" +graticule.getShapesCount());
	    if (aux_cell_map.containsKey(i)){
		//System.out.println("Contains: " + i +"/" +graticule.getShapesCount());
		IFeature cell = g_iter.next();
		Geometry geom = cell.getGeometry();
		Object[] values = (Object[]) aux_cell_map.get(i);
		//values = sumarizeIRI(values);
		for (int j = 0; j < values.length; j++){
		    //System.out.println(j + ": " + fieldTypes[j] + "  "+ fieldNames[j] + "\t" + values[j]);
		    try {
			if (fieldTypes[j].getClass().equals(Double.class)) {
			    double f = (Double)values[j];
			}
			if (fieldTypes[j].getClass().equals(String.class)) {
			    String f = (String)values[j];
			}
			if (fieldTypes[j].getClass().equals(Integer.class)) {
			    int f = (Integer)values[j];
			}
		    } catch (Exception e) {
			System.out.println("ERROR!!!!!!!!!!");
		    }
		}
		aux_result.addFeature(geom, values);
		writeCSV(geom, values);
		aux_cell_map.remove(i);
	    } else {
		g_iter.next();
	    }
	}
	//graticule.close();
	g_iter.close();
	closeCSV();
	////////////////////////////// END AUX_VECTOR


	final IVectorLayer result = getNewVectorLayer("RESULT_ACC_IRI", Sextante.getText("RESULT_ACC_IRI"),
		AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON, fieldTypes, fieldNames);

	openCSV(fieldNames, "/tmp/outCSV.csv");

	g_iter = graticule.iterator();
	for (int i = 0; g_iter.hasNext(); i++){
	    //System.out.println(i +"/" +graticule.getShapesCount());
	    if (cell_map.containsKey(i)){
		//System.out.println("Contains: " + i +"/" +graticule.getShapesCount());
		IFeature cell = g_iter.next();
		Geometry geom = cell.getGeometry();
		Object[] values = cell_map.get(i);
		values = sumarizeIRI(values);
		for (int j = 0; j < values.length; j++){
		    //System.out.println(j + ": " + fieldTypes[j] + "  "+ fieldNames[j] + "\t" + values[j]);
		    try {
			if (fieldTypes[j].getClass().equals(Double.class)) {
			    double f = (Double)values[j];
			}
			if (fieldTypes[j].getClass().equals(String.class)) {
			    String f = (String)values[j];
			}
			if (fieldTypes[j].getClass().equals(Integer.class)) {
			    int f = (Integer)values[j];
			}
		    } catch (Exception e) {
			System.out.println("ERROR!!!!!!!!!!");
		    }
		}
		result.addFeature(geom, values);
		cell_map.remove(i);
		writeCSV(geom, values);
	    } else {
		g_iter.next();
	    }
	}
	//graticule.close();
	g_iter.close();
	closeCSV();

	return !m_Task.isCanceled();
    }

    private boolean isResultField(String name) {

	if (name.equalsIgnoreCase("EST_ECO")){
	    return false;
	}
	//	if (name.equalsIgnoreCase("cuenca_km2")){
	//	    return false;
	//	}
	return true;
    }


    private Object[] sumarizeIRI(Object[] values) {

	double max_watershed = Double.MIN_VALUE;

	for (int i = 0; i < iri_lyrs.length; i++){
	    // +1: because "Xp" column
	    int j = num_first_cols + (i * (num_first_cols + 2)) + 1;
	    // -1: Not until the end because "name_vert" column
	    int stop_num = j + num_first_cols;
	    int num = 0;

	    for (;j< stop_num; j++,num++) {
		//System.out.println(j + ": " + values[j]);
		//System.out.printlA 2 pen(j + " type: " + fieldTypes[j]);
		if ((Double)values[j] != -1){
		    values[num] = (Double)values[num] + (Double)values[j];
		    //Get max cuenca to recalculate IRI_DIL
		    if (fieldNames[j].contains("cuenca")){
			//System.out.println(fieldNames[j]+ "["+j+"]: " + values[j]);
			double cuenca = (Double)values[j];
			if (cuenca > max_watershed){
			    max_watershed = cuenca;
			}
		    }
		}
	    }
	    values[--num] = max_watershed;

	    // RECALCULATE IRI_DIL
	    double iri_dil;
	    if (fieldNames[3].equalsIgnoreCase("IRI_HE")){

		final double perc_fact = 60;
		double iri_he = (Double)values[3];
		double he = (100000 * iri_he)/(he_weight * 60);
		if (fieldNames[4].equalsIgnoreCase("IRI_DIL")){
		    double concentracion_max = getMaxConcentration(max_watershed, he);
		    if (concentracion_max >= 300) {
			values[4] = 0.0;
		    } else {
			values[4] = (dil_weight * perc_fact / 100) * (1 - (concentracion_max / 300));
		    }
		} else {
		    System.out.println("\n\nERROR!!!!: NO DETECTA 'IRI_DIL' en la columna esperada!!!!! \n\n");
		}
	    } else {
		System.out.println("\n\nERROR!!!!: NO DETECTA 'IRI_HE' en la columna esperada!!!!! \n\n");
	    }
	}
	return values;
    }


    private void addCellWithValues(HashMap<Integer, Object[]> cellMap, int cellNum, IVectorLayer accLayer, int lyrIdx, String name_lyr, IFeature iriFeat, int num_lyrs) {

	Object[] values;
	if (cellMap.containsKey(cellNum)){
	    values = cellMap.get(cellNum);
	} else {
	    values = new Object[num_columns];
	    int i = 0;
	    for (; i < num_first_cols;){
		values[i++] = 0.0;
	    }


	    for (; i < values.length;i++){
		if (fieldTypes[i] == Double.class) {
		    values[i] = -1.0;
		} else if (fieldTypes[i] == Integer.class) {
		    values[i] = -1;
		} else if (fieldTypes[i] == String.class) {
		    values[i] = "--";
		}
	    }
	}
	int i = num_first_cols + (lyrIdx * (num_first_cols + 2));
	int name_idx = num_first_cols + ((lyrIdx + 1) * (num_first_cols + 2)) - 1;
	//System.out.println(i + " Xp??: " + values[i]);
	//System.out.println(name_idx + " NAME_LAYER?: " + values[name_idx]);
	if (((String)values[name_idx]).equalsIgnoreCase(name_lyr)){
	    System.out.println("OH OH Parece que hay 2 puntos en la misma celda --- 2222222222222222222222222222222222");
	}

	int stop_num = i + num_first_cols + 2;
	for (; i < stop_num; i++){
	    String col_name = fieldNames[i].substring(3);
	    values[i] = getValue(col_name, accLayer, iriFeat);
	    //System.out.println(i + ": "+ col_name + "  --  " + values[i]);
	}

	cellMap.put(cellNum, values);
    }


    private Object getValue(String field_name, IVectorLayer accLayer, IFeature iriFeat) {
	IRecord record = iriFeat.getRecord();
	int idx = accLayer.getFieldIndexByName(field_name);
	if (idx == -1){
	    System.out.println(field_name + " does not exists!!!!");
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

    private double getMaxConcentration(final double watershed_km2,
	    final double he) {

	final double conc_mez = 6;
	final double conc_rio = 3;

	final double _7Q10 = (3.1 * Math.pow(watershed_km2, 0.8736));
	final double cmax = conc_mez + (432 * ((_7Q10 * (conc_mez - conc_rio)) / he));
	return cmax;
    }

    private void openCSV(String[] header, String filepath){
	try{
	    // Create file
	    FileWriter fstream = new FileWriter(filepath);
	    outCSV = new BufferedWriter(fstream);
	    outCSV.write("X;Y;");
	    for (int i = 0; i < (header.length-1); i++) {
		outCSV.write(header[i]+";");
	    }
	    outCSV.write(header[header.length-1]+"\n");
	}catch (Exception e){//Catch exception if any
	    System.err.println("Error: " + e.getMessage());
	}
    }

    private void writeCSV(Geometry geom, Object[] values){
	try {
	    Coordinate coord = geom.getCentroid().getCoordinate();
	    outCSV.write(coord.x+";"+coord.y+";");
	    for (int i = 0; i < (values.length-1); i++) {
		outCSV.write(values[i].toString()+";");
	    }
	    outCSV.write(values[values.length-1].toString()+'\n');
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void closeCSV(){
	try {
	    //Close the output stream
	    outCSV.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }


}