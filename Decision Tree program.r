install.packages("rpart.plot")  #install this package
library("rpart")
library("rpart.plot")
play<-c('yes','no','yes','no','yes','yes','yes','yes','yes','no')
outlook<-c('rainy','rainy','overcast','sunny','rainy','sunny','rainy','sunny','overcast','sunny')
Temperature<-c('cool','cool','hot','mild','cool','cool','cool','hot','mild','mild')
Humidity<-c('normal','normal','high','high','normal','normal','normal','normal','high','high')
Wind<-c('FALSE','TRUE','FALSE','FALSE','FALSE','FALSE','FALSE','FALSE','TRUE','TRUE')
play_decision<-cbind(play,outlook,Temperature,Humidity,Wind)
play_decision
play_decision=as.data.frame(play_decision)
play_decision
summary(play_decision)
fit=rpart(play ~ outlook + Temperature  +Humidity +Wind,method="class",data=play_decision,control=rpart.control(minsplit=1),parms=list(split='information'))
fit
str(fit)
rpart.plot(fit,type=4,extra=1)
rpart.plot(fit,type=4,extra=2,clip.right.labs=FALSE,varlen=0,faclen=0)

?rpart.plot
