source("query.r")

toDates = function(vec)
{ 
  dates = c()
  for (date in vec) {dates = c(as.Date(date))}
  return(dates)
}

allAuthorPlots = function() {
	pdf("author-plots.pdf", onefile=TRUE)
	projects = c("eclipse-cs", "azureus", "squirrel-sql", "junit",
		"springframework", "findbugs", "jedit", "hibernate")
	for (project in projects) {
		authorPlot(project)
	}
	dev.off()
}

authorPlot = function(project, num_authors = 5) {
  datafile = sprintf("%s-users.tsv", project)
  data = read.table(datafile, sep="\t", header=1)
  dates = toDates(data[1])

  data$date = dates

  # choose the 5 most active authors
  authors = names(sort(summary(data$user), decreasing=TRUE))[1:num_authors]

  title = sprintf("Authors use of Types in %s", project)

  ylim = c(min(min(data$cum_rtypes), min(data$cum_ptypes)),
  		max(max(data$cum_rtypes), max(data$cum_ptypes)))

  xlim = c(as.numeric(data$date[1]-100), as.numeric(data$date[length(data$date)] + 100))
  
  sub=data[data$user==authors[1],]
  print(authors)

  plot(sub$date, sub$cum_rtypes, typ="l", xlab="Date", ylab="Number of Type Uses",
	main=title, col="red", lty="solid", ylim=ylim, xlim=xlim)

  i = 1
  colors = c("red", "orange", "blue", "green", "purple")
  lcolors = c()
  ltypes = c()
  lnames = c()
  for (author in authors) {
	sub = data[data$user==author,]
	lines(sub$date, sub$cum_ptypes, col=colors[i], lty="dashed")
	lines(sub$date, sub$cum_rtypes, col=colors[i], lty="solid")
	lcolors = c(lcolors, colors[i], colors[i])
	ltypes = c(ltypes, "dashed", "solid")
	lnames = c(lnames, paste(author, "ptypes"), paste(author, "rtypes"))
	i = i + 1
  }

  legend("topleft", legend = lnames,
	lty=ltypes, col=lcolors, inset=0.02)
  #return(data)
}
