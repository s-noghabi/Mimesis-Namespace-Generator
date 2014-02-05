package namespaceGenerator;

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.ArrayUtils;
/*
 *This class keeps the parameters of distributions needed in the namespace generation process,
 * e.g. the distribution of files at each depth, number of files per directory, and etc. 
 * These distributions are kept in the form of the PDF of it.Also, it contains some general parameters. 
 */
public class MimesisParameters  {
	
	// Files at each depth
	public final String filesAtDepthKeysKey = "FILES_AT_DEPTH_KEYS";
	public Long[] filesAtDepthKeys;
	public final String filesAtDepthWeightsKey = "FILES_AT_DEPTH_WEIGHTS";
	public double[] filesAtDepthWeights;

	// target number of files in namespace
	public final String targetFilesInNamespaceKey = "FILES_IN_NAMESPACE";
	public final int targetFilesInNamespaceDefault = 0;
	public int targetFilesInNamespace;
	
	
	// target number of directories in namespace
	public final String targetDirsInNamespaceKey = "DIRS_IN_NAMESPACE";
	public final int targetDirsInNamespaceDefault = 0;
	public int targetDirsInNamespace;
		
	// Files per dir
	public final String filesPerDirKeysKey = "FILES_PER_DIRECTORY_KEYS";
	public long[] filesPerDirKeys;
	public final String filesPerDirWeightsKey = "FILES_PER_DIRECTORY_WEIGHTS";
	public double[] filesPerDirWeights;
	
	// Subdirectories at each depth
	public final String subdirsAtDepthKeysKey = "SUBDIRS_AT_DEPTH_KEYS";
	public Long[] subdirsAtDepthKeys;
	public final String subdirsAtDepthWeightsKey = "SUBDIRS_AT_DEPTH_WEIGHTS";
	public double[] subdirsAtDepthWeights;
	
	// Subdirectories per directory
	public final String subdirsPerDirKeysKey = "SUBDIRS_PER_DIR_KEYS";
	public long[] subdirsPerDirKeys; 
	public final String subdirsPerDirWeightsKey = "SUBDIRS_PER_DIR_WEIGHTS";
	public double[] subdirsPerDirWeights;
	
	// File ages at t0
	public final String ageAtT0KeysKey = "AGE_AT_T0_KEYS";
	public long[] ageAtT0Keys; 
	public final String ageAtT0WeightsKey = "AGE_AT_T0_WEIGHTS";
	public double[] ageAtT0Weights;

	// File sizes at t0
	public final String fileSizesAtT0KeysKey = "FILE_SIZE_AT_T0_KEYS";
	public long[] fileSizesAtT0Keys; 
	public final String fileSizesAtT0WeightsKey = "FILE_SIZE_AT_T0_WEIGHTS";
	public double[] fileSizesAtT0Weights;
	
	// Flag to enable namespace stat printing
	public final String printNamespaceStatsKey = "PRINT_NAMESPACE_STATS";
	public boolean printNamespaceStats; 
	public boolean printNamespaceStatsDefault = false;
	
	DataConfiguration config = null;
	public boolean persistConfig = false;
	
	public MimesisParameters(Configuration c, boolean p)
	{
		this(c);
		
		this.persistConfig = p;
		
		if (!this.persistConfig)
			this.config = null;
	}
	
	
	public MimesisParameters(Configuration c)
	{
		config = new DataConfiguration(c) ;
		
		this.targetFilesInNamespace = config.getInteger(this.targetFilesInNamespaceKey, this.targetFilesInNamespaceDefault);
		this.targetDirsInNamespace = config.getInteger(this.targetDirsInNamespaceKey, this.targetDirsInNamespaceDefault);

		this.filesAtDepthKeys = config.getLongList(this.filesAtDepthKeysKey).toArray(ArrayUtils.EMPTY_LONG_OBJECT_ARRAY);
		this.filesAtDepthWeights = config.getDoubleArray(this.filesAtDepthWeightsKey);
		
		if (this.filesAtDepthKeys == null || this.filesAtDepthWeights == null)
			throw new RuntimeException("Could not load FILES_AT_DEPTH configuration values.");
		
		this.filesPerDirKeys = config.getLongArray(this.filesPerDirKeysKey);
		this.filesPerDirWeights = config.getDoubleArray(this.filesPerDirWeightsKey);
		
		if (this.filesPerDirKeys == null || this.filesPerDirWeights == null)
			throw new RuntimeException("Could not load FILES_PER_DIRECTORY configuration values.");
		
		this.subdirsAtDepthKeys = config.getLongList(this.subdirsAtDepthKeysKey).toArray(ArrayUtils.EMPTY_LONG_OBJECT_ARRAY);
		this.subdirsAtDepthWeights = config.getDoubleArray(this.subdirsAtDepthWeightsKey);
		
		if (this.subdirsAtDepthKeys == null || this.subdirsAtDepthWeights == null)
			throw new RuntimeException("Could not load SUBDIRS_AT_DEPTH configuration values.");
		
		this.subdirsPerDirKeys = config.getLongArray(this.subdirsPerDirKeysKey);
		this.subdirsPerDirWeights = config.getDoubleArray(this.subdirsPerDirWeightsKey);
		
		if (this.subdirsPerDirKeys == null || this.subdirsPerDirWeights == null)
			throw new RuntimeException("Could not load SUBDIRS_PER_DIR configuration values.");

		this.ageAtT0Keys = config.getLongArray(this.ageAtT0KeysKey);
		this.ageAtT0Weights = config.getDoubleArray(this.ageAtT0WeightsKey);
		
		if (this.ageAtT0Keys == null || this.ageAtT0Weights == null)
			throw new RuntimeException("Could not load AGE_AT_T0 configuration values.");
		
		this.fileSizesAtT0Keys = config.getLongArray(this.fileSizesAtT0KeysKey);
		this.fileSizesAtT0Weights = config.getDoubleArray(this.fileSizesAtT0WeightsKey);
		
		if (this.fileSizesAtT0Keys == null || this.fileSizesAtT0Weights == null)
			throw new RuntimeException("Could not load FILE_SIZE_AT_T0 configuration values.");
		
		this.printNamespaceStats = config.getBoolean(this.printNamespaceStatsKey, this.printNamespaceStatsDefault);
		
		if (!this.persistConfig)
			this.config = null;
	}
	
}
