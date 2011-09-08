# R Script to process Simple IRI results.
# It takes all dbfs and get a csv with the first factor found. 
# That's good for comparing with IRI formula of year 2009-2010

#### USAGE Example
## $for i in `ls`; do 
#       Rscript /home/nachouve/Dropbox/Nac_Kon/scriptR_prepararSimpleIRI.r $i $i >> /tmp/Result_IRI.txt;  
#  done
#####################

library(foreign)

args <- commandArgs(TRUE)

NAME = args[1]
DBF = args[2]

#DBF = '/home/conchalonso/compartida_vbox/Salidas_windows/IRI_Simple/11_08_29/net/1_Capa_175_IRInet_MONTECELO[87].dbf'
#DBF = '/home/nachouve/Dropbox/Nac_Kon/1_Capa_175_IRInet_MONTECELO[87].dbf'

copyXp <- function(arr, colNum){

       colXp = 1

       noZero = arr[,colNum]>0

       num = colNum[1]
       for (col in 1:length(colNum)) {
	     col_n = noZero[,col]
	     i = 1
	     for (idx in col_n){
	       #cat("col:", col, "i:", i, "idx: ", idx, "\n")
               if (idx){
	         xp = arr[i, colXp]
		 if (xp == 0){
		    xp = "ZERO";
		 }	
	         arr[i,num] = xp
	       }
	       i = i + 1
	   }
	   num = num + 1
       }
       return (arr)
}

getFirstNoZero <- function(arr, colNum){
   out = c()
   for (i in colNum){
       col = arr[,i]
       firstNoZero = col[col>0][1]
       out = c(out, firstNoZero)
   }
   return(out)
}

### READ DBF FILE

COLS_TO_PROCESS = 2:12

iri = read.dbf(DBF)
iri2 = copyXp(iri, COLS_TO_PROCESS)

#print(iri2)
#print("--------------------------------")

COLS_TO_PROCESS = 3:12
firsts = getFirstNoZero(iri2, COLS_TO_PROCESS)
aux = c(NAME,firsts)

out = gsub(',',';',toString(aux))
out = gsub('ZERO','0',toString(out))
print(out)
