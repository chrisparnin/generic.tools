import sys, os
print os.path.abspath(__file__)
libDir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "lib")
sys.path.append(libDir)
import pymysql

def convert(s):
	if type(s) == str or type(s) == unicode:
		return s
	return str(s)
	try:
		return str(s)
	except UnicodeDecodeError, e:
		print e
		return "unicode-error"

#example of a valid connectionString is:
#host=eb2-2291-fas01.csc.ncsu.ed;user=bird;passwd=somepasswd;db=generics;port=4747
def foo(user, passwd, project):
	options = {}
	# connect to the database
	sys.stdout.write("connecting..." )
	sys.stdout.flush()
	conn = pymysql.connect(host="eb2-2291-fas01.csc.ncsu.edu", db="generics", port=4747,
		user=user, passwd=passwd)
	print "done."
	# execute the query
	cur = conn.cursor()
	for table in """revisions casts casts_changed annotations halstead parameterized_types
		parameterized_types_changed rawtypes rawtypes_changed parameterized_declarations
		parameterized_declarations_changed""".split():
		sql = "delete from %s where project = '%s'" % (table, project)
		print sql
		cur.execute(sql)
	cur.close()
	conn.close()

if __name__ == "__main__":
	if len(sys.argv) != 4:
		print >> sys.stderr, "must pass: username, passwd, project"
		print >> sys.stderr, "on command line"
		sys.exit(1)
	foo(*sys.argv[1:4])
