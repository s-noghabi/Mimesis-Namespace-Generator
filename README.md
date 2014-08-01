Mimesis Namespace Generator
===================================
This project is a namespace generator that can create large and realistic hierarchical namespaces. This tool preserves the following distributions (as given by the configuration file used):  directories at each depth, subdirectories per directory, files at each depth, files per directory, file sizes, file creation stamps. Sample configurations based on a large Hadoop (HDFS) cluster is provided, in addition to several configurations based on statistics collected at several HPC deployments. More information is available in "Metadata Traces and Workload Models for Evaluating Big Storage Systems",  Cristina Abad, Huon Luu, Nathan Roberts, Kihwal Lee, Yi Lu, Roy Campbell, IEEE/ACM International Conference on Utility and Cloud Computing (UCC), 2012. Available at: https://wiki.engr.illinois.edu/download/attachments/207290740/cabad_UCC_2012.pdf


Requirements:
============
  - Java 1.6+
  - 12GB+ memory, depending on the size of namespace you plan to generate;
    in our x86_64 Linux test environment, the namespace configuration that
    comes with Mimesis takes ~15 minutes to generate on a machine with 12GB
    RAM on an Intel(R) Xeon(R) Quad Core CPU E5410 @ 2.33GHz.

Installation:
============
  - Unpack tar
  - Install (download and extract) the following Apache Commons libraries (jars)
    under lib/ :
    - Commons configuration 1.8
    - Commons lang 2.6
    - Commons lang 3.1
    - Commons logging 1.1.1

Configuring:
============
  mimesis.properties contains a sample configuration file modeled after a
  large HDFS namespace in a top Internet Services Company. This file can be
  replaced to model other namespaces or to generate a larger namespace with
  the same hierarchical shape as the original namespace. Larger namespaces
  take more time to generate and require more RAM.
  You can create you own configuration file. Put your files 
  under the  "conf" folder. When running the program give the file name as 
  a command line argument.  The default is set to "mimesis.properties"

Running:
==========
  Run the following commands in the terminal
  
  ``` ruby
  cd bin ; \
  date ; java -cp ../lib/commons-configuration-1.8/commons-configuration-1.8.jar:../lib/commons-lang-2.6/commons-lang-2.6.jar:../lib/commons-lang3-3.1/commons-lang3-3.1.jar:../lib/commons-logging-1.1.1/commons-logging-1.1.1.jar:. -Xms11g -Xmx11g namespaceGenerator.StandaloneNamespaceGenerationModule mimesis.properties  ; date ; 
  ```
  
  NOTE: To change the configuration of the tool,add you configuration file 
  under the folder "conf". Then, give the name of the file while running the program.
   For eample, the file "mimesis.properties" has been given above.
  
  NOTE: Adjust the memory settings in the java call (above) according to how
  much free memory your system has (and how large the namespace to be
  generated is). Now it is set to 11GB.
  
Output:
=========
  The output is written to stdout. This includes a few informational messages
  (at the beginning and end of the run), and the namespace generated. The
  format of the namespace generated is:
  File_creation_stamp (0 for directories)	Path_to_file/dir	File_size (null for directories)

  NOTE: Path /0 corresponds to the root of the namespace hierarchy

  A sample namespace generated with the default configuration file has been
  provided too, in `bin/output_file.txt`.

  Also, the statistics of the distributions create by this tool are written 
  in files under the output folder. Each file contains the CDF of a distribution
  (same as the file's name) of the created namespace. For example in dirs_at_depth.cdf,
  the CDF of the directories at each depth in the final namespace is provided.

Authors:
========
Cristina L. Abad: cabad@illinois.edu

Shadi A. Noghabi: abdolla2@illinois.edu

January 25th 2014

Credits:
==========
Publications that use namespaces generated with Mimesis should properly cite it as follows:

  Metadata Traces and Workload Models for Evaluating Big Storage Systems
  Cristina Abad, Huon Luu, Nathan Roberts, Kihwal Lee, Yi Lu, Roy Campbell
  IEEE/ACM International Conference on Utility and Cloud Computing (UCC), 2012.
  doi>10.1109/UCC.2012.27, available at: https://github.com/s-noghabi/Mimesis-Namespace-Generator


