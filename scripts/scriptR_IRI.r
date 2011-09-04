##
# Carga la tabla de resultados de la union del IRI
#

library(foreign)

getVertidos <- function(rowNum, arr) {
   ##Get all columns with "name of vertidos"
   row = arr[rowNum, grep("_name_ve", colnames(arr))]
   ##Get only differents to "--" 
   return(row[row != "--"])
}

getValuesVertidos <- function(rowNum, arr) {

   ## Get all columns with "name of vertidos"
   ## More than 20 because first columns are IRI_sumarizes
   rowNames = arr[rowNum, grep("_name_ve", colnames(arr))]
   rowValues = arr[rowNum, grep("_IRI$", colnames(arr))]

   ## Get only differents to "--"
   names = rowNames[rowNames != "--"]
   values = as.numeric(rowValues[rowValues != -1])
   
   return(matrix(c(names, values), ncol=2, nrow=length(names)))

}

addPercentColumn <- function(total, arr) {
   v = (100*as.numeric(arr[,2]))/total
   return (matrix(c(arr,v),ncol=3))

}


OUTPUT='/tmp/reduzed.csv'
#OUTPUT='/home/nachouve/CARTOLAB/IRI/outCSV.csv'

iri = read.csv(OUTPUT, sep=';', header=TRUE)
iri[, grep("_ve", colnames(iri))]


### Test
z = getValuesVertidos(9,iri)

print(z)
cat('\n')

z2 = addPercentColumn(iri[9,3],z)

print(z2)
cat('\n')