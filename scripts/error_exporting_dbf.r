##
# Script to test the exporting to dbf of tables with many columns.
##

library(foreign)


### 
NUM=1000

v = c(1:NUM)
test = matrix(v, ncol=length(v))

name_cols = c()
for (i in 1:NUM) {
    name_cols = c(name_cols, paste("A_",i,sep=""))
}
colnames(test) <- name_cols

write.dbf(test, "/tmp/test1000.dbf")


### NOW WITH 8000 columns

NUM=8000

v = c(1:NUM)
test = matrix(v, ncol=length(v))

name_cols = c()
for (i in 1:NUM) {
    name_cols = c(name_cols, paste("A_",i,sep=""))
}
colnames(test) <- name_cols

write.dbf(test, "/tmp/test8000.dbf")

########
## Check results

d1000 = read.dbf('/tmp/test1000.dbf')
d8000 = read.dbf('/tmp/test8000.dbf')

length(d1000)
## Returns --> [1] 1000

length(d8000)
## Returns --> [1] 1856