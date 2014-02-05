package randomGenerator;

import java.util.Random;
import java.util.TreeMap;
import java.util.NavigableMap;
import java.util.ArrayList;
/*
 * This class generates random keys from a list of keys based on the weights of the keys
 */
public class WeightedRanGen implements RandomGenerator<Long> {
	private final NavigableMap<Double, Long> map = new TreeMap<Double, Long>();
    private final Random random;
    private double total = 0;
    private int numTrials = 1000;
    private Long[] keys;
    private double[] weights;
	
	/**
	 * @param rnd	The random number generator
	 * @param keys	Array of size s of keys
	 * @param weights	Array of size s of weights, one for each key
	 */
	public WeightedRanGen(Random rnd, Long[] keys, double[] weights)
	{
		this.random = rnd;
		if (keys.length != weights.length)
		{
			throw new IllegalArgumentException("Mismatch key/weights length: " + keys.length + " vs. " + weights.length);
		}
		
		for (int i = 0; i < weights.length; i++)
		{
			this.add(weights[i], keys[i]);
		}
		
		this.keys = keys;
		this.weights = weights;
	}

	private void add(double weight, Long result) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, result);
    }
	
	@Override
	//returns a next randomly generated key based on the weights
	public Long next() {
		double value = random.nextDouble() * total;
        return map.ceilingEntry(value).getValue();
	}
	
	@Override
	public Long expectedValue(Long span) {
		
		long sum = 0;
		for (int i = 0; i < numTrials; i++)
		{
			long curr = 0;
			long arrivals = 1;
			while (curr < span)
			{
				curr += next();
				arrivals += 1;
			}
			if (curr > span)
				arrivals -= 1;
			sum += arrivals;
		}
		
		long heuristic = 100000;		
		if (sum / numTrials > heuristic)
		{
			int moreTrials = 1000;
			for (int i = 0; i < moreTrials; i++)
			{
				long curr = 0;
				long arrivals = 1;
				while (curr < span)
				{
					curr += next();
					arrivals += 1;
				}
				if (curr > span)
					arrivals -= 1;
				sum += arrivals;
			}
			return new Long(sum / (numTrials + moreTrials));
		}
		return new Long(sum / numTrials);
	}

	@Override
	//returns a list of randomly generated keys based on the weights. 
	public Long[] next(long n, Long span) {
		ArrayList<Long> list = new ArrayList<Long>((int) n);
		long sum = 0;
		int curr = 0;
		
		for (int i = 0; i < this.weights.length ; i++) {
			for (int j = 0 ; j < Math.round(n * this.weights[i]) && curr < n ; j++) {
				sum += this.keys[i];
				list.add(this.keys[i]);
				curr++;
			}
		}
		
		System.out.println("[DEBUG] n = " + n + "; curr = + " + curr + "; span = " + span + "; sum = " + sum + "; sum/span = " + (sum/(double) span));
		
		// generate a random permutation of the arrivals
		java.util.Collections.shuffle(list, this.random);
		
		if (Math.abs(span - sum)/(double) span > 0.1)
			throw new RuntimeException("Span differs too much from expected one: " + span + " " + sum);
		
		for (int i = curr; i < n ; i++)
			list.add(-1L);
		
		return (Long[]) list.toArray(new Long[list.size()]);				
	}
	
	
}

