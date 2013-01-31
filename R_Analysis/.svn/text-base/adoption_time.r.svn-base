source("query.r")

toDates = function(vec)
{ 
  dates = c()
  for (date in vec) {dates = c(as.Date(date))}
  return(dates)
}

timePlot = function(project, user, passwd) {

  sql = sprintf("select distinct r.datetime, r.userid, rtc.filename,
rtc.revision, rtc.container, rtc.type, ptc.class_type, ptc.type_args from
parameterized_types_changed ptc, rawtypes_changed rtc, revisions r where
ptc.filename = rtc.filename and ptc.revision = rtc.revision and rtc.container =
ptc.container and rtc.container_granularity = 'name_only' and
ptc.container_granularity = 'name_only' and not rtc.added and ptc.added and
rtc.type = ptc.class_type and rtc.project = '%s' and ptc.project =
rtc.project and r.project = rtc.project and r.transactionid = rtc.revision
order by r.datetime", project)

  
  sql2 = sprintf("select distinct r.datetime, r.ptc.class_type, ptc.type_args from
parameterized_types_changed ptc, revisions r where
ptc.container_granularity = 'name_only' and
ptc.added and
ptc.project = '%s' and r.project = ptc.project and r.transactionid = ptc.revision
order by r.datetime", project)
  
  data = executeMySqlQuery(sql, user, passwd, debug=1)

  dates = toDates(data[1])

  #dates = c()
  #for (date in data[1]) { dates = c(as.Date(date)) }

  data$date = dates
  title = sprintf("Conversions to Generics in %s", project)

  plot(data$date, 1:length(data$date), typ="l", xlab="Date", ylab="Conversions to Generics",
	main=title)
  readline()

  data$users = as.factor(data$userid)
  title = sprintf("Conversions to Generics in %s", project)
  plot(data$users, xlab="Contributor", ylab="Conversions to Generics", main=title)

  return(data)

}

sql = "select 
