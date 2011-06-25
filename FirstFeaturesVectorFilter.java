package es.udc.sextante.gridAnalysis.IRI;

import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.vectorFilters.IVectorLayerFilter;


public class FirstFeaturesVectorFilter
         implements
            IVectorLayerFilter {

   private int NUM_FEATURES = 0;


   public FirstFeaturesVectorFilter(final int numberOfFeatures) {
      NUM_FEATURES = numberOfFeatures;
   }


   public boolean accept(final IFeature feature,
                         final int iIndex) {

      if (iIndex < NUM_FEATURES) {
         return true;
      }
      return false;
   }

}
