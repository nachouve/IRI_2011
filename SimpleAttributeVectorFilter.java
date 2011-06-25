package es.udc.sextante.gridAnalysis.IRI;

import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.vectorFilters.IVectorLayerFilter;


public class SimpleAttributeVectorFilter
implements
IVectorLayerFilter {

    private int ATTRIB_IDX  = -1;
    private String OPERATION = "=";
    private double VALUE = 0;

    /**
     * 
     * @param attr_idx
     * @param operation Only "=", ">" or "<"
     * @param value
     */
    public SimpleAttributeVectorFilter(final int attr_idx, final String operation, final double value) {
	ATTRIB_IDX = attr_idx;
	OPERATION = operation.trim();
	VALUE = value;
    }


    public boolean accept(final IFeature feature,
	    final int iIndex) {

	boolean b = false;

	IRecord record = feature.getRecord();
	Object[] values = record.getValues();

	//TODO Reconocer bien los tipos de los campos
	double v = (Double)values[ATTRIB_IDX];

	if (OPERATION.equals("=")){
	    if (v == VALUE){
		b = true;
	    }
	} else if (OPERATION.equals("<")){
	    if (v < VALUE){
		b = true;
	    }
	} else if (OPERATION.equals(">")){
	    if (v > VALUE){
		b = true;
	    }
	}
	return b;
    }

}

