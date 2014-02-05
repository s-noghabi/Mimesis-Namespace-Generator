package namespaceGenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import randomGenerator.RandomGenerator;
import randomGenerator.WeightedRanGen;
import randomGenerator.WeightedTriangularRanGen;
/*
 * This is the base class for generating the namespace. By calling the generateFileSystem() the namespace would be generated.
 * First the directories are created with the binpacking approach, via the method packBins(). Second, the files are created 
 * with the binpacking approach,and assigned to directories. Then the file sizes and create stamps are assigned
 */
public class HierarchicalFileSystemGenerator  {

	public HierarchicalNamespaceEntry root = null;
	private HierarchicalNamespaceEntry user = null;
	private HierarchicalNamespaceEntry projects = null;
	private HierarchicalNamespaceEntry other = null;
	private int maxDepth = 32;
	private long lastStampInSnapshot = -1;
	public ArrayList<ArrayList<HierarchicalNamespaceEntry>> byDepthDirs = null; //Arrays of directories at each depth, e.g. byDepthDirs[i] = all directories at depth i
	public ArrayList<ArrayList<HierarchicalNamespaceEntry>> byDepthFiles = null; //Arrays of files at each depth, e.g. byDepthFiles[i] = all files at depth i
	private long numTargetDirs  = 0; 
	private long numTargetFiles = 0; 
	public long numDirs = 0;
	public long numFiles = 0;
	private Random random = null;
	private WeightedRanGen filesDepthGen; //samples from the distribution of files at each depth
	private WeightedTriangularRanGen filesPerDirGen; //samples from the distribution of number of files per directory
	private WeightedRanGen subdirsDepthGen; //samples from the distribution of directories at each depth
	private WeightedTriangularRanGen subdirsPerDirGen; //samples from the distribution of number of subdirectories per directory
	private RandomGenerator<Long> fileAgeGen;
	private RandomGenerator<Long> fileSizeGen;
	private char[] p = new char[256*2 + 1]; //used by "path()", declared here for performance reasons
	
	/*
	 * 		initializing the File System. 
	 * 			Depth 0 is root.
	 * 			Depth 1 is: user, project, other.
	 * 			The rest is not known, just routinely initialized
	 */  
	public HierarchicalFileSystemGenerator(Random r, MimesisParameters params)
	{	

		byDepthDirs = new ArrayList<ArrayList<HierarchicalNamespaceEntry>>(maxDepth); 
		byDepthFiles = new ArrayList<ArrayList<HierarchicalNamespaceEntry>>(maxDepth);
		numDirs = 4; 
		root = new HierarchicalNamespaceEntry(0, 0, null, true);
		user = new HierarchicalNamespaceEntry(0, 1, root, true);
		projects = new HierarchicalNamespaceEntry(0, 2, root, true);
		other = new HierarchicalNamespaceEntry(0, 3, root, true);
		ArrayList<HierarchicalNamespaceEntry> depth0 = new ArrayList<HierarchicalNamespaceEntry>(1);
		depth0.add(root);
		byDepthDirs.add(0, depth0);
		byDepthFiles.add(0, new ArrayList<HierarchicalNamespaceEntry>());
		ArrayList<HierarchicalNamespaceEntry> depth1 = new ArrayList<HierarchicalNamespaceEntry>(3);
		depth1.add(user);
		depth1.add(projects);
		depth1.add(other);
		byDepthDirs.add(1, depth1);
		byDepthFiles.add(1, new ArrayList<HierarchicalNamespaceEntry>());
		for (int i = 2; i < maxDepth; i++)
		{
			byDepthDirs.add(i, new ArrayList<HierarchicalNamespaceEntry>());
			byDepthFiles.add(i, new ArrayList<HierarchicalNamespaceEntry>());
		}
		this.random = r;
		filesDepthGen = new WeightedRanGen(this.random, params.filesAtDepthKeys, params.filesAtDepthWeights);
		filesPerDirGen = new WeightedTriangularRanGen(this.random, params.filesPerDirKeys, params.filesPerDirWeights);
		subdirsDepthGen = new WeightedRanGen(this.random, params.subdirsAtDepthKeys, params.subdirsAtDepthWeights);
		subdirsPerDirGen = new WeightedTriangularRanGen(this.random, params.subdirsPerDirKeys, params.subdirsPerDirWeights);
		fileAgeGen = new WeightedTriangularRanGen(this.random, params.ageAtT0Keys, params.ageAtT0Weights, false);
		fileSizeGen = new WeightedTriangularRanGen(this.random, params.fileSizesAtT0Keys, params.fileSizesAtT0Weights, false);
		
		this.numTargetDirs = params.targetDirsInNamespace;
		this.numTargetFiles = params.targetFilesInNamespace;
	}
	
	public void setTargetFiles(long t)
	{
		if (this.numFiles >= 1)
			throw new UnsupportedOperationException("Cannot change the target files once the namespace has been partially generated.");
		this.numTargetFiles = t;
	}
	public long getLastStampInSnapshot()
	{
		return lastStampInSnapshot;
	}
	
	/* Returns the path from the root to that node. */
	public String path(HierarchicalNamespaceEntry node)
	{ 	
		long id;
		int digit;
		int pos = p.length - 1;
		
		if (node.isDir())
		{
			p[pos] = '/';
			pos -= 1;
		}
		
		HierarchicalNamespaceEntry i = node;
		
		while (i != null)
		{
			id = i.getName();
			while (true)
			{
				digit = (int) (id % 10);
				id = id / 10;
				p[pos] = (char) ((char) digit + (char) '0');
				pos -= 1;
				if (id == 0)
					break;
			}
			p[pos] = '/';
			pos -= 1;
            i = i.getParent();
		}
		pos += 1;
		return new String(p, pos, p.length - pos); 
	}

	/*Prints the File System. i.e. the directories, and the files in each directory */
	public void print(BufferedWriter out) throws IOException
	{
		int i, j, k;
		ArrayList<HierarchicalNamespaceEntry> level, children;
		HierarchicalNamespaceEntry directory;
		HierarchicalNamespaceEntry file;
		String currentPath;
		
		// Print the directories
		for (i = 0; i < this.byDepthDirs.size(); i++)
		{
			level = this.byDepthDirs.get(i);
			for (j = 0; j < level.size(); j++)
			{
				directory = level.get(j);
				currentPath = this.path(directory);
				out.write(directory.getCreationStamp() + "\t" + currentPath + "\tnull");
				out.newLine();
				
				// Print the files of the directory
				children = directory.getChildren();
				for (k = 0; k < children.size(); k++)
				{
					file = children.get(k);
					if ( file.isDir() )
						continue;
					out.write(file.getCreationStamp() + "\t" + currentPath + file.getName() + "\t" + file.getSize());
					out.newLine();
				}
			}			
		}
		
		out.flush();
	}
	
	/* Prints the cdf to a file based on the given HashMap. this is used in printStats()*/
	public void print_cdf(String file_name, HashMap<Long, Long> g , long tot) throws IOException
	{
		BufferedWriter out = new BufferedWriter(new FileWriter("../output/"+file_name));
		out.write("#"); out.newLine();
		double cdf = 0.0;
	    ArrayList<Long> sortedKeys = new ArrayList<Long>(g.keySet());
	    Collections.sort(sortedKeys);
	    Long theValue;
	    double p;
	   
	    for ( Long theKey : sortedKeys )
	    {
	    	if (theKey == 0) continue;
	    	if(file_name=="files_at_depth.cdf"&&theKey<2) continue;
	    	theValue = g.get(theKey);
	    	p = theValue.doubleValue() / tot;
	    	cdf += p;
	    	out.write(theKey.toString() + '\t' + theValue.toString() + '\t' + p + '\t' + cdf);
	    	out.newLine();
	    }
	    out.flush();
	    out.close();
	    sortedKeys.clear();
	}
	/*Prints the statics of the File System to files. The distributions considered are:
	 * 		1) Subdirectories per directory
	 * 		2) Directories at each depth
	 * 		3) Files per directory
	 * 		4) Files at each depth*/ 
	public void printStats() throws IOException
	{
		int i, j, k;
		long subdirsInDir,filesInDir;
		ArrayList<HierarchicalNamespaceEntry> level, children;
		HierarchicalNamespaceEntry directory;
		HashMap<Long, Long> g = new HashMap<Long, Long>();
		long tot = 0;
		Long key, value;

		// Subdirectories per directory statistics
		for (i = 0; i < this.byDepthDirs.size(); i++)
		{
			level = this.byDepthDirs.get(i);
			for (j = 0; j < level.size(); j++)
			{
				directory = level.get(j);
				subdirsInDir = 0;
				children = directory.getChildren();
				for (k = 0; k < children.size(); k++)
				{
					if (children.get(k).isDir())
						subdirsInDir += 1;
				}
				key = Long.valueOf(subdirsInDir);
				if (key == 0)
					continue; // Ignore leaf directories for stats
				if (g.containsKey(key))
					value = Long.valueOf( g.get(key).longValue() + 1L );
				else
					value = Long.valueOf(1L);
				g.put(key, value);
                tot += 1; 
			}
		}
		tot -= (g.get(0) == null) ? 0 : g.get(0);
		print_cdf("subdirs_per_dir.cdf", g, tot);
	
		// Directories at depth statistics
	    g.clear();
	    tot = 0;
	    long subdirsAtDepth = 0;
	    for (i = 0; i < this.byDepthDirs.size(); i++)
		{
			level = this.byDepthDirs.get(i);
			subdirsAtDepth = level.size();
			key = Long.valueOf(i);
			value = Long.valueOf(subdirsAtDepth);
			g.put(key, value);
            tot += value;
		}
	    print_cdf("dirs_at_depth.cdf", g, tot);

		// Files per directory statistics
		tot = 0;
		g.clear();
		for (i = 0; i < this.byDepthDirs.size(); i++)
		{
			level = this.byDepthDirs.get(i);
			for (j = 0; j < level.size(); j++)
			{
				directory = level.get(j);
				filesInDir = 0;
				children = directory.getChildren();
				for (k = 0; k < children.size(); k++)
				{
					if (!children.get(k).isDir())
						filesInDir += 1; 
				}
				key = Long.valueOf(filesInDir);
				if (key == 0)
					continue; // Ignore leaf directories for stats
				if (g.containsKey(key))
					value = Long.valueOf( g.get(key).longValue() + 1L );
				else
					value = Long.valueOf(1L);
				g.put(key, value);
                tot += 1; 
			}
		}
	    tot -= (g.get(0) == null) ? 0 : g.get(0);
	    print_cdf("files_per_dir.cdf", g, tot);

		// Files at depth statistics
	    g.clear();
	    tot = 0;
	    long filesAtDepth = 0;
	    for (i = 0; i < this.byDepthFiles.size(); i++)
		{
			level = this.byDepthFiles.get(i);
			filesAtDepth = level.size();
			key = Long.valueOf(i);
			value = Long.valueOf(filesAtDepth);
			g.put(key, value);
            tot += value;
		}
	    print_cdf("files_at_depth.cdf", g, tot);
				
	}
	
	/* given the bins (size of each bin), a number of objects are generated, sorted descending,
	 * and packed in the bins starting from the worst bin, i.e. the bin with the most free capacity left. */
	private ArrayList<HierarchicalNamespaceEntry> packBins(int[] bins, long targetObjects, RandomGenerator<Long> gen, boolean packDirs)
	{

		// 1) Generate target objects
		Integer[] targetObjectCount = new Integer[(int) targetObjects];
		long allocatedObjects = 0;
		int numObjects;
		boolean done = false; //used for finishing the process
		
		for (int i = 0; i < (int) targetObjects; i++)
		{
			if (done)  //used for subdirs per dir: the remaining subdirs will have zero subdirs
			{
				targetObjectCount[i] = 0;
				continue;
			}
			numObjects = (int) ((long) gen.next());
			allocatedObjects += numObjects;
			if (allocatedObjects > targetObjects)
			{
				allocatedObjects -= numObjects;
				numObjects = (int) (targetObjects - allocatedObjects);
				allocatedObjects += numObjects;		
				done = true; //finish the object generating process
			}
			targetObjectCount[i] = numObjects;
		}
						
		// 2) Sort objects target list, descending
		Arrays.sort(targetObjectCount, Collections.reverseOrder());
		
		// 3) Pack them : assign depths to each object in targetObjectCount. try to find the worst fit!
		int[] targetDepths = new int[targetObjectCount.length];
		Arrays.fill(targetDepths, -1);

		long worstFit = -1;
		long r;
		for (int i = 0; i < targetObjectCount.length; i++)
		{
			worstFit = -1; 	
			if (targetObjectCount[i] == 0)
			{
				r = (packDirs) ? this.subdirsDepthGen.next() : this.filesDepthGen.next();
				worstFit = r;
			} else {
				//when packing bins for directories, depth 0 is root, and depth 1 is user, project, program 
				r = ((packDirs) ? 2 : 1) + random.nextInt(bins.length - ((packDirs) ? 3 : 2)); 
				for (int j = (packDirs) ? 2 : 1; j < bins.length; j++)
					if (bins[(j + (int)r) % bins.length] > 0 && bins[(j + (int)r) % bins.length] >= targetObjectCount[i])
						if (worstFit == -1)
							worstFit = (j + (int)r) % bins.length; 
						else if (bins[(j + (int)r) % bins.length] > bins[(int)worstFit])
							worstFit = (j + (int)r) % bins.length;
				if (worstFit == -1) //one last attempt; find the worst fit, even if bin is not big enough
				{
					r = (packDirs) ? this.subdirsDepthGen.next() : this.filesDepthGen.next();
					worstFit = r;
					for (int j = (packDirs) ? 2 : 1; j < bins.length; j++)
						if ( bins[j]  > bins[(int)worstFit] )
							worstFit = j;

				}
			}
			if (worstFit < 0 || worstFit >= bins.length)
			{
				System.err.println("Could not find target bin; worstFit = " + worstFit + " i = " + i + "; r = " + r + "; targetObjectCount[i] = " + targetObjectCount[i] + " bins = " + Arrays.toString(bins));
				System.exit(0);
			}
			
			// found a bin; decrease capacity
			bins[(int)worstFit] -= targetObjectCount[i];
			
			// assign target depth to corresponding object
            targetDepths[i] = (int)worstFit;
		}
		
		for (int i = 0; i < bins.length; i++)
		{
			bins[i] = 0;
			for (Integer aux : targetDepths)
				if (aux == i)
					bins[i] += 1;
			if (bins[i] == 0 ) // Nothing will be packed at this depth; so add 3 random files
			{					
				for (int j = 0; j < 3; j++)
				{
					r = this.random.nextInt(targetDepths.length);
					targetDepths[(int)r] = i;
				}
				bins[i] += 3;
			}
		}

        // 4) Create directories in depth 2, assign a random parent to them 
		HierarchicalNamespaceEntry dir, newObject;
		if (packDirs)
		{
			for (int i = 0; i < targetObjectCount.length; i++)
			{
				if (targetDepths[i] != 2)
					continue;
	            dir = this.byDepthDirs.get(1).get(random.nextInt( this.byDepthDirs.get(1).size()) ); // select a parent at depth -1 (i.e., at depth 1)
	            newObject = new HierarchicalNamespaceEntry(0, dir.getNumChildren() + 1, dir, true); //create the new directory
	            dir.addChild(newObject);
	            this.byDepthDirs.get(targetDepths[i]).add(newObject);
	            this.numDirs += 1;			
			}
		}

		// 5) Create the other objects; PACK THE BINS!
		int current = -1;
		ArrayList<HierarchicalNamespaceEntry> newFiles = null;
		if (!packDirs)
			newFiles = new ArrayList<HierarchicalNamespaceEntry>((int) targetObjects);
		for (int d = (packDirs) ? 2 : 1; d < bins.length - 1; d++)
		{
			if (d + 1 >= this.byDepthDirs.size())
				break;
			// Create all children of directories in depth d
			current = 0; //points to the dir which children are going to be added to		
			for (int i = 0; i < targetObjectCount.length; i++) 
			{
				if (targetDepths[i] != d + 1) // if not a child of level d, no need to create it now
					continue;
				
				
				if (current >= this.byDepthDirs.get(d).size()) //all directories at depth d already have children, start assigning from the first 
                    current = 0; 
				
				if (this.byDepthDirs.get(d).size() == 0)
                    continue;
                dir = this.byDepthDirs.get(d).get(current);
                
                for (int c = 0; c < targetObjectCount[i]; c++) // create all the children of current directory -> dir
                {
                   //create the new children 
                   newObject = (packDirs) ? 
                		new HierarchicalNamespaceEntry(0, dir.getNumChildren() + 1, dir, true) :
                		new HierarchicalNamespaceEntry(0, dir.getNumChildren() + 1, dir, false) ; 
                   dir.addChild(newObject);
                   if (packDirs) {
                	   this.byDepthDirs.get(d + 1).add(newObject);  
                	   this.numDirs += 1;
                   } else {
                	   this.byDepthFiles.get(d + 1).add(newObject);
                	   this.numFiles += 1;
                	   newFiles.add(newObject);
                   }
                }
                if (targetObjectCount[i] > 0) 
                	current += 1;
			}
		}
		if (newFiles != null)
			Collections.shuffle(newFiles);
		return newFiles;
	}

	public void generateFileSystem()
	{
		
		//1) Create Directories
		int[] bins = new int [maxDepth+1];
		Arrays.fill(bins, 0);
		bins[1]=3;
		int subdirs;
		
		
		for (long l = 0; l < this.numTargetDirs ; l++){
			subdirs = (int) (long) this.subdirsDepthGen.next();
			bins[subdirs] += 1;
		}
		for (int i = 0; i < bins.length ; i++){ // just for preventing very small bins
			if (bins[i] < 10)
				bins[i] += 1;
		}
		
		System.out.println("Packing bins.");
		this.packBins(bins, this.numTargetDirs, this.subdirsPerDirGen, true);
		System.out.println("Bins packed; numDirs = " + this.numDirs + "; numFiles = " + this.numFiles);
		
		//2) Create files
		Arrays.fill(bins, 0);
		for (long l = 0; l < this.numTargetFiles ; l++){
			subdirs = (int) (long) this.filesDepthGen.next();
			bins[subdirs] += 1;
		}
		for (int i = 0; i < bins.length ; i++){ // just for preventing very small bins
			if (bins[i] < 10)
				bins[i] += 1;
		}
		
		System.out.println("Packing bins (files).");
		ArrayList<HierarchicalNamespaceEntry> newFiles = this.packBins(bins, this.numTargetFiles, this.filesPerDirGen, false);
		
		//3) Assigning creation stamps to files 
		long creationStamp;
		long clock = -1;
		System.out.println("Assigning creation stamps to files; numDirs = " + this.numDirs + "; numFiles = " + this.numFiles);

		for (HierarchicalNamespaceEntry file : newFiles)
		{
			creationStamp = this.fileAgeGen.next();
			clock = (clock > creationStamp) ? clock : creationStamp + 1;
			file.setCreationStamp(creationStamp);
		}
		this.lastStampInSnapshot = clock;
		
		// convert the ages of the files to creation stamps
		for (HierarchicalNamespaceEntry file : newFiles)
		{
			creationStamp = file.getCreationStamp();
			file.setCreationStamp(this.lastStampInSnapshot - creationStamp);
		}
		System.out.println("Files created; numDirs = " + this.numDirs + "; numFiles = " + this.numFiles + " & " + newFiles.size() + "; current time: " +
				System.currentTimeMillis());

		//4) Assigning sizes to files
		System.out.println("Assigning file sizes.");
		long fileSize;
		for (HierarchicalNamespaceEntry file : newFiles)
		{			
			fileSize = this.fileSizeGen.next();
			file.setSize(fileSize);
		}

		newFiles.clear();
		newFiles = null;

		//5) Sorting the files at each depth based on the creation stamp
		for (int i = 0; i < this.byDepthFiles.size(); i++)
		{
			Collections.sort(this.byDepthFiles.get(i),
					new Comparator<HierarchicalNamespaceEntry>() {
			    @Override
			    public int compare(HierarchicalNamespaceEntry a, HierarchicalNamespaceEntry b) {
			    	if (a.equals(b)) {
			    	    return 0;
			    	} else if (a.getCreationStamp() > b.getCreationStamp()) {
			    	    return 1;
			    	} else if (b.getCreationStamp() > a.getCreationStamp()) {
			    	    return -1;
			    	} else {
			    	    return System.identityHashCode(a) > System.identityHashCode(b) ? 1 : -1;
			    	}
			    } });
		}
		
	}
	
	
}
