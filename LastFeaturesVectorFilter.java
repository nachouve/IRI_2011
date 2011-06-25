package es.udc.sextante.gridAnalysis.IRI;

import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.vectorFilters.IVectorLayerFilter;


public class LastFeaturesVectorFilter
implements
IVectorLayerFilter {

    private int NUM_FEATURES = 0;
    private int NUM_LAST_FEATURES = 0;


    public LastFeaturesVectorFilter(final int num_features,final int num_last_features) {
	NUM_FEATURES = num_features;
	NUM_LAST_FEATURES = num_last_features;
    }


    public boolean accept(final IFeature feature,
	    final int iIndex) {

	final int MIN_INDEX = NUM_FEATURES - NUM_LAST_FEATURES;
	if (iIndex >= MIN_INDEX) {
	    return true;
	}
	return false;
    }

}
