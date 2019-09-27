install.packages("ggplot2")
install.packages("gcookbook")
install.packages("MASS")
library(gcookbook)
library(ggplot2)
library(MASS)

#4a)Using the dataset Cabbage_exp ( import from library gcookbook) create a bargraph as shown below for the cultivar field.
cabbage_exp
m<-matrix(cabbage_exp$Weight,cabbage_exp$n)
m
barplot(m, 
        main="Cabbage_Exp",col=c("green","blue"), beside=TRUE,xlab="weights",ylab="sl.no")

#4b)Create a BoxPlot using the dataset BirthWt (import from library gcookbook) with the x axis as ageyear and y axis as height.
birthwt
boxplot(age~ht, data = birthwt, xlab = "age",
        ylab = "height", main = "birth height")

