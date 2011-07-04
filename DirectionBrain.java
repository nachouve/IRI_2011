package es.udc.sextante.gridAnalysis.IRI;

import java.util.ArrayList;

/**
 * Una lista FIFO para hacer algo inteligente la direccion a tomar
 * @author uve
 *
 */
public class DirectionBrain {

    private final int SIZE;
    private final double MDT_TOLERANCE;
    private final double ERROR_TOLERANCE;
    private final ArrayList<Integer> directions;

    public DirectionBrain(double mdt_tolerance, int size){
	this(mdt_tolerance, 0.0, size);
    }

    public DirectionBrain(double mdt_tolerance, double error_tolerance, int size){
	SIZE = size;
	MDT_TOLERANCE = mdt_tolerance;
	ERROR_TOLERANCE = error_tolerance;
	directions = new ArrayList<Integer>();
    }

    public void addDirection(int dir){
	if (directions.size() == SIZE){
	    directions.remove(0);
	}
	directions.add(dir);
    }

    public int getLastDirection(){
	if (directions.size()>0){
	    return directions.get(directions.size()-1);
	} else {
	    return -1;
	}
    }

    public int getLastDirection(int num_last){
	int size = directions.size();
	if (directions.isEmpty()){
	    return -1;
	}
	if (size < num_last ){
	    num_last = size;
	}
	//int[] dir_sum = new int[8];
	//for (int i = 0; i < 8; i++){
	int[] dir_sum = new int[24];
	for (int i = 0; i < 24; i++){
	    dir_sum[i] = -1;
	}

	for (int i = 0; i < num_last; i++){
	    dir_sum[directions.get(size-1-i)] = dir_sum[directions.get(size-1-i)]++;
	}

	int max_dir = -1;
	int max_repetition = -1;
	for (int i = 0; i < 24; i++){
	    if (dir_sum[i] > max_repetition){
		max_repetition = dir_sum[i];
		max_dir = i;
	    }
	}
	return max_dir;

    }


    //    public int getMostFrecuentDirection(){
    //	int sum = 0;
    //	int num = directions.size();
    //	int[] dir_sum = new int[8];
    //
    //	for (int i = 0; i < 8; i++){
    //	    dir_sum[i] = -1;
    //	}
    //
    //	for (int i = 0; i < num; i++){
    //	    dir_sum[directions.get(i)] = dir_sum[directions.get(i)]++;
    //	}
    //
    //	int max_dir = -1;
    //	int max_repetition = -1;
    //	for (int i = 0; i < 8; i++){
    //	    if (dir_sum[i] > max_repetition){
    //		max_repetition = dir_sum[i];
    //		max_dir = i;
    //	    }
    //	}
    //	return max_dir;
    //    }

    private int getMinZdir(ArrayList<Integer> cell_dirs, double[] z_pos){
	int min_dir = -1;
	double min = Double.MAX_VALUE;
	for (int i = 0; i < cell_dirs.size(); i++){
	    double value = z_pos[cell_dirs.get(i)];
	    if (value < min){
		min = value;
		min_dir = cell_dirs.get(i);
	    }
	}
	return min_dir;
    }


    /**
     * Hice una prueba de pasarle 24 celdas de alrededor y aún está ahí el código. pero el tema es que si el MDT está bien relleno
     * nunca debería llegar a usar ese código.
     * 
     * @param z mdt value of the currect cell
     * @param z_pos values of the 8 possibities cell
     * @return
     */
    public int getLogicalDirection(double z, double[] z_pos, boolean onlyMin){

	double min = Double.MAX_VALUE;
	for (int i = 0; i < z_pos.length; i++){
	    System.out.println(z + "[" + i +"] --> " + z_pos[i]);
	    if (z_pos[i] < min){
		min = z_pos[i];
	    }
	}
	ArrayList<Integer> min_cells = new ArrayList<Integer>();

	for (int i = 0; i < z_pos.length; i++){
	    if (z_pos.length == 8) {
		int[] not_allowed = getNotAllowedCells(getLastDirection());

		boolean not_allowed_dir = false;
		for (int j = 0; j < not_allowed.length; j++){
		    if (not_allowed[j] == i){
			not_allowed_dir = true;
			break;
		    }
		}
		if (not_allowed_dir){
		    continue;
		}
	    }
	    if (onlyMin){
		if (z_pos[i] <= (min + ERROR_TOLERANCE)){
		    System.out.println(" Added_E[" + i +"]");
		    min_cells.add(i);
		}
	    } else {
		if (z_pos[i] <= (z + MDT_TOLERANCE)){
		    System.out.println(" Added_T[" + i +"]");
		    min_cells.add(i);
		}
	    }
	}

	// If lastDirection can be... go ahead!
	int lastDirection = getLastDirection();
	if (onlyMin && min_cells.contains(lastDirection)){
	    System.out.println(" Using last [" + lastDirection +"]");
	    return lastDirection;
	}


	//	for (int i = 0; i < not_allowed.length; i++){
	//	    min_cells.remove(new Integer(not_allowed[i]));
	//	}

	// If most used Direction of the last 3 can be... go ahead!
	int last3Direction = getLastDirection(3);
	if (onlyMin && min_cells.contains(last3Direction)){
	    System.out.println(" Using 3 Brains [" + last3Direction +"]");
	    return last3Direction;
	}

	// If most used Direction of the last X can be... go ahead!
	int lastXDirection = getLastDirection(SIZE);
	if (onlyMin && min_cells.contains(lastXDirection)){
	    System.out.println(" Using 10 Brains [" + lastXDirection +"]");
	    return lastXDirection;
	}

	if (min_cells.size()>0){
	    System.out.println(" Using first on the array [" + min_cells.get(0) +"]");
	    return getMinZdir(min_cells, z_pos);
	} else {
	    if (onlyMin){
		System.out.println(" CALL WITH MORE TOLERANCE]");
		return getLogicalDirection(z, z_pos, false);
	    }
	    return -1;
	}
    }


    private int[] getNotAllowedCells(int lastDirection){
	int[] not_allowed_dir = new int[3];
	if (lastDirection == -1){
	    not_allowed_dir[0] = -1;
	    not_allowed_dir[1] = -1;
	    not_allowed_dir[2] = -1;
	} else {
	    int aux = (lastDirection + 3) % 8;
	    not_allowed_dir[0] = aux;
	    not_allowed_dir[1] = (aux + 1) % 8;
	    not_allowed_dir[2] = (aux + 2) % 8;
	}
	return not_allowed_dir;
    }

}
