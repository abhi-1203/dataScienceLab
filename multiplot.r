Length<-c(20,21,22,23,21,20)
Speed<-c(12,14,12,16,20,21)
Algae<-c(40,45,45,80,75,65) 
NO3<-c(2.25,2.15,1.75,1.95,1.95,2.75)
BOD<-c(200,180,135,120,110,120)
mf<-data.frame(Length,Speed,Algae,NO3,BOD)
mf
Opt=par(mfrow=c(2,2))
Opt
plot(Length~BOD,data=mf, main='plot1')
plot.new()
plot.new()
plot(Length ~ NO3,data=mf,main='plot4')
