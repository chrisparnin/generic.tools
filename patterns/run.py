# vi:noexpandtab:tabstop=4:shiftwidth=4
import os, sys, re, socket, time
	
from projects_in import projects
from natsort import natsorted
import platform


def process(prjs, shape, outputFile):

	for p in prjs:
		if platform.system() == "Darwin" or platform.system() == "Linux":
			p = os.path.abspath(p)
		outputFile = open(p + '.' + shape,'w')
		print "Processing",p,shape
		findPattern(p,shape,outputFile)
		outputFile.close()


def tabSeparatedData(*list):
	return "\t".join([str(x) for x in list])


#define one of these for each pattern. they handle all the things that differ between
#patterns/visitors
class PatternInfo:
	# by default we run patterns with two granularities of container names:
	# the full signature and the name only.  Override this in a subclass to change it.
	containerGranularities = ["name_only", "full"]
	def outputData(self, project, module, revision, token, containerGranularity, output):
		raise Exception("outputData not implemented in PatternInfo class")

class CastsPattern(PatternInfo):
	def __init__(self):
		self.patternName = "castsparams"
		self.table = "casts"
		self.fields = "container container_granularity type count".split()
		self.changedFields = "added container container_granularity type".split()
		self.doChanged = True
		
	def outputData(self, project, module, revision, tokens, containerGranularity, output):
		# tokens for this pattern are of the form:
		# line:container:type
		for filename in tokens.keys():
			for (container, type), count in self.getCountSet(tokens[filename]).items():
				print >> output, tabSeparatedData(project, module, filename, revision, container, containerGranularity, type, count)

	def outputDiffData(self, project, module, revision, diffFiles, containerGranularity, output):
		for kind, filename, info in diffFiles:
			if kind == "M" or kind == "A":	
				for container, addedOrDeleted, type in info:
					if addedOrDeleted[0] == "-":
						added = "0"
					elif addedOrDeleted[0] == "+":
						added = "1"
					else:
						raise Exception("don't know how to handle addedOrDeleted = '" + addedOrDeleted + "'")
					print >> output, tabSeparatedData(project, module, filename, revision, added, container, containerGranularity, type)


	def getCountSet(self, lines):
		set = {}
		for lineNum, container, type in [line.strip().split(":") for line in lines]:
			set[container, type] = set.get((container,type), 0) + 1
		return set

	def getSetForDiff(self, lines):
		set = {}
		for lineNum, container, type in [line.strip().split(":") for line in lines]:
			set[container] = set.get(container, {})
			set[container][type] = set[container].get(type, 0) + 1
		return set

class RawTypesPattern(PatternInfo):
	def __init__(self):
		self.patternName = "rawtypes"
		self.table = "rawtypes"
		self.fields = "rawtype_linenumber rawtype_container container_granularity rawtype_type".split()
		self.changedFields = "added container container_granularity type".split()
		self.doChanged = True

	def outputData(self, project, module, revision, tokens, containerGranularity, output):
		# tokens for this pattern are of the form:
		# linenumber:container:type
		for filename in tokens.keys():
			print tokens[filename]
			for lineNumber, container, type in [ x.strip().split(":") for x in tokens[filename]]:
				print >> output, tabSeparatedData(project, module, filename, revision, lineNumber, container, containerGranularity, type)

	def outputDiffData(self, project, module, revision, diffFiles, containerGranularity, output):
		for kind, filename, info in diffFiles:
			if kind == "M" or kind == "A":	
				for container, addedOrDeleted, type in info:
					if addedOrDeleted[0] == "-":
						added = "0"
					elif addedOrDeleted[0] == "+":
						added = "1"
					else:
						raise Exception("don't know how to handle addedOrDeleted = '" + addedOrDeleted + "'")
					print >> output, tabSeparatedData(project, module, filename, revision, added, container, containerGranularity, type)


	def getSetForDiff(self, lines):
		set = {}
		for lineNum, container, type in [line.strip().split(":") for line in lines]:
			set[container] = set.get(container, {})
			set[container][type] = set[container].get(type, 0) + 1
		return set

class ParameterizedTypePattern(PatternInfo):
	def __init__(self):
		self.patternName = "parameterizedtypes"
		self.table = "parameterized_types"
		self.fields = "container container_granularity class_type type_args count".split()
		self.changedFields = "added container container_granularity class_type type_args".split()
		self.doChanged = True

	def outputData(self, project, module, revision, tokens, containerGranularity, output):
		for filename in tokens.keys():
			for key, count in self.getCountSet(tokens[filename]).items():
				container, classType, typeArgs = key
				print >> output, tabSeparatedData(project, module, filename, revision, container, containerGranularity, classType, typeArgs, count)
			
	def outputDiffData(self, project, module, revision, diffFiles, containerGranularity, output):
		for kind, filename, info in diffFiles:
			if kind == "M" or kind == "A":	
				for container, addedOrDeleted, (classType, typeArgs) in info:
					if addedOrDeleted[0] == "-":
						added = "0"
					elif addedOrDeleted[0] == "+":
						added = "1"
					else:
						raise Exception("don't know how to handle addedOrDeleted = '" + addedOrDeleted + "'")
					print >> output, tabSeparatedData(project, module, filename, revision, added, container, containerGranularity, classType, typeArgs)

	def getCountSet(self, lines):
		set = {}
		for line, container, classType, typeArgs in [line.strip().split(":") for line in lines]:
			key = (container, classType, typeArgs)
			set[key] = set.get(key, 0) + 1
		return set
	
	def getSetForDiff(self, lines):
		set = {}
		for lineNum, container, classType, typeArgs in [line.strip().split(":") for line in lines]:
			set[container] = set.get(container, {})
			set[container][classType, typeArgs] = set[container].get((classType, typeArgs), 0) + 1
		return set

class ParameterizedDeclaration(PatternInfo):
	def __init__(self):
		self.patternName = "parameterizeddeclarations"
		self.table = "parameterized_declarations"
		self.fields = "kind class_type type_args".split()
		self.changedFields = "added kind class_type type_args".split()
		self.doChanged = True
		self.containerGranularities = ["full"]

	def outputData(self, project, module, revision, tokens, containerGranularity, output):
		for filename, lines in tokens.items():
			for key, value in self.getCountSet(lines).items():
				print filename,revision,lines
				entityKind, classType, typeArgs = key
				print >> output, tabSeparatedData(project, module, filename, revision, entityKind, classType, typeArgs)

	def outputDiffData(self, project, module, revision, diffFiles, containerGranularity, output):
		for kind, filename, info in diffFiles:
			if kind == "M" or kind == "A":	
				for entityKind, addedOrDeleted, (classType, typeArgs) in info:
					if addedOrDeleted[0] == "-":
						added = "0"
					elif addedOrDeleted[0] == "+":
						added = "1"
					else:
						raise Exception("don't know how to handle addedOrDeleted = '" + addedOrDeleted + "'")
					print >> output, tabSeparatedData(project, module, filename, revision, added, entityKind, classType, typeArgs)

	def getCountSet(self, lines):
		set = {}
		for line, entityKind, classType, typeArgs in [line.strip().split(":") for line in lines]:
			key = (entityKind, classType, typeArgs)
			set[key] = set.get(key, 0) + 1
		return set
	
	def getSetForDiff(self, lines):
		set = {}
		for lineNum, entityKind, classType, typeArgs in [line.strip().split(":") for line in lines]:
			set[entityKind] = set.get(entityKind, {})
			set[entityKind][classType, typeArgs] = set[entityKind].get((classType, typeArgs), 0) + 1
		return set

class Annotations(PatternInfo):
	def __init__(self):
		self.patternName = "annotations"
		self.table = "annotations"
		self.fields = "annotation_container container_granularity annotation_property annotation_type".split()
		self.doChanged = False

	def outputData(self, project, module, revision, tokens, containerGranularity, output):
		# there should be only one line per filename
		for filename, lines in tokens.items():
			for token in lines:	
				parent, prop, type = token.strip().split(":")
				print >> output, tabSeparatedData(project, module, filename, revision, parent, containerGranularity, prop, type)


class Halstead(PatternInfo):
	def __init__(self):
		self.patternName = "halstead"
		self.table = "halstead"
		self.fields = "distinct_operators distinct_operands total_operators total_operands".split()
		self.doChanged = False
		self.containerGranularities = ["full"]

	def outputData(self, project, module, revision, tokens, containerGranularity, output):
		# there should be only one line per filename
		for filename, lines in tokens.items():
			for token in lines:	
				n1, n2, N1, N2 = token.strip().split(":")
				print >> output, tabSeparatedData(project, module, filename, revision, n1, n2, N1, N2)

patterns = {
	"rawtypes": RawTypesPattern(),
	"castsparams": CastsPattern(),
	"parameterizedtypes": ParameterizedTypePattern(),
	"parameterizeddeclarations": ParameterizedDeclaration(),
	"halstead": Halstead(),
	"annotations": Annotations()
}
	

def findPattern(p,shape,outputFile):
	project = os.path.basename(os.path.abspath(p))
	shape = shape.lower()
	patternInfo = patterns[shape]
	# open the file that will have all of the inserts in it
	insertFilename = project + "-" + shape + "-inserts.sql"
	insertFile = open(insertFilename, "w")
	# add statements clearing any data for this pattern and project that may already
	# be in the table
	table = patternInfo.table
	
	print >> insertFile, "delete from %(table)s where project = '%(project)s';" % locals()
	print >> insertFile, ("""LOAD DATA LOCAL INFILE '%(project)s-%(table)s.data' INTO TABLE %(table)s
		FIELDS TERMINATED BY '\\t' (project, module, filename, revision, """ + ", ".join(patternInfo.fields) + ");") % locals()
	if patternInfo.doChanged:
		print >> insertFile, "delete from %(table)s_changed where project = '%(project)s';" % locals()
		print >> insertFile, ("""LOAD DATA LOCAL INFILE '%(project)s-%(table)s_changed.data' INTO TABLE %(table)s_changed
			FIELDS TERMINATED BY '\\t' (project, module, filename, revision, """ + ", ".join(patternInfo.changedFields) + ");") % locals()

	rawDataFile = open("%(project)s-%(table)s.data" % locals(), "w")
	changedDataFile = open("%(project)s-%(table)s_changed.data" % locals(), "w")

	modules = {}
	for dir in os.listdir(p):
		if dir.find('___') < 0:
			continue
		try:
			m, rev, state = dir.split('___')
			if not modules.has_key(m):
				modules[m] = {}
			modules[m][rev] = rev
		except Exception, e:
			print "Failed to unpack " + dir

	for m in modules.keys():
		revs = map(int,modules[m].keys())
		revs.sort()
		print "doing module", m, "revs", len(revs)
		for r in revs:
			# we want to do this when considering the container full signature (i.e. with parameters)
			# and also with only the container name	
			for containerGranularity in patternInfo.containerGranularities:
				k = str(r)
				aPath = os.path.join(p,m+"___"+k+"___after")
				bPath = os.path.join(p,m+"___"+k+"___before")
				
				if not os.path.exists(aPath) or not os.path.exists(bPath):
					continue
		
				aTokens = getFileTokens(aPath,shape, containerGranularity)
				bTokens = getFileTokens(bPath,shape, containerGranularity)
			
				print m,k	
				patternInfo.outputData(project, m, k, aTokens, containerGranularity, rawDataFile )
				# we only do this on the patterns where we want to see deltas.
				# i.e., we don't do this for the halstead metrics
				if patternInfo.doChanged:
					diffFiles = diffTokens(patternInfo, aTokens,bTokens)
					patternInfo.outputDiffData(project, m, k, diffFiles, containerGranularity, changedDataFile)

def emitSetDiff(patternInfo, aList,bList):

	aMethods = patternInfo.getSetForDiff(aList)
	bMethods = patternInfo.getSetForDiff(bList)

	d = []

	for m in aMethods.keys():
		if bMethods.has_key(m):
			aSet = aMethods[m]	
			bSet = bMethods[m]
			for aT in aSet.keys():
				if not aT in bSet:
					d.append((m,"+",aT))
				elif aSet[aT] - bSet[aT] == 0:
					pass
				elif aSet[aT] - bSet[aT] < 0:
					d.append((m,"-",aT))
				elif aSet[aT] - bSet[aT] > 0:
					d.append((m,"+",aT))
					
			for bT in bSet.keys():
				if not bT in aSet:
					d.append((m,"-",bT))
		if not bMethods.has_key(m):
			aSet = aMethods[m]
			for aT in aSet.keys():
				d.append((m,"+m",aT))

	for m in bMethods.keys():
		if not aMethods.has_key(m):
			bSet = bMethods[m]
			for bT in bSet.keys():
				d.append((m,"-m",bT))
			
	return d

def diffTokens(patternInfo, aTokens,bTokens):
	"""determine which tokens are in files that are only in a (after the commit) and mark those with A (added),
	which tokens are in files that are only in b (before the commit) and those with D (deleted),
	and which tokens are in files that exist in both a and b, and do the set difference and marke them with M (modified).

	We're most interested in files that are in both a and b, but in which the tokens are modified.
	"""

	diffFiles = []

	for a in aTokens.keys():
		if not bTokens.has_key(a):
			d = emitSetDiff(patternInfo, aTokens[a],[])
			if len(d) > 0:
				diffFiles.append(("A",a,d))
		else: 
			d = emitSetDiff(patternInfo, aTokens[a],bTokens[a])
			if len(d) > 0:
				diffFiles.append(("M",a,d))
	
	for b in bTokens.keys():
		if not aTokens.has_key(b):
			d = emitSetDiff(patternInfo, [],bTokens[b])
			if len(d) > 0:
				diffFiles.append(("D",b,d))
	if len(diffFiles) > 0:
		print diffFiles
		#time.sleep(1)

	return diffFiles

def getFileTokens(dir, shape, containerGranularity):
	tokens = {}

	for root,dirs, files in os.walk(dir):
		paths = [ os.path.join(root,f) for f in files ] 
		for p in paths:
			k = p.replace(dir,"")
			#print k
			tokens[k] = tokensService(p,shape, containerGranularity)

	return tokens

# this is not in use currently, since we now use the tokens service
# over a socket.  But we keep it here in case we decide to move back to it
def _tokensService(p,pattern,output):
	#be platform independent
	if platform.system() == "Windows":
		path = '../export/eclipse.exe'
	elif platform.system() == "Darwin":
		path = '../export/eclipse.app/Contents/MacOS/eclipse'
	cmd = path + ' -file ' + p + ' -pattern ' + pattern + ' -output ' + output
	print cmd
	out = os.popen(cmd).read();

	f = open(output,'r')
	data = f.readlines()
	f.close()

	return data


# get the tokens by connecting to the eclipse plugin over a socket
# requests are of the form:
# filepath:pattern
#
# and responses are of the form
# line number:pattern info separated by colons
# 
# end of message is delineated by two empty lines
# if ERROR occurs in the message then there was an exception
# during parsing
parserSocket = None
def tokensService(p, pattern, containerGranularity):
	global parserSocket
	if not parserSocket:
		print "opening socket"
		parserSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		parserSocket.connect( ("localhost", 6000) )
		
	request = p + ":" + pattern + ":" + "containergranularity=" + containerGranularity + "\n"
	print "sending request: " + request
	parserSocket.send(request)
	buf = ""
	while not buf.endswith("\n\n"):
		try:
			more = parserSocket.recv(4096)
			buf += more
		except socket.error, e:
			print e
	if "ERROR" in buf:
		print "ERROR getting tokens", buf
		sys.exit(1)
	# this emulates the effect of file.readlines() which was in the 
	# original version of tokensService
	return [x + "\n" for x in buf.strip().split("\n") if x]

def diffPrint(b,a):
	print os.popen('diff '+b+' '+a).read()

#reportRevToOutputFile(bPath,aPath,len(diffFiles),outputFile)
def reportRevToOutputFile(bPath,aPath,files,file):
	file.write(bPath + ',' + aPath + ',('+str(files)+')\n')
	file.write('-------------------\n')
	file.flush()

#reportDiffToOutputFile(bPath,aPath,kind,info,outputFile)
def reportDiffToOutputFile(b,a,kind,info,file):
	file.write('M '+b+'=>'+a+'\n')
	file.write(str(info)+'\n')
	file.write(os.popen('diff '+b+' '+a).read())
	file.write('\nend diff\n');
	file.flush()

#reportToOutputFile(d,kind,info,outputFile)
def reportToOutputFile(d,kind,info,file):
	file.write(kind +' '+ d+ '\n')
	file.write(str(info)+'\n')
	file.flush()

if __name__ == "__main__":
	import sys

	#process(projects,"ClassTypeParams","class.output")
	
	process(projects,"rawtypes", "./rawtypes.output")
	process(projects,"halstead","./halstead.output")
	process(projects,"parameterizedtypes","./types.output")
	process(projects,"parameterizeddeclarations","./decls.output")
	process(projects,"castsparams","./cast.output")
	process(projects,"annotations","./annotations.output")

	#process(projects,"CastsParams",os.path.abspath("./cast.output"))
	#tokensService('../genericfactory/src/tokenize/Application.java',"ClassTypeParams")
