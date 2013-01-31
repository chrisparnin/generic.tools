#!/net/hc293/vector/local/bin/python
# vi:noexpandtab:tabstop=4:shiftwidth=4
import os, sys, glob, pickle

logParser = "java -classpath ../../scmlogparsers/build/jar/logparsers.jar edu.ucdavis.cssr.logparsers.GitLogParser"

genericsConnector = "java -classpath GenericsConnector/build/jar/genericsconnector.jar com.ninlabs.GetTransactions"

def upload_revisions(project, logPath, jdbc):
	jdbc = "'" + jdbc +"'"
	args = " '" + project + "' '" + logPath + "' "
	cmd = logParser + args + jdbc
	print cmd	
	fd = os.popen(cmd)
	print fd.read()
	status = fd.close()
	if status:
		raise Exception("Error uploading revisions data to " + jdbc)

class GitDriver:

	def __init__(self):
		pass

	def log(self,base,root,jdbc, skipCommits):
		self.base = base
		logPath = os.path.join(base,base+".gitlog")
		uploadedFlagPath = os.path.join(base,base+".gitlog.uploaded")
		if not os.path.exists(logPath):
			data = git_log(root)
			f = open(logPath,'w')
			f.write(data)
			f.close()

		if not os.path.exists(uploadedFlagPath) and not skipCommits:
			upload_revisions(base, logPath, jdbc )
			open(uploadedFlagPath, "w").close()

		f = open(logPath,'r')
		self.logFile = f.readlines()
		f.close()

	#def export(self,root,path,rev,dest):
	def export( self, gitRoot, base, transaction, path, version ):
		newBase = base

		print "gitRoot", gitRoot
		print "base", base
		print "path", path

		# remove svn root from path
		# i.e. https://squirrel-sql.svn.sourceforge.net/../fw/blah/stuff.java
		repoPath = path.replace(gitRoot,"")

		baseDest = os.path.join(base,os.path.dirname(repoPath))
		dest = os.path.join(base, repoPath)

		# clear the way
		if not os.path.exists(baseDest):
			os.makedirs(baseDest)

		revision = self.transactionToRevision[transaction]

		# this will create file on disk
		git_export(gitRoot, repoPath, revision, dest)


	def batchexport( self, filesToExport ):
		paths = []
		for f in filesToExport:
			gitRoot,newBase,transaction,path,version = f
			self.export( gitRoot, newBase, transaction, path, version )


	def summarize(self,root,transaction,module):
		files = []
		revs = self.transactions[transaction]
		for _, revision, file, _, state in revs:
			if file[0] == "/":
				path = root + file[1:]
			else:
				path = root + file
			state = state[0].upper()
			files.append(state + " " + path)
		return str.join("\n", files)


	def init(self,root,jdbc):
		self.transactions = getRevisionsFromDB(self.base, "", jdbc)
		self.transactionToRevision = {}
		for tran, changes in self.transactions.items():
			revision = changes[0][1]
			self.transactionToRevision[tran] = revision

	def getLatestRevisionDate(self, module, rev):
		return None
	def getEarliestRevisionDate(self,module,rev):
		return None

	def getTransactions(self,root):
		trans = map(int,self.transactions.keys())
		trans.sort()
		return [(str(t),"",None,None) for t in trans]

def getRevisionsFromDB( project, m, jdbc ):
	dataFile = project + "-connector.data"
	if os.path.exists(dataFile):
		contents = open(dataFile).read()
	else:
		args = " '" + project + "' '" + m+ "' "
		jdbc = "'" + jdbc +"'"
		cmd = genericsConnector + args + jdbc + " --git"
		print cmd
		fd = os.popen(cmd)
		contents = fd.read();
		dataFile = open(dataFile, "w")
		dataFile.write(contents)
		dataFile.close()
		if fd.close():
			raise Exception("command failed: " + cmd)
	lines = contents.split('\n')

	transactions = {}
	transaction = lines[0].split(';')[0]
	lastTransaction = transaction
	group = []
	for line in lines:
		if line == "":
			continue
		transaction,revision,file,date,state = line.split(';')
		if not transaction == lastTransaction:
			transactions[transaction] = group
			group = []
		group.append((transaction,revision,file,date,state))
		lastTransaction = transaction
	# last group
	transactions[transaction] = group
	print map(int, transactions.keys())
	return transactions

def git_log(root):
	command = "git --git-dir=" + root + "/.git log --reverse "
	command += " --date=iso --name-status -M -C " 
	command +=	" --pretty=format:\"__START_GIT_COMMIT_LOG_MSG__%n" 
	command += "revision: %H%ncommitter: %cn%ndate: %ci%nparent: %P%n%s%n%b%n" 
	command += "__END_GIT_COMMIT_LOG_MSG__\""
	print command
	return os.popen(command).read();


def git_summarize(root, before, after):
	#add some formatting to this
	query = 'git log ' + git_findrev(before) + ".." + git_findrev(after) + " --name-status --format=%H"
	print query
	text = os.popen(query).read();
	output = ""
	for line in text.split("\n"):
		if "\t" in line:
			output += line.replace("\t", " ")+"\n"
	return output

def git_export(gitRoot, path, rev, dest):
	# append @rev to make sure peg revision is specified.
	if os.path.exists(dest) and os.stat(dest).st_size != 0:
		print dest, "already exists, skipping."
		return
	command = "git --git-dir='" + gitRoot + "/.git' show " + rev + ":" + path  +  " > " + dest
	query = command 
	print query
	return os.popen(query).read();

### Functions below here are for svn repos converted to git.
### For now we don't need them

def git_findrev(rev):
	return revMap[int(rev)]
	command = 'git svn find-rev r' + rev
	return os.popen(command).read().strip();

#mapping between git sha1's and svn revs
# open a .rev_map file and return a mapping of sha1 hashes to svn revisions
def readRevs(filename):
	print "reading %s" % filename
	sha1Map = {}
	revMap = {}
	data = map(ord, open(filename, "r+").read())
	for i in range(len(data)/24):
		(rev, sha1) = getRev(data[i*24:(i+1)*24])
		sha1Map[sha1] = rev
		revMap[rev] = sha1
	return (sha1Map, revMap)

#given 24 bytes of data, return a tuple of (svn revision, sha1)
def getRev(data):
	revision = (data[0] << 24) + \
		(data[1] << 16) + (data[2] << 8) + data[3]
	sha1 = "".join(["%02x" % data[x] for x in range(4, 24)])
	return (revision, sha1)

#called by os.path.walk, this will read all the .rev_map files in the 
#directory being walked and update the sha1 map with the data in the files
def revWalker(runningSha1Map, dir, filenames):
	for filename in filenames:
		if ".rev_map" in filename:
			path = os.path.join(dir, filename)
			runningSha1Map.update(readRevs(path))

#recursively walk the directory given, reading .rev_map files
def traverse(dir):
	sha1Map = {}
	os.path.walk(dir, revWalker, sha1Map) 
	return sha1Map


