#Creates SSANOVA plots
#Example prompt to place in R-console:
#source("/Volumes/Second HD (3TB)/ScotsGaelic2013/Analyses/ssanova_group_sj.R")
#plotsFromCSV("/SingleFrames/05/05_NTC_compiled.txt","/CSV_comparison_stims/SG_study_11a.csv", "05")
#Includes function name, path to compliled trace adjustments, path to CSV comparisons, and subject identifier (to place on plots)


plotsFromCSV<-function(subjectFile,comparisonCSV,plotIdentifier) {

	source('/Volumes/Second HD (3TB)/ScotsGaelic2013/Analyses/ssanova_sj.R')

	#a table of the calculated coordinates in the file generated from Julia's script; variable honorably named after her
	julia <- read.table(paste(path,subjectFile,sep=""),h=T)

	#a table of a csv file of the first stim in column A, second in column B
	loadedCSV<-read.csv(paste(path,comparisonCSV,sep=""), header = TRUE, sep = ",",quote="", stringsAsFactors = FALSE)
	for(i in 1:length(loadedCSV[,1])) {
		word1<-loadedCSV[i,1]
		word2<-loadedCSV[i,2]

		out<-tryCatch(
			{
				compare(word1, word2, julia, plotIdentifier);
				comparegray(word1, word2, julia, paste("gray",plotIdentifier,sep = "_"));
				},
			error=function(cond) {
				print(paste("MY_ERROR:  ",cond))
				}
		)
	}
}
