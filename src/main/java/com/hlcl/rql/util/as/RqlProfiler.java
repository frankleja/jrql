package com.hlcl.rql.util.as;

import java.util.*;


/**
 * Statement profiler for various RQL calls over time.
 * Count what has been called how often and how long it took.
 * This helps finding potential call-optimization strategies.
 * Thread-safe, with or without CmsClient connection.
 * 
 * @author Raimund Jacob-Bloedorn
 */
public class RqlProfiler {

	
	/**
	 * Collected information about a specific type of call. 
	 */
	public static class Call implements Cloneable {
		
		/**
		 * Identify what the call did
		 */
		public final String what; 
		
		
		/**
		 * Total number of times this call has been used.
		 */
		public long count = 0;
		
		
		/**
		 * Number of accumulated ms performing this call.
		 */
		public long totalTime = 0;
		
		
		/**
		 * The shortest invocation.
		 */
		public long fastest = -1;
		
		
		/**
		 * The longest invocation.
		 */
		public long slowest = -1;
		
		
		/**
		 * A new entry. 
		 */
		public Call(String what) {
			this.what = what;
		}

		
		/**
		 * A simple copy.
		 */
		public Call clone() {
			try {
				return (Call) super.clone();
			} catch (CloneNotSupportedException e) {
				// implements Clonable, you see?
				throw new IllegalStateException();
			}
		}
		
		
		/**
		 * One line of profiling output.
		 * @param sb in and out
		 * @return sb the input value
		 */
		public StringBuilder toString(StringBuilder sb) {
			sb.append(what);
			sb.append(" ").append(count).append(" calls");
			sb.append(" in ").append(totalTime).append(" ms");
			sb.append(" (").append(fastest).append("-").append(slowest).append(" ms");
			sb.append(", ").append((int) ((double) totalTime / count)).append(" avg");
			sb.append(")");
			return sb;
		}


		/**
		 * Add a number of collected calls.
		 * @param o
		 */
		public void add(Call o) {
			this.count += o.count;
			this.totalTime += o.totalTime;
			
			if (slowest < 0 || o.slowest > slowest)
				slowest = o.slowest;
			
			if (fastest < 0 || o.fastest < fastest)
				fastest = o.fastest;
		}


		/**
		 * Add one call.
		 */
		public void add(long time) {
			this.count += 1;
			this.totalTime += time;
			
			if (slowest < 0 || time > slowest)
				slowest = time;
			
			if (fastest < 0 || time < fastest)
				fastest = time;
		}
	}
	

	/**
	 * Compare calls by number of invocations.
	 */
	public final Comparator<Call> BY_COUNT = new Comparator<Call>() {
		@Override
		public int compare(Call o1, Call o2) {
			int d = (int) (o2.count - o1.count);
			return d == 0 ? o2.what.compareTo(o1.what) : d;
		}
	};


	/**
	 * Compare calls by total time spent.
	 */
	public final Comparator<Call> BY_TIME = new Comparator<Call>() {
		@Override
		public int compare(Call o1, Call o2) {
			int d = (int) (o2.totalTime- o1.totalTime);
			return d == 0 ? o2.what.compareTo(o1.what) : d;
		}
	};

	
	
	/**
	 * Some kind of context this is is profiling.
	 */
	public final String context;
	
	
	/**
	 * Accumulated call data.
	 */
	protected final Map<String, Call> data = new HashMap<String, Call>();

	
	/**
	 * Totals
	 */
	protected final Call total = new Call("TOTAL");
	

	/**
	 * Time when this profiler was started.
	 */
	public long before; 
	
	
	/**
	 * Time when this profiler was stopped.
	 */
	public long after;
	
	
	/**
	 * The context defines some kind of log prefix.
	 * @param context
	 */
	public RqlProfiler(String context) {
		this.context = context;
		this.before = System.currentTimeMillis();
	}
	
	
	/**
	 * Empty context name
	 */
	public RqlProfiler() { this(""); }
	
	
	/**
	 * Get and lazily allocate an entry, unsynchronized.
	 */
	private Call find(String key) {
		Call c = data.get(key);
		if (c != null)
			return c;
		
		c = new Call(key);
		data.put(key, c);
		return c;
	}
	
	
	/**
	 * Add all of the other statements to this.
	 * @param other
	 */
	public void add(RqlProfiler other) {
		synchronized (data) {
			synchronized (other.data) {
				for (Call o: other.data.values()) {
					Call c = find(o.what);
					c.add(o);
				}
				
				total.add(other.total);
			}
		}
	}
	
	
	/**
	 * Count a certain event to have happened.
	 * 
	 * @param what the call that was performed.
	 * @param time the time that was spent.
	 */
	public void add(String what, long time) {
		synchronized (data) {
			Call c = find(what);
			c.add(time);
			total.add(time);;
		}
	}
	
	
	/**
	 * The total number of different statements that have been collected.
	 */
	public int size() {
		synchronized (data) {
			return data.size();
		}
	}
	
	
	private Collection<Call> getTopMost(TreeSet<Call> s, int limit) {
		synchronized (data) {
			for (Call c : data.values()) {
				s.add(c.clone());
			}
		}
		
		if (limit <= 0)
			return s;
		
		ArrayList<Call> out = new ArrayList<Call>(limit);
		for (Call c: s) {
			if (out.size() >= limit)
				return out;
			out.add(c);
		}
		
		return out;
	}
	
	
	/**
	 * The Top X called statements.
	 * 
	 * @param limit X max. number of calls, 0 for unlimited
	 * @return a sorted list of Calls.
	 */
	public Collection<Call> getMostCalled(int limit) {
		TreeSet<Call> s = new TreeSet<Call>(BY_COUNT);
		return getTopMost(s, limit);
	}
	

	/**
	 * The Top X of expensive (time-consuming) statements.
	 * 
	 * @param limit X max. number of calls, 0 for unlimited
	 * @return a sorted list of Calls.
	 */
	public Collection<Call> getMostExpensive(int limit) {
		TreeSet<Call> s = new TreeSet<Call>(BY_TIME);
		return getTopMost(s, limit);
	}

	
	/**
	 * A readable snapshot of the collected profiling data.
	 */
	public String toString() {
		final int top = 10;
		StringBuilder sb = new StringBuilder(512);
		
		sb.append("[ RqlProfiler ").append(context == null || context.isEmpty() ? "" : context).append(":\n");
		sb.append("  Variations: ").append(size()).append("\n");
		sb.append("  ");
		total.toString(sb).append("\n");

		sb.append("\n  Top ").append(top).append(" called:\n");
		for (Call c : getMostCalled(top)) {
			c.toString(sb).append("\n");
		}

		sb.append("\n  Top ").append(top).append(" expensive:\n");
		for (Call c : getMostExpensive(top)) {
			c.toString(sb).append("\n");
		}

		sb.append("] (wallclock time: ").append(after - before).append(" ms)");
		return sb.toString();
	}
	
	
}
