import sys, os
sys.path.append(os.path.join("..", "lib"))
import pymysql

def main(project, conn):

	sql = """select datetime, r.userid, r.filename, state, rawtype_container, rawtype_type, container_granularity
		from revisions r, rawtypes t where r.project = t.project and r.project = '%(project)s'
		and r.transactionid = t.revision and r.filename = t.filename""" % locals()
	print sql
	cursor = conn.cursor()
	cursor.execute(sql)
	timeHash = {}
	for time, user, filename, state, container, type, container_granularity in cursor:
		if container_granularity == "name_only":
			continue
		if not timeHash.has_key(time):
			timeHash[time] = []
		timeHash[time].append( (filename, user, state, container, type, "raw") )
	
	
	sql = """ select datetime, r.userid, r.filename, state, container, class_type, container_granularity
		from revisions r, parameterized_types t where r.project = t.project and r.project = '%(project)s'
		and r.transactionid = t.revision and r.filename = t.filename""" % locals()
	print sql
	cursor = conn.cursor()
	cursor.execute(sql)
	for time, user, filename, state, container, type, container_granularity in cursor:
		if container_granularity == "name_only":
			continue
		if not timeHash.has_key(time):
			timeHash[time] = []
		timeHash[time].append( (filename, user, state, container, type, "parameterized") )

	times = timeHash.keys()
	times.sort()

	print times

	fd = open(project + "-users.tsv", "w")
	print >> fd, "datetime\tuser\trtypes\tptypes\tcum_rtypes\tcum_ptypes"
	files = {}
	lastRawTypes = 0
	lastParameterizedTypes = 0
	authors = {}
	for time in times:
		print time
		for file in [x[0] for x in timeHash[time]]:
			files[file] = []
		theUser = None
		for filename, user, state, container, type, kind in timeHash[time]:
			theUser = user
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
		if not authors.has_key(theUser):
			authors[theUser] = []
		deltaRawTypes = totalRawTypes - lastRawTypes
		deltaParameterizedTypes = totalParameterizedTypes - lastParameterizedTypes

	
		lastUserRawTypes = 0
		lastUserParameterizedTypes = 0
		if len(authors[theUser]) > 0:
			junk, lastUserRawTypes, lastUserParameterizedTypes = authors[theUser][-1]
		authors[theUser].append( (time, lastUserRawTypes + deltaRawTypes,
			lastUserParameterizedTypes + deltaParameterizedTypes) )

		print "total raw types at time", time, "is", totalRawTypes
		print "total parameterized types at time", time, "is", totalParameterizedTypes
		print >> fd, str(time)+"\t"+theUser+"\t"+str(deltaRawTypes)+"\t"+str(deltaParameterizedTypes) \
			+ "\t" + str(authors[theUser][-1][1]) + "\t" + str(authors[theUser][-1][2])
		lastRawTypes = totalRawTypes
		lastParameterizedTypes = totalParameterizedTypes

	
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

