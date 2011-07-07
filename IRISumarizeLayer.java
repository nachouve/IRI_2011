package es.udc.sextante.gridAnalysis.IRI;

import es.unex.sextante.dataObjects.IVectorLayer;

public class IRISumarizeLayer {

    public static int      shapetype  = IVectorLayer.SHAPE_TYPE_POINT;

    public static String[] fieldNames = new String[] {
	// suma de IRI_fact e IRI_dma
	"IRI",
	// suma de los IRI_dma de Red de drenaje
	"IRI_dma",
	// suma de IRI todos los factores ambientales
	"IRI_fact",
	// IRI HE
	"IRI_he",
	// IRI dilucion
	"IRI_dil",
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
	// IRI de bateas para cada punto
	"IRI_b",
	// IRI de zonas de marisqueo para cada punto
	"IRI_z_m",
	// IRI de piscifactorías para cada punto
	"IRI_p",
	// cuenca_km2
    "cuenca_km2"            };

    public static Class[]  fieldTypes = new Class[] {
	// suma de IRI_fact e IRI_dma
	Double.class,
	//"IRI_dma",
	Double.class,
	//"IRI_fact",
	Double.class,
	//"IRI_HE",
	Double.class,
	//"IRI_dil",
	Double.class,
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
	//IRI_z_m IRI de zonas de marisqueo para cada punto
	Double.class,
	//IRI_b IRI de bateas para cada punto
	Double.class,
	//IRI_p IRI de piscifactorías para cada punto
	Double.class,
	//Cuenca_km2
	Double.class            };

}
