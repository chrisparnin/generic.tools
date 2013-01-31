sql = "select class_type, sum(count) as total from ( 
 select f.filename, transactionid, r.datetime, p.class_type, p.type_args, count 
 from revisions r, parameterized_types p, 
 (select r.filename, datetime, max(datetime) lastdate from revisions r 
     where r.project = 'eclipse-cs' group by filename) as f 
 where r.datetime = f.lastdate and r.filename = f.filename 
 and p.filename = r.filename and p.revision = r.transactionid 
 ) a group by class_type order by total desc;" 
print(sql)
a = executeMySqlQuery(sql, "bird", "30pastnoon")

par(mar=c(6.1, 3.1, 4.1, 0.5)) 

barplot(a$total, names.arg=a$class_type, las=2, main="Use of Generics in Eclipse-cs")

