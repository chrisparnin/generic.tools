import sys, os
sys.path.append(os.path.join("..", "lib"))
import pymysql

def main(project, conn):

	sql = """ select datetime, r.filename, state, rawtype_container, rawtype_type, container_granularity
		from revisions r, rawtypes t where r.project = t.project and r.project = '%(project)s'
		and r.transactionid = t.revision and r.filename = t.filename""" % locals()
	print sql
	cursor = conn.cursor()
	cursor.execute(sql)
	timeHash = {}
	for time, filename, state, container, type, container_granularity in cursor:
		if container_granularity == "name_only":
			continue
		if not timeHash.has_key(time):
			timeHash[time] = []
		timeHash[time].append( (filename, state, container, type, "raw") )
	
	
	sql = """ select datetime, r.filename, state, container, class_type, container_granularity
		from revisions r, parameterized_types t where r.project = t.project and r.project = '%(project)s'
		and r.transactionid = t.revision and r.filename = t.filename""" % locals()
	print sql
	cursor = conn.cursor()
	cursor.execute(sql)
	for time, filename, state, container, type, container_granularity in cursor:
		if container_granularity == "name_only":
			continue
		if not timeHash.has_key(time):
			timeHash[time] = []
		timeHash[time].append( (filename, state, container, type, "parameterized") )

	times = timeHash.keys()
	times.sort()

	print times

	fd = open(project + ".tsv", "w")
	print >> fd, "datetime\trtypes\tptypes"
	files = {}
	for time in times:
		print time
		for file in [x[0] for x in timeHash[time]]:
			files[file] = []
		for filename, state, container, type, kind in timeHash[time]:
			if state == 'deleted':
				pass
			else:
				files[filename].append( (type, kind) )
		totalRawTypes = 0
		totalParameterizedTypes = 0
		for file, types in files.items():
			for type, kind in types:
				if kind == "raw":
					totalRawTypes += 1
				else:
					totalParameterizedTypes += 1
		print "total raw types at time", time, "is", totalRawTypes
		print "total parameterized types at time", time, "is", totalParameterizedTypes
		print >> fd, str(time)+"\t"+str(totalRawTypes)+"\t"+str(totalParameterizedTypes)

	
if __name__ == "__main__":
	if len(sys.argv) != 2:
		print >> sys.stderr, "usage: python " + __file__+ " username password "
		print >> sys.stderr, "arguments on the command line"
	conn = pymysql.connect(host="eb2-2291-fas01.csc.ncsu.edu",
		db="generics", port=4747, user=sys.argv[1], passwd = sys.argv[2])
	sql = "select distinct project from revisions"
	cursor = conn.cursor()
	cursor.execute(sql)
	projects = []
	for project, in cursor:
		projects.append(project)
	print projects
	for project in projects:
		main(project, conn)

