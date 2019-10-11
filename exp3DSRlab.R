install.packages("gcookbook")
install.packages("ggplot2")
library(gcookbook)
library(ggplot2)

cabbage_exp
str(cabbage_exp)

ggplot(mpg,aes(displ,hwy,colour = class)) +
  geom_pointrange(ymin=0,ymax=0)



plot(graph, dnorm(graph,150,2))
  plot(graph, rnorm(graph,150,2))
