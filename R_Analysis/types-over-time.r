source("query.r")

toDates = function(vec)
{ 
  dates = c()
  for (date in vec) {dates = c(as.Date(date))}
  return(dates)
}

timePlot = function(project) {
  datafile = sprintf("%s.tsv", project)

  data = read.table(datafile, sep="\t", header=1)

  dates = toDates(data[1])


  data$date = dates
  title = sprintf("Types usage in %s", project)

  m = max(max(data$rtypes), max(data$ptypes))

  plot(data$date, data$rtypes, typ="l", xlab="Date", ylab="Number of Type Uses",
	main=title, col="red", lty="solid", ylim=c(0, m))

  lines(data$date, data$ptypes, col="blue", lty="dashed")

  legend("topleft", legend = c("Raw Types", "Parameterized Types"),
	lty=c("solid", "dashed"), col=c("red", "blue"), inset=0.02)
  

  return(data)

}

