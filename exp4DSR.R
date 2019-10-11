#a)Bottle are Being produced with mean as 150 cc and std deviation of 2 cc
#Sampes of 100 bottles show the mean as 152.Has the mean value changed check 
#with 95% confidence interval.calculate pnorm,dnorm values of the result obtained.

library(visualize)
library(BSDA)

zv = (152-150)/(2/sqrt(100))
zv
pnorm(zv)
visualize.norm(stat=zv,mu=0,sd=1,section="upper")
qnorm(0.95)
