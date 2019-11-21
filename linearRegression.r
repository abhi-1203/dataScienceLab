getwd()
setwd("C:/Program Files/RStudio/R/Dataset")
getwd()
var1<-read.csv("Mult_Reg_Yield.csv")
var1
set.seed(1234)
ind<-sample(2,nrow(var1),replace=TRUE,prob=c(0.7,0.3))
ind
length(ind)
training<-var1[ind==1,]
testing<-var1[ind==2,]

#Multiple Linear Regression
model<-lm(X.Yield ~ Time +Temperature,data=training)
model
summary(model)
plot(X.Yield ~ Time,training)
abline(model,col="red")
modelnew<-lm(X.Yield ~ Time,data=training)
modelnew
summary(modelnew)
plot(X.Yield ~ Time,training)
abline(modelnew,col="red")

#Prediction
newypred<-fitted(modelnew)                   #predict y values  for each x value
newypred
pred<-predict(modelnew,training)             #Both are same
pred
testpred<-predict(modelnew,testing)
testpred
predict(model,data.frame(Time=200,Temperature=240))
predict(modelnew,data.frame(Time=200,Temperature=240))
