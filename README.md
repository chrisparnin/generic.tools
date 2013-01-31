generic.tools
=============

Research tool chain for analysis of Java generic usage in open source projects.



## How to set up

It's possible that this walkthrough may reference directories that don't
exist.  I'm not sure which directories are actually in the repository
and which need to be created.  If you run into a situation where a
directory doesn't exist, just create it, as some tools don't check
and create them when they need them.

Also, I realize that not all of this is straightforward and may not make
immediate sense.  Part of the reason for this is that we tried seemingly
simpler ways and they ended up not being so simple.  In other cases, we
are simply "victims" of our own bad software evolution.  This was the
easiest path to getting things to work.

If you run into any problems, please feel free to update this document
and/or email me cabird@gmail.com

1. checkout project:

    git clone https://github.com/chrisparnin/generic.tools.git generics

2. need to build GenericsConnector
	
    cd $GENERICS/tools/activity/activity/GenericsConnector
    ant

3. Next we need to get a local copy of a cvs or svn repository
   since we're interested in eclipse-cs which is hosted on sourceforge,
   we can use rsync.  This will create a local copy in the directory
   $GENERICS/data/eclipse-cs.cvs, but it will take a little while...

    cd $GENERICS/data
    rsync -av eclipse-cs.cvs.sourceforge.net::cvsroot/eclipse-cs/* ./eclipse-cs.cvs
   
4. Next, if you plan to use your own mysql database then you you will need to create
   the database and it's tables.  This can be done while we are acquiring a copy of 
   the eclipse-cs project.
   
   Start by installing your mysql-server, and setting the root password if you have not:
   
    mysqladmin -u root -h localhost password 'new_mysql_root_password'
   
   Once this has been setup we will create the generics database, set permissions for
   our user, and then create our tables.  Open a connection to your mysql-server:
   
    mysql -u root -h localhost -p mysql_root_password
   
   Then, from within the open connection execute:
   
    create database generics;
    grant usage on *.* to new_username@localhost identified by 'new_user_password';
    grant all privileges on generics.* to new_username@localhost;
   
   Now we can create the tables from the scripts in $GENERICS/tools/sql_statements/.
   Close the mysql connection, move to the directory, and execute the sql scripts.
   
    cd $GENERICS/tools/sql_statements/;
    cat *_tables.sql create_indexes.sql update_fileids.sql | mysql -u username --password=user_password generics
   
   Note that the order of script execution is important as the tables need to exist
   before the indexes are created.

5. Now we need to create the changesets.  This uses the
   `create_project_for_compare.py` in $GENERICS/tools/activity/activity/ which takes four arguments:

    create_project_for_compare.py <cvs root> <project> <repo driver> <jdbc> <skip inserts>

   <cvs root> is the path to the cvs repo that we just grabbed (similarly
   for svn root).  Not for svn roots, you need to use the file:///home/project url.

   <project> is simply the name of the project and will be used in the
   database and for the created directories

   <repo driver> is either 'cvs' or 'svn' and indicates the repo type

   <jdbc> is the jdbc string which is used to connect to the database.
   An example is:
     "jdbc:mysql://<url>:4747/generics?user=bird&password=PASSWORD"
   note that you need to put the jdbc string in double quotes because
   it contains an ampersand which bash will interpret if it is not within
   quotes or escaped.

   <skip inserts> indicates if the database should be updated with inserts.  This requires
   some discussion.  If no one has analyzed this project yet, then the database has not been
   with the repository change logs.  In that case you want to insert the log information into
   the database.  However, you don't want to insert the log information into the database
   if someone else already has done this.  If that is the case, and you want to skip inserting
   log information, use the option "true" for <skip inserts>.  Otherwise don't indicate anything
   and the log information will be inserted.

   Since eclipse-cs already has log info in the database, I use these commands:

    cd $GENERICS/tools/activity/activity
    python create_project_for_compare.py $GENERICS/data/eclipse-cs.cvs eclipse-cs cvs "jdbc:mysql://<url>:4747/generics?user=bird&password=PASSWORD" true

   When running this on a svn repository, I noticed that svn wanted the
   protocol and the full path to the repo.  Since I rsync'ed the svn repo for
   squirrel-sql previously, I did this (yes, all three leading slashes are
   required in "file:///" below):
   
    python create_project_for_compare.py file:///Users/cabird/data/squirrel-sql.svn squirrel-sql svn "jdbc:mysql://<url>:4747/generics?user=bird&password=PASSWORD" false


   this will run for a while...
   
   If this fails and complains about cvs, your system may need a newer version of cvs
   which supports the rls command.  It is present in at least 1.12.x and forward.

   This creates a directory named eclipse-cs in the $GENERICS/tools/activity/activity directory,
   but we need it to be in the $GENERICS/data directory, so execute:

   mv $GENERICS/tools/activity/activity/eclipse-cs $GENERICS/data

6. Next we want to run the source analysis on each change.  The architecture of our server is
   a mix of python and java.  A java source analysis program will start up and listen on a socket
   for analysis requests.  The python program will make requests over the socket and put the results
   into a database.

   Start by opening eclipse.  Select $GENERICS/tools as the workspace.
   You may need to add the project in the genericfactory directory to eclipse or it may 
   simply detect that it is already there.

7. Build the eclipse project by selecting Project -> Build All

8. Now we want to run it.  You need to set up a run configuration.  
   Select "Run" -> "Run Configurations"
   Right click on "Eclipse Application" and select "new"
   For the Name, enter something like "run_as_server"
   Select the radio button "Run an application" and in the drop down next to it, select
     "Tokenize Application"
   Select the "Arguments" tab and add the argument "-server" to the Program Arguments
   text area.
   Next select "Apply" at the bottom and then "Run"
   From now own, you should be able to select "run as server" from the Run drop down button
   which looks like a play button on the toolbar.

   If all goes well, you should see the following in the console:

        Hello RCP World!
        Running as server on port 6000
   
   The server is now running.  If you want to kill it, click on the red square button on the 
   console output toolbar.

9. Next we want the analysis server to analyze the project we just downloaded.
   The following is lame, but it's how things work now.

    cd $GENERICS/tools/patterns

   open the file projects_in.py this is a python file.  All you really need to know is that everything after the #
   character is a comment.  Thus, there is a lot of commented out code in this file.
   You want to create a line indicating the paths to the projects that you want to analyze,
   relative to the directory that this file is is.  So I comment out all of the lines and add:

    projects = ['../../data/eclipse-cs/',]

   Note that python is whitespace sensitive, so there can be no indentation in that line.

   Once you've made this change, run the program with
  
    cd $GENERICS/tools/patterns
    python run.py

10. Once that has run, there are a number of files in the directory $GENERICS/tools/patterns
    of the form eclipse-cs-*.sql and eclipse-cs-*.data

    The .sql files load the data in the .data file into the database.

    The way to execute this is by using the mysql program.  If I want to insert the rawtypes
    data, I'd execute

     mysql -h eb2-2291-fas01.csc.ncsu.edu -P 4747 -u bird --password=PASSWORD generics < eclipse-cs-rawtypes-inserts.sql

    You will want to execute this for each of the eclipse-cs-*-inserts.sql files.
    
    To execute a batch of sql files in one go we can use a command similar to the following:
    
     cat eclipse-cs-*-inserts.sql | mysql -h eb2-2291-fas01.csc.ncsu.edu -P 4747 -u bird --password=PASSWORD generics
     
    This will feed mysql any files are globed by the shell expansion of eclipse-cs-*-inserts.sql
    If one needs to control the order of execution, then cat will respect the order of the 
    file names passed as arguments. ex. cat 1stfile.sql file2.sql multiplefiles*.sql lastfile.sql

11. You're done.  I'm betting that this didn't work perfectly your first time through.  If so, 
    update this document.  Now all you have to do is actually analyze the data that 
    now sits in the database.
   
