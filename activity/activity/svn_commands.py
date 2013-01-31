#!/net/hc293/vector/local/bin/python
# vi:noexpandtab:tabstop=4:shiftwidth=4
import os

logParser = "java -Xmx1024m -classpath ../../scmlogparsers/build/jar/logparsers.jar edu.ucdavis.cssr.logparsers.RevisionSVNLogParser"

genericsConnector = "java -classpath GenericsConnector/build/jar/genericsconnector.jar com.ninlabs.GetTransactions"

def upload_revisions( project, module, logPath, jdbc ):
	jdbc = "'" + jdbc +"'"
	args = " '" + project + "' '" + module + "' '" + logPath + "' "
	cmd = logParser + args + jdbc
	print cmd	
	fd = os.popen(cmd)
	print fd.read()
	status = fd.close()
	if status:
		raise Exception("Error uploading revisions data to " + jdbc)

class SvnDriver:

	def __init__(self):
		pass

	def log(self,base,root,jdbc,skipCommits=False):
		self.base = base
		logPath = os.path.join(base,base+".svnlog")
		logXmlPath = os.path.join(base,base+".svnxml")
		uploadedFlagPath = os.path.join(base,base+".snvxml.uploaded")
		if not os.path.exists(logPath):
			data = svn_log(root)
			f = open(logPath,'w')
			f.write(data)
			f.close()

		# upload
		if not os.path.exists(logXmlPath):
			data = svn_xml_log(root)
			f = open(logXmlPath,'w')
			f.write(data)
			f.close()

		if not os.path.exists(uploadedFlagPath) and not skipCommits:
			upload_revisions(base, "", logXmlPath, jdbc )
			open(uploadedFlagPath, "w").close()

		f = open(logPath,'r')
		self.logFile = f.readlines()
		f.close()

	def batchexport( self, filesToExport ):

		paths = []

		for f in filesToExport:
			svnroot,newBase,rev,path,version = f
			self.export( svnroot, newBase, rev, path, version )

	def export( self, svnroot, base, rev, path, version ):
		newBase = base

		# remove svn root from path
		# i.e. https://squirrel-sql.svn.sourceforge.net/../fw/blah/stuff.java
		relPath = path.replace(svnroot,"")
		if not relPath[0] == "/":
			relPath = "/" + relPath

		baseDest = newBase + os.path.dirname(relPath)
		dest = newBase + relPath

		# clear the way
		if not os.path.exists(baseDest):
			os.makedirs(baseDest)

		# this will create file on disk
		svn_export(path,rev,dest)

	def summarize(self,root,t,module):
 		files = []
		revs = self.transactions[t]
		for _,file,_,state in revs:
			if file[0] == "/":
				path = os.path.join(root, file[1:])
			else:
				path = os.path.join(root, file)
			state = state[0].upper()
			files.append( state + " " + path)
		return str.join("\n",files)

	def init(self,root,jdbc):
		self.transactions = getRevisionsFromDB( self.base, "", jdbc )

	def getLatestRevisionDate(self, module, rev):
		return None
	def getEarliestRevisionDate(self,module,rev):
		return None

	def getTransactions(self,root):
		trans = map(int,self.transactions.keys())
		trans.sort()
		return [(str(t),"",None,None) for t in trans]

def getRevisionsFromDB( project, m, jdbc ):
	args = " '" + project + "' '" + m+ "' "
	jdbc = "'" + jdbc +"'"
	cmd = genericsConnector + args + jdbc
	print cmd
	fd = os.popen(cmd)
	contents = fd.read();
	if fd.close():
		raise Exception("command failed: " + cmd)
	lines = contents.split('\n')

	transactions = {}
	t = lines[0].split(';')[0]
	group = []
	for line in lines:
		if line == "":
			continue
		id,file,date,state = line.split(';')
		if not t == id:
			transactions[t] = group
			group = []
		group.append((id,file,date,state))
		t = id
	# last group
	transactions[t] = group
	return transactions

def svn_log( root ):
	command = 'svn log '
	query = command + root
	print query
	return os.popen(query).read();

def svn_xml_log( root ):
	command = 'svn log --xml -v '
	query = command + root
	print query
	return os.popen(query).read();

def svn_summarize(root, before, after):
	command = 'svn diff  --summarize -r ' + before + ':' + after + ' '
	query = command + root
	print query
	return os.popen(query).read();

def svn_batch_export( paths ):
	commands = []
	for p in paths:	
		path, rev, dest = p
		if os.path.exists(dest) and os.stat(dest).st_size != 0:
			continue	
		command = 'svn export -r ' + rev + " " + path + "@"+ rev  +  " '" + dest +"'"
		commands.append( command )	

	bigCommand = str.join(";",commands)
	return os.popen(bigCommand).read();

def svn_export(path, rev, dest):
	# append @rev to make sure peg revision is specified.
	if os.path.exists(dest) and os.stat(dest).st_size != 0:
		print dest, "already exists, skipping."
		return

	command = 'svn export -r ' + rev + " " + path + "@"+ rev  +  " " + dest
	query = command 
	print query
	return os.popen(query).read();
	
if __name__ == "__main__":
	import sys
	toGet = sys.argv[1]
	#svn_log(toGet);
	print svn_summarize(toGet,'2000','2001')

