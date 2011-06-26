package es.udc.sextante.gridAnalysis.IRI;

import es.unex.sextante.dataObjects.IVectorLayer;

public class IRIAccumulatedLayer {

   public static int shapetype  = IVectorLayer.SHAPE_TYPE_POINT;

   public static String[] fieldNames = new String[] {
            // distancia del punto de la red de drenaje al punto de vertido en metros
            "Xp",
            // Suma de IRI_FACT e IRI_DMA
            "IRI",
            // estado ecol�gico del r�o en ese punto: A, B,C, D y E
            "EST_ECO",
            // Suma de los IRI_dma de la red de drenaje
            "IRI_dma",
            // Suma de los valores siguientes
            "IRI_fact",
            // IRI de habitantes equivalentes para cada punto
            "IRI_he",
            // IRI de dilución para cada punto
            "IRI_dil",
            // IRI de captaciones existentes para cada punto
            "IRI_c_e",
            // IRI de captaciones propuestas para cada punto
            "IRI_c_p",
            // IRI de espacios naturales para cada punto 
            "IRI_e_n",
            // IRI de zonas pisc�colas para cada punto
            "IRI_z_p",
            // IRI de playas marinas para cada punto
            "IRI_p_m",
            // IRI de playas fluviales para cada punto
            "IRI_p_f",
            // IRI de zonas sensibles para cada punto
            "IRI_z_s",
            // IRI de embalses y lagos para cada punto
            "IRI_e_l",
            // IRI de zonas de marisqueo para cada punto
            "IRI_z_m",
            // IRI de bateas para cada punto
            "IRI_b",
            // IRI de piscifactor�as para cada punto
            "IRI_p"};

   public static Class[]  fieldTypes = new Class[] {
            //Xp distancia del punto de la red de drenaje al punto de vertido en metros
            Integer.class,
            //Suma de IRI_fact e IRI_dma
            Double.class,
            //EST_ECO estado ecol�gico del r�o en ese punto: A, B,C, D y E
            String.class,
            //Suma de los IRI_dma de la red de drenaje
            Double.class,
            //IRI_fact
            Double.class,
            //IRI de habitantes equivalentes para cada punto
            Double.class,
            // IRI de dilución para cada punto
            Double.class,
            //IRI_c_e IRI de captaciones existentes para cada punto
            Double.class,
            //IRI_c_p IRI de captaciones propuestas para cada punto
            Double.class,
            //IRI_e_n IRI de espacios naturales para cada punto
            Double.class,
            //IRI_z_p IRI de zonas pisc�colas para cada punto
            Double.class,
            //IRI_p_m IRI de playas marinas para cada punto
            Double.class,
            //IRI_p_f IRI de playas fluviales para cada punto
            Double.class,
            //IRI_z_s IRI de zonas sensibles para cada punto
            Double.class,
            //IRI_e_l IRI de embalses y lagos para cada punto
            Double.class,
            //IRI_z_m IRI de zonas de marisqueo para cada punto
            Double.class,
            //IRI_b IRI de bateas para cada punto
            Double.class,
            //IRI_p IRI de piscifactor�as para cada punto
            Double.class};

}
