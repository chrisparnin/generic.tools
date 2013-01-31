# vi:noexpandtab:tabstop=4:shiftwidth=4
import os
import datetime
#from study_cvs_process import ParseCVSLogContents 

#jdbc = "'jdbc:mysql://chimp.cs.ubc.ca:4747/generics?user=chris&password=capcom'"
#jdbc = "'jdbc:mysql://localhost/generics?user=root&password=20pastnoon'"


logParser = "java -classpath ../../scmlogparsers/build/jar/logparsers.jar  edu.ucdavis.cssr.logparsers.FastRevisionCVSLogParser"

genericsConnector = "java -classpath GenericsConnector/build/jar/genericsconnector.jar com.ninlabs.GetTransactions"

class Transaction:

	def __init__(self, revs, id ):
		self.revs = revs
		self.id = id

	def __str__(self):
		return str(self.id)

	def __getitem__(self,idx):
		return self.revs[idx]

	def getEarlyDate(self):
		_,_,e,_ = self.revs[0]
		for _,_,r,_ in self.revs[1:]:
			if e > r:
				e = r
		return e
	
	def getLateDate(self):
		_,_,l,_ = self.revs[0]
		for _,_,r,_ in self.revs[1:]:
			if l < r:
				l = r
		return l

def upload_revisions( project, module, logPath, jdbc ):
	jdbc = "'" + jdbc +"'"
	args = " '" + project + "' '" + module + "' '" + logPath + "' "
	cmd = logParser + args + jdbc
	print cmd
	status = os.system(cmd)
	# check to see if it failed
	if status << 8:
		raise Exception("Error uploaded revisions with command: " + cmd)

class CvsDriver:
	def __init__(self):
		self.modules = None

	def log(self, base, root, jdbc, skipCommits=False):
		self.modules = cvs_list_modules(root)
		self.base = base
		self.logs = {}

		for m in self.modules:
			modulePath = self._getLogPath(m)
			uploadedModulePath = modulePath + ".uploaded"
			if not os.path.exists(modulePath):
				log = cvs_log( root, m )
				#cache
				f = open(modulePath,'w')
				f.write(log)
				f.close()
	
			
			# only hit db if not file exists
			if not os.path.exists(uploadedModulePath) and not skipCommits:
				upload_revisions(base,m,modulePath, jdbc)
				#just touch the file so we know it was uploaded
				open(uploadedModulePath, "w").close()

			
			#f = open(modulePath,'r')
			#self.logs[m] = f.readlines()
			#f.close()

	def _getLogPath( self, m ):
		return os.path.join(self.base,self.base+"."+m+".cvslog")	

	def getLatestRevisionDate(self,module,a):
		return a.getLateDate()
	
	def getEarliestRevisionDate(self,module,a):
		return a.getEarlyDate()

	def init(self, root, jdbc):
		if self.modules == None:
			self.modules = cvs_list_modules(root)
		self.transactions = {}
		print "creating transactions"
		for m in self.modules:
			trans = cvs_createtransactions2( self.base, m, jdbc )
			objs = []
			for t in trans:
				if len(t) == 0:
					continue
				id, _f, _d, state = t[0]
				objs.append( Transaction(t,id) )

			self.transactions[m] = objs

		print "transactions",len(self.transactions)

	def getTransactions(self,root):
		if self.modules == None:
			self.modules = cvs_list_modules(root)
		trans = []
		for m in self.modules:
			for x in range(len(self.transactions[m])-1):
				t = self.transactions[m][x]
				aDate = t.getEarlyDate()
				bDate = self.transactions[m][x-1].getLateDate()
				trans.append( (str(t.id),m,aDate,bDate) )
		return trans

	def summarize(self,root, t, module):
		#bef = datetime.strptime(before.getLateDate(),'%Y-%m-%d %H:%M:%S')

		files = []
		for _,file,_,state in self.transactions[module][int(t)].revs:
			path = module + formatFile(root,file)
			state = state[0].upper()
			files.append( state + " " + path)
		return str.join("\n",files)

	def batchexport( self, filesToExport ):

		batchFiles = []
		for f in filesToExport:
			# remove module and append transaction base.
			root, base, rev, path, date = f
			dest = base +"/"+ str.join("/",path.split("/")[1:])
			baseDest = os.path.dirname(dest)

			# clear the way
			if not os.path.exists(baseDest):
				os.makedirs(baseDest)

			batchFiles.append( (root, path, date, dest) )

		cvs_batch_export( batchFiles )

	def export( self, root, base, rev, path, date ):

		# remove module and append transaction base.
		dest = base +"/"+ str.join("/",path.split("/")[1:])
		baseDest = os.path.dirname(dest)

		# clear the way
		if not os.path.exists(baseDest):
			os.makedirs(baseDest)

		cvs_export( root, path, date, dest) 

def cvs_list_modules( root ):
	base = 'cvs -d:local:' + root
	query = base + " rls "#2>/dev/null"
	print query
	mods = os.popen(query).read()
	return filter( lambda m: m != "CVSROOT" and m!="" , mods.split("\n"))

def cvs_log( root, module ):
	base = 'cvs -d:local:' + root 
	query = base + " rlog '" + module + "' 2>/dev/null"
	print query
	return os.popen(query).read();

def cvs_summarize(root, beforeDate, afterDate, module):
	base = 'cvs -d:local:' + root

	query = base + " rdiff -s -D \"" + beforeDate + "\" -D \"" + afterDate +"\"" + " " + module + " 2>/dev/null"
	print query 
	result = os.popen(query).read()
	results = []
	for line in result.split("\n"):
		if line == "":
			continue	
		results.append(_formatLine(line))
	return str.join("\n",results)


def _formatLine( line ):
	state = "M"
	if line.find("is removed;") > 0:
		state = "D"
	if line.find("is new;") > 0:
		state = "A"

	return state + " " + line.split(" ")[1]


#RCS file: /home/cp125/downloads/azureus/azureus1/org/gudy/../
def formatFile(root,path):
	clean = path.replace("RCS file: ","")
	return clean.replace(",v","").replace(root,"")

def cvs_batch_export( batchFiles ):

	commands = []
	for b in batchFiles:
		root, path, date, dest = b
		base = 'cvs -d:local:' + root 
		command = " co -D '" + str(date) + "' -p " + formatFile(root,path) + " > '" + dest + "' 2> /dev/null"
		query = base + command 
		commands.append( query )

	superCommand = str.join(";",commands)
	status = os.system(superCommand)
	print "checked out ", len(commands), "files"
	# check to see if it failed
	if status << 8:
		raise Exception("Error checking out files: " + superCommand)
	

def cvs_export(root, path, date, dest):
	base = 'cvs -d:local:' + root 

	command = " co -D '" + str(date) + "' -p " + formatFile(root,path) #+" 2>/dev/null"
	query = base + command 
	print query
	contents = os.popen(query).read();
	f = open(dest,"w")
	f.write(contents)
	f.close()
	return contents

def cvs_createtransactions2( project, m, jdbc, threshold=datetime.timedelta(minutes=5) ):
	args = " '" + project + "' '" + m+ "' "
	jdbc = "'" + jdbc +"'"
	cmd = genericsConnector + args + jdbc
	print cmd
	fd = os.popen(cmd)
	contents = fd.read();
	if fd.close():
		raise Exception("command failed: " + cmd)
	lines = contents.split('\n')

	transactions = []
	t = lines[0].split(';')[0]
	group = []
	for line in lines:
		if line == "":
			continue
		print line
		id,file,date,state = line.split(';')
		#print id,file,date 
		if not t == id:	
			transactions.append(group)
			group = []
		group.append((id,file,date,state))
		t = id
	# last group
	transactions.append(group)
	return transactions

if __name__ == "__main__":
	import sys

	#print cvs_summarize("/home/cp125/downloads/azureus/", "1/1/2007", "2/1/2007","azureus2")

	#cvs_createtransactions( "azureus.azureus3.log" )

	#driver = CvsDriver()
	#driver.log( "azureus", "/home/cp125/downloads/azureus/"  )
	#driver.init("/home/cp125/downloads/azureus/")


	print cvs_createtransactions2( 'freemind', 'Freemind' )
	

	#cvs_export( "/home/cp125/downloads/azureus", "azureus2/com/aelitis/azureus/core/dht/impl/Test.java", "1.77", "Test.java") 

	#cvs -d:local:/home/cp125/downloads/azureus/ rdiff -s  -D "1/1/2007" -D "2/1/2007" -W "*.java" azureus2  > out.txt

#cvs -d:local:/home/cp125/downloads/azureus/ co -r 1.77 -p azureus2/com/aelitis/azureus/core/dht/impl/Test.java > Test.java
