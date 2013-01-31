import sys, os, pymysql

def main(user, password, rows):
	conn = pymysql.connect(host="eb2-2291-fas01.csc.ncsu.edu",
		db="generics", port=4747, user=user, passwd = password)

	sql = """
		select class_type, type_args, sum(count) count 
		from parameterized_types p,
		(select filename, max(revision) as last_revision from
		parameterized_types p where project = 'squirrel-sql'
		and not substring(filename, 1, 5) = '/bran' group by
		filename) a where p.filename = a.filename and
		p.revision = a.last_revision 
		group by class_type, type_args
		order by count desc
 	"""
	cursor = conn.cursor()
	cursor.execute(sql)

	print r"""
\begin{table}
\centering
\begin{tabular}{lr}
\toprule
\textbf{Type} & \textbf{Declarations} \\
\midrule """
	i = 0
	for class_type, type_args, count in cursor:
		if i == rows:
			break
		print r"\texttt{" + str(class_type) + "<" + str(type_args) + ">} & " + str(count) + r" \\"
		i += 1
	print r"""\bottomrule
\end{tabular}
\caption{Number of declarations of different generic types in \squirrelsql}
\label{tbl:squirrel-sql-types-table}
\end{table}
"""


	
if __name__ == "__main__":
	if len(sys.argv) != 4:
		print >> sys.stderr, "usage: python squirrel-sql-types-table.py username password rows"
		print >> sys.stderr, "arguments on the command line"
	main(sys.argv[1], sys.argv[2], int(sys.argv[3]))

