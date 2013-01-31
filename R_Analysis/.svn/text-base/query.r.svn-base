

# if keepFile is true then the temporary file that holds the result of the query is not deleted
# if debug is true, the some additional output is printed
# the server, database, and port values here are defaults.  override them by specifying them
# by name when calling the function (e.g. executeMySqlQuery("select * from a", "cbird", "foo", server="localhost")
executeMySqlQuery = function(query, user, passwd, server="eb2-2291-fas01.csc.ncsu.edu", database="generics",
	port=4747, keepFile = 0, debug=0) {

	connectionString = sprintf("host=%s;db=%s;port=%i;user=%s;passwd=%s", server, database, port, user, passwd);

	# create a temp file with a base name
	filename = tempfile("r-query")

	# path to python query script
	queryPath = paste('"', file.path(getwd(), "mysqlquery.py"), '"', sep = "")
	queryPath = '"/Users/cabird/projects/generics/tools/R_Analysis/mysqlquery.py"'

	# try to be smart and look in the usual places for python
	# if R is not finding python on your system, add it's location here so it
	# will look for it
	candidates = c("c:\\cygwin\\bin\\python.exe",
		"/usr/bin/python", "/usr/local/bin/python")
	pythonPath = ""
	for (candidate in candidates) {
		if (file.exists(candidate)) {
			pythonPath = candidate
			break;
		}
	}
	if (pythonPath == "") {
		stop("Could not find python on your system, please add the correct path to 'candidates' in query.r")
	}
	pyPath = '"/usr/bin/python"'


	if (debug) {
		print(paste("the path to python is", pythonPath))
	}

	# quote things that may have spaces or special characters in them
	query = sprintf("\"%s\"", query)
	connectionString = sprintf("\"%s\"", connectionString)
	# set up the massive command line
	command = paste(pythonPath, queryPath, connectionString, query, filename)

	if (debug) {
		print("command is:")
		print(command)
	}

	# system executes the command and returns non-zero on error
	if (system(command)) {
		print("error executing python script... see output for details\n")
		return();
	}

	#if we were successful, then read the results back out	
	data = read.table(tempFile, header=1, sep="\t", stringsAsFactors=0)
	#now delete the temp file
	if (!keepFile) {
		unlink(filename)
	} else {
		print(paste("temp file is located at", filename))
	}
	return(data)
}
