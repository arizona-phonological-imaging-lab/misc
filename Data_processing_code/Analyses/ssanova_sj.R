################################################################
# function codes -- sort of like m-files.  we just have to put
# this in once

# comp will give you the smoothing spline fit
# to get smoothing parameter values, type summary(*)

comp<-function(data,word1,word2){
      # w1w2<-data[data$word == word1 | data$word == word2,]
      w1w2<-rbind(subset(data,word == word1),subset(data,word == word2));
      # print(w1w2)
       a<-levels(w1w2$word)
       # print(a)
       word3<-a[a!=word1 & a!=word2]
       # print(word3)
       levels(w1w2$word)<-list(word1=c(word1,word3),word2=word2)
       # print(levels)
       fit.w1w2<-ssr(Y~word*X,rk=list(cubic(X),rk.prod(cubic(X),shrink1(word))),
               data=w1w2,scale=T)
}

#comp.plot will you the fitted curves and corresponding BCI's

comp.plot<-function(fit,word.1,word.2,subject){
       w1<-fit$data[fit$data$word=="word1",]
       w2<-fit$data[fit$data$word=="word2",]
       n1<-nrow(w1)
       ntot<-nrow(w1)+nrow(w2)
       fit.pred<-predict(fit)
       fit.w1<-data.frame(fit=fit.pred$fit[1:n1],pstd=fit.pred$pstd[1:n1])
       fit.w2<-data.frame(fit=fit.pred$fit[n1+1:ntot],pstd=fit.pred$pstd[n1+1:ntot])
       order1<-order(w1$X)
       order2<-order(w2$X)
       par(mfrow=c(1,1))
       path <- "/Volumes/Second HD (3TB)/ScotsGaelic2013/Analyses/plots/Z_X_Y.pdf"
       path <- sub('X', word.1, path)
       path <- sub('Y', word.2, path)
       path <- sub('Z', subject, path)
       pdf(path, width=8, height=6)
       plot(fit$data$X,-fit$data$Y,type="n",xlab="X",ylab="Y")
       #points(w1$X,-w1$Y,col=2,pch=".")
       lines(w1$X[order1],-fit.w1$fit[order1],col=2,lwd=6,lty=4)
       lines(w1$X[order1],-fit.w1$fit[order1]+1.96*fit.w1$pstd[order1],col=2,lwd=2,lty=4)
       lines(w1$X[order1],-fit.w1$fit[order1]-1.96*fit.w1$pstd[order1],col=2,lwd=2,lty=4)
       #points(w2$X,-w2$Y,col=3,pch=".")
       lines(w2$X[order2],-fit.w2$fit[order2],col=12,lwd=6,lty=5)
       lines(w2$X[order2],-fit.w2$fit[order2]+1.96*fit.w2$pstd[order2],col=12,lwd=2,lty=5)
       lines(w2$X[order2],-fit.w2$fit[order2]-1.96*fit.w2$pstd[order2],col=12,lwd=2,lty=5)
       title(paste(word.1,"vs",word.2))

	coords <- par("usr");
	legend(coords[1]+5,coords[4]-5,c(word.1,word.2),lwd=3,col=c(2,12),lty=c(4,5))
       dev.off()
}

# Same as comp.plot2 above, but uses point markers that will show up in grayscale


comp.plot2<-function(fit,word.1,word.2,subject){
       w1<-fit$data[fit$data$word=="word1",]
       w2<-fit$data[fit$data$word=="word2",]
       n1<-nrow(w1)
       ntot<-nrow(w1)+nrow(w2)
       fit.pred<-predict(fit)
       fit.w1<-data.frame(fit=fit.pred$fit[1:n1],pstd=fit.pred$pstd[1:n1])
       fit.w2<-data.frame(fit=fit.pred$fit[n1+1:ntot],pstd=fit.pred$pstd[n1+1:ntot])
       order1<-order(w1$X)
       order2<-order(w2$X)
       par(mfrow=c(1,1))
       path <- "/Volumes/Second HD (3TB)/ScotsGaelic2013/Analyses/plots/Z_X_Y.pdf"
       path <- sub('X', word.1, path)
       path <- sub('Y', word.2, path)
       path <- sub('Z', subject, path)
       pdf(path, width=8, height=6)
       plot(fit$data$X,-fit$data$Y,type="n",xlab="X",ylab="Y")

       lines(w1$X[order1],-fit.w1$fit[order1]+1.96*fit.w1$pstd[order1],col=1,lwd=1,lty=3)
       lines(w1$X[order1],-fit.w1$fit[order1],col=1,lwd=5)
       lines(w1$X[order1],-fit.w1$fit[order1]-1.96*fit.w1$pstd[order1],col=1,lwd=1,lty=3)

       lines(w2$X[order2],-fit.w2$fit[order2],col=8,lwd=5)
       lines(w2$X[order2],-fit.w2$fit[order2]+1.96*fit.w2$pstd[order2],col=8,lwd=1,lty=5)
       lines(w2$X[order2],-fit.w2$fit[order2]-1.96*fit.w2$pstd[order2],col=8,lwd=1,lty=5)
       title(paste(word.1,"vs",word.2))

	coords <- par("usr");
	legend(coords[1]+5,coords[4]-5,c(word.1,word.2),lwd=5,col=c(1,8),lty=c(5,5))
       dev.off()
}

comp.plot3<-function(fit1,fit2,word.1,word.2,word.3,subject){
       w1<-fit1$data[fit1$data$word=="word1",]
       w2<-fit1$data[fit1$data$word=="word2",]
       w3<-fit2$data[fit2$data$word=="word1",]
       n1<-nrow(w1)
       ntot<-nrow(w1)+nrow(w2)
       n3<-nrow(w3)
       fit1.pred<-predict(fit1)
       fit2.pred<-predict(fit2)
       fit.w1<-data.frame(fit=fit1.pred$fit[1:n1],pstd=fit1.pred$pstd[1:n1])
       fit.w2<-data.frame(fit=fit1.pred$fit[n1+1:ntot],pstd=fit1.pred$pstd[n1+1:ntot])
       fit.w3<-data.frame(fit=fit2.pred$fit[1:n3],pstd=fit2.pred$pstd[1:n3])
       order1<-order(w1$X)
       order2<-order(w2$X)
       order3<-order(w3$X)
       par(mfrow=c(1,1))
       path <- "/Volumes/Second HD (3TB)/ScotsGaelic2013/Analyses/Epen_plots/Z_W_X_Y.pdf"
       path <- sub('W', word.1, path)
       path <- sub('X', word.2, path)
       path <- sub('Y', word.3, path)
       path <- sub('Z', subject, path)
       pdf(path, width=8, height=6)
       plot(fit1$data$X,-fit1$data$Y,type="n",xlab="X",ylab="Y")
       #points(w1$X,w1$Y,col=2,pch=".")
       lines(w1$X[order1],-fit.w1$fit[order1],col=2,lwd=6, lty=1)
       lines(w1$X[order1],-fit.w1$fit[order1]+1.96*fit.w1$pstd[order1],col=2,lwd=2,lty=1)
       lines(w1$X[order1],-fit.w1$fit[order1]-1.96*fit.w1$pstd[order1],col=2,lwd=2,lty=1)
       #points(w2$X,-w2$Y,col=3,pch=".")
       lines(w2$X[order2],-fit.w2$fit[order2],col=3,lwd=6, lty=2)
       lines(w2$X[order2],-fit.w2$fit[order2]+1.96*fit.w2$pstd[order2],col=3,lwd=2,lty=2)
       lines(w2$X[order2],-fit.w2$fit[order2]-1.96*fit.w2$pstd[order2],col=3,lwd=2,lty=2)

       lines(w3$X[order3],-fit.w3$fit[order3],col=4,lwd=6, lty=3)
       lines(w3$X[order3],-fit.w3$fit[order3]+1.96*fit.w3$pstd[order3],col=4,lwd=2,lty=3)
       lines(w3$X[order3],-fit.w3$fit[order3]-1.96*fit.w3$pstd[order3],col=4,lwd=2,lty=3)

       #title("Y3")

       coords <- par("usr");
       legend(coords[1]+1,coords[4]-10,c(word.1, word.2, word.3),lwd=3,lty=c(1,2,3),col=c(2,3,4),cex=.8)
       dev.off()
}


#getting Bayes Confidence Intervals for interaction effects
#if the BCI's include 0 at a given value of X, then the two curves are
#similar there.

get.int<-function(fit,word.1,word.2){
       w1<-fit$data[fit$data$word=="word1",]
       w2<-fit$data[fit$data$word=="word2",]
       n1<-nrow(w1)
       ntot<-nrow(w1)+nrow(w2)
       fit.pred<-predict(fit,terms=c(0,1,0,1,0,1))
       fit.w1<-data.frame(fit=fit.pred$fit[1:n1],pstd=fit.pred$pstd[1:n1])
       fit.w2<-data.frame(fit=fit.pred$fit[n1+1:ntot],pstd=fit.pred$pstd[n1+1:ntot])
       order1<-order(w1$X)
       order2<-order(w2$X)
       par(mfrow=c(2,1))
       ylimits<-c(min(-fit.w1$fit[order1]-3*fit.w1$pstd[order1]),
               max(-fit.w1$fit[order1]+3*fit.w1$pstd[order1]))
       plot(fit$data$X,-fit.pred$fit,type="n",xlab="X",ylab="Y",ylim=ylimits)
       abline(0,0)
       lines(w1$X[order1],-fit.w1$fit[order1],col=2,lwd=5)
       lines(w1$X[order1],-fit.w1$fit[order1]+3*fit.w1$pstd[order1],col=2,lwd=1,lty=3)
       lines(w1$X[order1],-fit.w1$fit[order1]-3*fit.w1$pstd[order1],col=2,lwd=1,lty=3)
       title(paste("Interaction effects w/BCI for",word.1))
       ylimits<-c(min(-fit.w2$fit[order2]-3*fit.w2$pstd[order2]),
               max(-fit.w2$fit[order2]+3*fit.w2$pstd[order2]))
       plot(fit$data$X,-fit.pred$fit,type="n",xlab="X",ylab="Y",ylim=ylimits)
       abline(0,0)
       lines(w2$X[order2],-fit.w2$fit[order2],col=3,lwd=5)
       lines(w2$X[order2],-fit.w2$fit[order2]+3*fit.w2$pstd[order2],col=3,lwd=1,lty=3)
       lines(w2$X[order2],-fit.w2$fit[order2]-3*fit.w2$pstd[order2],col=3,lwd=1,lty=3)
       title(paste("Interaction effects w/BCI for",word.2))
       list(get.int=fit.pred)
}

compare<-function(w1,w2,data,subject){
	sepsp.t1<-comp(data=data,w1,w2)
      print('1')
	summary(sepsp.t1)
      print('2')
	comp.plot(sepsp.t1,w1,w2,subject)
      print('3')
#	interaction.bci<-get.int(sepsp.t1,w1,w2)
}

comparegray<-function(w1,w2,data,subject){
	sepsp.t1<-comp(data=data,w1,w2)
	summary(sepsp.t1)
	comp.plot2(sepsp.t1,w1,w2,subject)
#	interaction.bci<-get.int(sepsp.t1,w1,w2)
}

compare3<-function(w1,w2,w3,data,subject){
    sepsp.t1<-comp(data=data,w1,w2)
    sepsp.t2<-comp(data=data,w3,w2)
    summary(sepsp.t1)
    summary(sepsp.t2)
    comp.plot3(sepsp.t1,sepsp.t2,w1,w2,w3,subject)
}

##################################################################
# for ssatest.txt
##################################################################

library(assist)
options(memory=1000000000)


