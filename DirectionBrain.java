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


    public int getDirection8(int dir){

	if (dir < 8){
	    return dir;
	}

	switch (dir) {
	case 8: dir = 0; break;
	case 9: dir = 0; break;
	case 10: dir = 1; break;
	case 11: dir = 1; break;
	case 12: dir = 2; break;
	case 13: dir = 2; break;
	case 14: dir = 3; break;
	case 15: dir = 3; break;
	case 16: dir = 4; break;
	case 17: dir = 4; break;
	case 18: dir = 5; break;
	case 19: dir = 5; break;
	case 20: dir = 6; break;
	case 21: dir = 6; break;
	case 22: dir = 7; break;
	case 23: dir = 7; break;
	default: dir = -1; break;
	}
	return dir;
    }

    public void addDirection(int dir){

	if (dir >= 8){
	    dir = getDirection8(dir);
	}

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

	//	int[] dir_sum = new int[24];
	//	for (int i = 0; i < 24; i++){
	int[] dir_sum = new int[8];
	for (int i = 0; i < 8; i++){
	    dir_sum[i] = -1;
	}

	for (int i = 0; i < num_last; i++){
	    dir_sum[directions.get(size-1-i)] = dir_sum[directions.get(size-1-i)]++;
	}

	int max_dir = -1;
	int max_repetition = -1;
	//	for (int i = 0; i < 24; i++){
	for (int i = 0; i < 8; i++){
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
     * Check 3 cells on the last directions. Needs 24 cells (5x5)
     * 
     * @param z
     * @param z_pos 24 positions
     * @return
     */
    public int getDirection2Steps(double z, double[] z_pos){

	int lastDirection = getLastDirection();

	int[] possibleCells = new int[3];

	if (lastDirection == 0){
	    possibleCells[0] = 23;
	    possibleCells[1] = 8;
	    possibleCells[2] = 9;
	} else if (lastDirection == 1){
	    possibleCells[0] = 9;
	    possibleCells[1] = 10;
	    possibleCells[2] = 11;
	} else if (lastDirection == 2){
	    possibleCells[0] = 11;
	    possibleCells[1] = 12;
	    possibleCells[2] = 13;
	} else if (lastDirection == 3){
	    possibleCells[0] = 13;
	    possibleCells[1] = 14;
	    possibleCells[2] = 15;
	} else if (lastDirection == 4){
	    possibleCells[0] = 15;
	    possibleCells[1] = 16;
	    possibleCells[2] = 17;
	} else if (lastDirection == 5){
	    possibleCells[0] = 17;
	    possibleCells[1] = 18;
	    possibleCells[2] = 19;
	} else if (lastDirection == 6){
	    possibleCells[0] = 19;
	    possibleCells[1] = 20;
	    possibleCells[2] = 21;
	} else if (lastDirection == 7){
	    possibleCells[0] = 21;
	    possibleCells[1] = 22;
	    possibleCells[2] = 23;
	} else {
	    possibleCells[0] = -1;
	    possibleCells[1] = -1;
	    possibleCells[2] = -1;
	}
	double min = Double.MAX_VALUE;
	int dir = -1;
	for (int i = 0; i < 3; i++){
	    int aux_dir = possibleCells[i];
	    System.out.println(z + "[" + aux_dir +"] --> " + z_pos[aux_dir]);
	    if (z_pos[aux_dir] < min){
		min = z_pos[aux_dir];
		dir = aux_dir;
	    }
	}

	if (min < z + MDT_TOLERANCE){
	    return dir;
	}
	return -1;

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
