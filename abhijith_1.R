getwd()
list.files(getwd(), pattern=NULL, all.files=TRUE, full.names=TRUE) 

mydata= read.csv("adult.csv")
mydata
summary(mydata)
class(mydata)
plot(mydata$income,mydata$education)
head(mydata)
my_data=mydata$income
mydata = cbind(mydata, my_data)
head(mydata)

hello=read.delim("adult.csv")
hello
d=hello[1,]
d
write.table(d,"new.csv",sep="\t",row.names = TRUE)
new=read.table("new.csv")
new
