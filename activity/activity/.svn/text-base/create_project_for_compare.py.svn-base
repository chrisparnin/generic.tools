import os
import string
import time


from cvs_commands import *
from svn_commands import *
from git_commands import *

def workflow(root,base,driver,includeExt,jdbc,skip):

	# 1) Prepare Directory (a/b)
	#base = os.path.basename(root)
	if not os.path.exists(base):
		os.mkdir(base)

	# 2) Save Log
	driver.log(base,root,jdbc, skipCommits=skip)

	# Any init from log
	driver.init(root,jdbc)

	# 3) Get Transactions (from db)
	trans = driver.getTransactions(root)

	if not os.path.exists(os.path.join(base,".diff/")):
		os.mkdir(os.path.join(base,".diff/"))

	# 4) Checkout transactions files
	# 8-before files
	# 8-after  files
	print len(trans)
	for t,module,aDate,bDate in trans:
		diffPath = os.path.join(base,".diff/"+ module + '-'+ str(t) + ".diff")
		print diffPath
		if not os.path.exists(diffPath) or os.stat(diffPath).st_size == 0:
			data = driver.summarize(root,t,module)
			f = open(diffPath,'w')
			f.write(data)
			f.close()

		# get cached diffs 
		f = open(diffPath,'r')
		changes = f.readlines()
		f.close()
		
		filteredFiles = getChangedFiles(changes,includeExt)

		print t, "changed files (",len(filteredFiles),")","*"+includeExt

		
		#aDate = driver.getLatestRevisionDate(module,t)
		#bDate = driver.getEarliestRevisionDate(module,t)

		#if aDate <= bDate and not aDate == bDate == None:
			#raise "invalid date state " + str(aDate) + " " + str(bDate)
		#	print "Warning: same dates (may be only one transaction) " + str(aDate) + "->" + str(bDate)

		afterRevFiles = []
		for state, path in filteredFiles:
			exportBase=os.path.join(base,module+"___"+str(t)+"___after")
			if state[0] == "D":
				relpath = path.replace(root,"")
				if not relpath[0] == "/":
					relpath = "/" + relpath
				dir = os.path.dirname(exportBase + relpath)
				if not os.path.exists(dir):
					os.makedirs(dir)
				continue
			afterRevFiles.append( (root, exportBase, t, path, aDate ) )

		driver.batchexport( afterRevFiles )

		# before
		beforeRevFiles = []
		for state, path in filteredFiles:
			exportBase=os.path.join(base,module+"___"+str(t)+"___before")
			#if the mode is "A" then the file was created in this revision, so for
			#the "before" version, create the directory
			if state[0] == "A":
				relpath = path.replace(root,"")
				if not relpath[0] == "/":
					relpath = "/" + relpath
				dir = os.path.dirname(exportBase + relpath)
				if not os.path.exists(dir):
					os.makedirs(dir)
				continue
			beforeRevFiles.append( (root, exportBase, str(int(t)-1), path, bDate ) )
		driver.batchexport( beforeRevFiles )


def getChangedFiles(changes,includeExt):
	filteredFiles = []

	for ch in changes:
		ch = ch.strip()
		if ch == "":
			continue
		tuple = [ s for s in ch.split(' ') if s]
		try:
			changedFile = tuple[1]
		except:
			raise str(tuple)
		if changedFile.endswith( includeExt ):
			filteredFiles.append(tuple)
	return filteredFiles	

if __name__ == "__main__":
	import sys
	
	if len(sys.argv) < 5 or len(sys.argv) > 6:
		print "must execute with 4 arguments"
		print sys.argv[0] + "<cvs root> <project> <repo driver> <jdbc> <skip committing>"
		sys.exit(1)

	if sys.argv[3]	== "svn":
		print "svn driver"
		driver = SvnDriver()

	if sys.argv[3] == "cvs":
		print "cvs driver"
		driver = CvsDriver()

    # using this to get lucene imported
	if sys.argv[3] == "git":
		print "git driver"
		driver = GitDriver()

	jdbc = sys.argv[4]

	if len(sys.argv) == 6:
		skip = sys.argv[5]
		if skip.lower() in ["true", "yes", "1"]:
			skip = True
		elif skip.lower() in ["false", "no", "0"]:
			skip = False
		else:
			print >> sys.stderr, "Don't understand argument", skip, "for skip option.  Valid choices are true, false, yes, no, 1, or 0"
	else:
		skip = False
	

	workflow(sys.argv[1],sys.argv[2],driver,".java",jdbc, skip)
