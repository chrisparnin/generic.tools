#!/net/hc293/vector/local/bin/python
import os

def verify( path, logFile, diffPath ):

	print "verifying", logFile, "downloaded with", diffPath
	#1 get revision pairs
	f = open(logFile,'r')
	data = f.readlines()
	f.close()
	pairs = getPairs(data)
	
	#2 For each pair
	print "pairs",len(pairs)

	for after,before in pairs:
		#3 Get Diff files
		#foo/.diff/2760-2759.diff	
		dPath = os.path.join(diffPath, after + "-" + before + ".diff")
		if not os.path.exists( dPath ):
			print "No diffpath exists", dPath
			continue

		f = open(dPath,'r')
		diffs = f.readlines()
		f.close()
		
		afterChanges = getChangedFiles(diffs,"D",".java")
		beforeChanges = getChangedFiles(diffs,"A",".java")

		#4 Make sure number of exported files (with size > 0)
		nameAfter = after + "-" + before + "-after"
		nameBefore = after + "-" + before + "-before"
		
		filesAfter = getFilesInRev( os.path.join(path,nameAfter) )
		filesBefore = getFilesInRev( os.path.join(path,nameBefore) )
		
		#print len(afterChanges),len(beforeChanges),len(filesAfter),len(filesBefore)	
	
		if len(filesAfter) != len(afterChanges):
			print "Missing files for revision: after", after, len(afterChanges),len(afterChanges) - len(filesAfter),printMissing(filesAfter,afterChanges)
		
		if len(filesBefore) != len(beforeChanges):
			print "Missing files for revision: before", before, len(beforeChanges),len(beforeChanges) - len(filesBefore),printMissing(filesBefore,beforeChanges)

def printMissing(files,changes):
	prefix = "https://squirrel-sql.svn.sourceforge.net/svnroot/squirrel-sql/trunk/sql12/"
	a = {}
	for f in files:
		a[f] = f
	missing = []
	for c in changes:
		x = os.path.basename(c[1].replace(prefix,""))
		if not a.has_key(x):
			missing.append(x)
	return missing
def getChangedFiles(changes,excludeStatus, includeExt):
	filteredFiles = []
	for ch in changes:
		ch = ch.strip()
		if ch == "":
			continue
		tuple = [ s for s in ch.split(' ') if s]
		status = tuple[0]
		changeFile = tuple[1].strip()
		if not (status == excludeStatus) and changeFile.endswith(includeExt):
			filteredFiles.append(tuple)
	return filteredFiles

def getFilesInRev( path ):
	allFiles = []
	for root, dirs, files in os.walk(path):
		allFiles += [ f for f in files if f.endswith(".java") ]
	return allFiles	

def getPairs(log):
	rev_delim = '-------------------------------------------'
	state = 'START'
	revs = []
	for line in log:
		if state == 'START' and line.startswith(rev_delim) :
			state = 'REVISION_START'

		elif state == 'REVISION_START':
			rev = processRevisionLine(line)
			revs.append(rev.strip('r '))
			state = 'COMMENTS'

		elif state == 'COMMENTS':
			# Ignoring comments for now
			if line.startswith(rev_delim):
				state = 'REVISION_START'
	pairs = []
	for x in range(len(revs)-1):
		pairs.append( (revs[x], revs[x+1]))
	return pairs
def processRevisionLine(line):
	try:
		rev, author, date, changes = line.split('|')
		return rev
	except:
		raise Exception("svn log file contents unexpected entries:"+line)

	
if __name__ == "__main__":
	import sys
	
	s = sys.argv[1]
	verify( s, os.path.join(s,s.strip('/')+".svnlog"), os.path.join(s,'.diff/') ) 
