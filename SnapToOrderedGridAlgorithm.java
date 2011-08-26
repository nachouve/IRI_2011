package es.udc.sextante.gridAnalysis.IRI;

import java.awt.geom.Point2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.rasterWrappers.GridCell;

/**
 * La idea es que una red de drenaje de puntos equiespaciados se ajuste a un grid
 * que representa un rio cuyas celdas aumentan de valor en el sentido del agua
 * 
 * @author uve
 *
 */

public class SnapToOrderedGridAlgorithm
extends
GeoAlgorithm {

    public static final String POINTS	   = "POINTS";
    public static final String GRID        = "GRID";
    public static final String INTERPOLATE = "INTERPOLATE";
    public static final String RESULT      = "RESULT";

    private IVectorLayer       m_Layer;
    private IRasterLayer       m_Grid;

    private final static int   m_iOffsetX[]   = { 0, 1, 1, 1, 0, -1, -1, -1 };
    private final static int   m_iOffsetY[]   = { 1, 1, 0, -1, -1, -1, 0, 1 };

    @Override
    public void defineCharacteristics() {

	setName(Sextante.getText("Snap Points To Ordered Grid"));
	setGroup(Sextante.getText("AA_IRI Algorithms"));
	setUserCanDefineAnalysisExtent(true);

	try {
	    m_Parameters.addInputVectorLayer(POINTS, "Network_Points",
		    AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
	    m_Parameters.addInputRasterLayer(GRID, "River_grid", true);

	    addOutputVectorLayer(RESULT, Sextante.getText("Snaped_net"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
	}
	catch (final RepeatedParameterNameException e) {
	    Sextante.addErrorToLog(e);
	}

    }


    @Override
    public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

	int i, j;
	int iTotalProgress;
	int iProgress = 0;
	int iLayer;
	int iShapeCount;

	IRasterLayer grid;

	m_Layer = m_Parameters.getParameterValueAsVectorLayer(POINTS);
	m_Grid = m_Parameters.getParameterValueAsRasterLayer(GRID);
	//m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));

	m_Layer.open();
	m_Grid.open();
	m_Grid.setFullExtent();
	AnalysisExtent ext = m_Grid.getLayerGridExtent();

	iShapeCount = m_Layer.getShapesCount();
	iTotalProgress = iShapeCount;

	IVectorLayer result = getNewVectorLayer(RESULT, Sextante.getText("Snap_to_graticule"),
		IVectorLayer.SHAPE_TYPE_POINT, m_Layer.getFieldTypes(), m_Layer.getFieldNames());

	setProgressText(Sextante.getText("Creating_snaping_layer"));

	GeometryFactory gf = new GeometryFactory();

	GridCell cell = null;
	boolean found = false;
	IFeatureIterator iter = m_Layer.iterator();
	while (iter.hasNext() && setProgress(iProgress, iTotalProgress)) {
	    IFeature feature = iter.next();
	    Geometry geom = feature.getGeometry();
	    Object[] attributes = feature.getRecord().getValues();
	    final Coordinate[] coords = geom.getCoordinates();
	    double dValue = m_Grid.getNoDataValue();
	    if (!found){
		dValue = m_Grid.getValueAt(coords[0].x, coords[0].y);
		if (!m_Grid.isNoDataValue(dValue)){
		    found = true;
		}
	    }
	    if (found){
		if (cell == null) {
		    //First point found
		    cell = ext.getGridCoordsFromWorldCoords(coords[0].x, coords[0].y);
		    cell.setValue(dValue);
		} else {
		    // Find next less higher neighbor cell values
		    cell = findHigherCell(cell);
		}
		Point2D point = ext.getWorldCoordsFromGridCoords(cell);
		geom = gf.createPoint(new Coordinate(point.getX(), point.getY()));
	    }
	    result.addFeature(geom, attributes);
	    iProgress++;

	}
	iter.close();
	m_Layer.close();
	m_Grid.close();

	if (m_Task.isCanceled()) {
	    return false;
	}

	//    final Object[][] values = new Object[m_Grids.size()][iShapeCount];
	//    final String[] sNames = new String[m_Grids.size()];
	//    final Class[] types = new Class[m_Grids.size()];
	//    for (i = 0; i < m_Grids.size(); i++) {
	//	final RasterLayerAndBand rab = (RasterLayerAndBand) m_Grids.get(i);
	//	grid = rab.getRasterLayer();
	//	sNames[i] = grid.getName();
	//	types[i] = Double.class;
	//	for (j = 0; j < iShapeCount; j++) {
	//	    values[i][j] = new Double(dValues[i][j]);
	//	}
	//    }
	//    final IOutputChannel channel = getOutputChannel(RESULT);
	//    final OutputVectorLayer out = new OutputVectorLayer();
	//    out.setName(RESULT);
	//    out.setOutputChannel(channel);
	//    out.setDescription(Sextante.getText("Points"));
	//    out.setOutputObject(ShapesTools.addFields(m_OutputFactory, m_Layer, channel, sNames, values, types));
	//    addOutputObject(out);

	return !m_Task.isCanceled();

    }


    private GridCell findHigherCell(GridCell cell) {

	int x = cell.getX();
	int y = cell.getY();
	double value = cell.getValue();

	double[] z_posib = new double[8];
	int min_idx = -1;
	double higher = Double.MIN_VALUE;
	for (int i = 0; i < 8; i++){
	    z_posib[i] = m_Grid.getCellValueAsDouble(x + m_iOffsetX[i], y + m_iOffsetY[i]);
	    if (z_posib[i] >= value){
		if (z_posib[i] > higher){
		    higher = z_posib[i];
		    min_idx = i;
		}
	    }
	}

	GridCell c = null;
	if (min_idx != -1){
	    c = new GridCell(x + m_iOffsetX[min_idx], y + m_iOffsetY[min_idx], higher);
	}
	return c;
    }
}