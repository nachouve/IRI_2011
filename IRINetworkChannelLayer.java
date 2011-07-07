package es.udc.sextante.gridAnalysis.IRI;

import es.unex.sextante.dataObjects.IVectorLayer;

public class IRINetworkChannelLayer {

    public static int      shapetype  = IVectorLayer.SHAPE_TYPE_POINT;

    public static String[] fieldNames = new String[] {
	// distancia del punto de la red de drenaje al punto de vertido en metros
	"Xp",
	// IRI de captaciones existentes para cada punto
	"IRI_c_e",
	// IRI de captaciones propuestas para cada punto
	"IRI_c_p",
	// IRI de espacios naturales para cada punto
	"IRI_e_n",
	// IRI de zonas piscícolas para cada punto
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
	"IRI_b",
	// IRI de piscifactorías para cada punto
	"IRI_z_m",
	// IRI de bateas para cada punto
	"IRI_p",
	// estado ecológico del río en ese punto: A, B,C, D y E
	"EST_ECO",
	// IRIDMA para cada punto
    "IRI_DMA"               };

    public static Class[]  fieldTypes = new Class[] {
	//Xp distancia del punto de la red de drenaje al punto de vertido en metros
	Integer.class,
	//IRI_c_e IRI de captaciones existentes para cada punto
	Double.class,
	//IRI_c_p IRI de captaciones propuestas para cada punto
	Double.class,
	//IRI_e_n IRI de espacios naturales para cada punto
	Double.class,
	//IRI_z_p IRI de zonas piscícolas para cada punto
	Double.class,
	//IRI_p_m IRI de playas marinas para cada punto
	Double.class,
	//IRI_p_f IRI de playas fluviales para cada punto
	Double.class,
	//IRI_z_s IRI de zonas sensibles para cada punto
	Double.class,
	//IRI_e_l IRI de embalses y lagos para cada punto
	Double.class,
	//IRI_b IRI de bateas para cada punto
	Double.class,
	//IRI_z_m IRI de zonas de marisqueo para cada punto
	Double.class,
	//IRI_p IRI de piscifactorías para cada punto
	Double.class,
	//EST_ECO estado ecológico del río en ese punto: A, B,C, D y E
	String.class,
	//IRI_DMA IRIDMA para cada punto
	Double.class            };

}
