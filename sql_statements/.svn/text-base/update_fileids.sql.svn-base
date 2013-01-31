
update annotations p, revisions r set p.fileid = r.fileid
where r.project = p.project and r.module = p.module and p.filename =
r.filename and r.transactionid = p.revision;

update casts p, revisions r set p.fileid = r.fileid
where r.project = p.project and r.module = p.module and p.filename =
r.filename and r.transactionid = p.revision;

update casts_changed p, revisions r set p.fileid = r.fileid
where r.project = p.project and r.module = p.module and p.filename =
r.filename and r.transactionid = p.revision;

update halstead p, revisions r set p.fileid = r.fileid
where r.project = p.project and r.module = p.module and p.filename =
r.filename and r.transactionid = p.revision;

update parameterized_declarations p, revisions r set p.fileid = r.fileid
where r.project = p.project and r.module = p.module and p.filename =
r.filename and r.transactionid = p.revision;

update parameterized_declarations_changed p, revisions r set p.fileid = r.fileid
where r.project = p.project and r.module = p.module and p.filename =
r.filename and r.transactionid = p.revision;

update parameterized_types p, revisions r set p.fileid = r.fileid
where r.project = p.project and r.module = p.module and p.filename =
r.filename and r.transactionid = p.revision;

update parameterized_types_changed
 p, revisions r set p.fileid = r.fileid
where r.project = p.project and r.module = p.module and p.filename =
r.filename and r.transactionid = p.revision;

update rawtypes
 p, revisions r set p.fileid = r.fileid
where r.project = p.project and r.module = p.module and p.filename =
r.filename and r.transactionid = p.revision;

update rawtypes_changed
 p, revisions r set p.fileid = r.fileid
where r.project = p.project and r.module = p.module and p.filename =
r.filename and r.transactionid = p.revision;

select count(*),project,fileid=0  from annotations group by project,fileid=0;
select count(*),project,fileid=0 from casts group by project,fileid=0;
select count(*),project,fileid=0 from casts_changed group by project,fileid=0;
select count(*),project,fileid=0 from halstead group by project,fileid=0;
select count(*),project,fileid=0 from parameterized_declarations group by project,fileid=0;
select count(*),project,fileid=0 from parameterized_declarations_changed group by project,fileid=0;
select count(*),project,fileid=0 from parameterized_types group by project,fileid=0;
select count(*),project,fileid=0 from parameterized_types_changed group by project,fileid=0;
select count(*),project,fileid=0 from rawtypes group by project,fileid=0;
select count(*),project,fileid=0 from rawtypes_changed group by project,fileid=0;
