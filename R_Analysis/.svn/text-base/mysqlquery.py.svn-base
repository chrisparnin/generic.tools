import sys, os
sys.path.append( os.path.join(os.path.dirname(__file__), "../lib"))
print sys.path
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
def foo(connectionString, query, filename):
	options = {}
	for option in connectionString.split(";"):
		if len(option) == 0:
			continue
		key, val = option.split("=")
		options[key] = val
		if key == "port":
			options[key] = int(val)
	# connect to the database
	sys.stdout.write("connecting with options %s..." % str(options))
	sys.stdout.flush()
	conn = pymysql.connect(**options)
	print "done."
	# execute the query
	cur = conn.cursor()
	cur.execute(query)
	# create output file
	outFd = open(filename, "w")
	# get the names of the columns to build the header
	# I like tab separated values since sometimes values have
	# commas in them
	print "there are %i fields" % len(cur.description)
	print ", ".join([x[0] for x in cur.description])
	header = "\t".join([x[0] for x in cur.description])
	print >> outFd, header
	# output the rows of the query
	for row in cur:
		outFd.write("\t".join([convert(x) for x in row]) + "\n")
	#clean up
	outFd.close()
	cur.close()
	conn.close()

if __name__ == "__main__":
	if len(sys.argv) != 4:
		print >> sys.stderr, "must pass: conection string, query, output file"
		print >> sys.stderr, "on command line"
		sys.exit(1)
	foo(*sys.argv[1:4])
