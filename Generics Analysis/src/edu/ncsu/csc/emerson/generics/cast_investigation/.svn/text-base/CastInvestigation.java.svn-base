package edu.ncsu.csc.emerson.generics.cast_investigation;

import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.functors.TruePredicate;
import org.apache.commons.collections15.map.PredicatedSortedMap;

import edu.ncsu.csc.emerson.generics.Analysis;
import edu.ncsu.csc.emerson.generics.BasicMetric;
import edu.ncsu.csc.emerson.generics.CastAnalysis;
import edu.ncsu.csc.emerson.generics.HalsteadAnalysis;
import edu.ncsu.csc.emerson.generics.HalsteadMetric;
import edu.ncsu.csc.emerson.generics.ParameterizedTypeAnalysis;

public class CastInvestigation {

	public static void main(String[] args) {
		TimeGraph<CombinedMetric> tg = gatherResults();
		
		analyze(tg);
		
		choose(5,tg);
	}

	private static void choose(int n, TimeGraph<CombinedMetric> tg) {
		
		
		for(String projectName : tg.totals.keySet()){
			
			tg.sample(n, projectName, "cast_increase", new Predicate<CombinedMetric>() {
				public boolean evaluate(CombinedMetric m) {
					return m.castDelta > 0 && m.relativeCastDelta > 0 && m.genericDelta==0;
				}
			});
			
			tg.sample(n, projectName, "cast_decrease", new Predicate<CombinedMetric>() {
				public boolean evaluate(CombinedMetric m) {
					return m.castDelta < 0 && m.relativeCastDelta < 0 && m.genericDelta==0;
				}
			});
			
			tg.sample(n, projectName, "cast_increase_with_generic_decrease", new Predicate<CombinedMetric>() {
				public boolean evaluate(CombinedMetric m) {
					return m.castDelta > 0 && m.relativeCastDelta > 0 && m.genericDelta < 0 && m.relativeGenericDelta < 0;
				}
			});
			
			tg.sample(n, projectName, "cast_decrease_with_generic_increase", new Predicate<CombinedMetric>() {
				public boolean evaluate(CombinedMetric m) {
					return m.castDelta < 0 && m.relativeCastDelta < 0 && m.genericDelta > 0 && m.relativeGenericDelta > 0;
				}
			});
			
		}
	}

	private static TimeGraph<CombinedMetric> gatherResults() {
		PrintWriter out = new PrintWriter(System.out);
		CastReducer cr = new CastReducer(out);
		GenericReducer gr = new GenericReducer(out);
		HalsteadReducer hr = new HalsteadReducer(out);
		
		cr.run();
		gr.run();
		hr.run();
		
		TimeGraph<CombinedMetric> tg = new TimeGraph<CombinedMetric>();
		
		for(Entry<String, SortedMap<Timestamp, HalsteadMetric>> e : hr.tg.totals.entrySet()){
			String project = e.getKey();
			for(Timestamp ts : e.getValue().keySet()){
				CombinedMetric cm = new CombinedMetric();
				cm.hm = hr.tg.totals.get(project).get(ts);

				Map<Timestamp, BasicMetric> map = cr.tg.totals.get(project);
				cm.castMetric = map!=null ? 
						map.get(ts) : 
							new BasicMetric(0);
				Map<Timestamp, BasicMetric> map2 = gr.tg.totals.get(project);
				cm.genericMetric= map!=null ? map2.get(ts) : new BasicMetric(0);
			
				tg.add(ts, project, cm);
			}
		}
		return tg;
	}
	
	private static void analyze(TimeGraph<CombinedMetric> tg) {
		
		for(List<CombinedMetric> projectMetrics : tg){
			CombinedMetric last = null;
			for(CombinedMetric thisMetric : projectMetrics){
				if(last!=null){
					thisMetric.relativeCastDelta = thisMetric.normalizedCasts() - last.normalizedCasts();
					thisMetric.relativeGenericDelta = thisMetric.normalizedGenerics() - last.normalizedGenerics();
					thisMetric.castDelta = thisMetric.castCount() - last.castCount();
					thisMetric.genericDelta = thisMetric.genericCount() - last.genericCount();				
				}
				last = thisMetric;
			}
		}
	}

	
}

class CastReducer extends CastAnalysis{

	TimeGraph<BasicMetric> tg = new TimeGraph<BasicMetric>();
	
	public CastReducer(Writer out) {
		super(out);
	}

	@Override
	protected void print(Timestamp time, String projectName,Map<String, Revision> mostRecentRevisions) {
		tg.add(time,projectName,metricFrom(mostRecentRevisions.values()));
	}	
}

class GenericReducer extends ParameterizedTypeAnalysis{

	TimeGraph<BasicMetric> tg = new TimeGraph<BasicMetric>();

	public GenericReducer(Writer out) {
		super(out);
	}
	
	@Override
	protected void print(Timestamp time, String projectName,Map<String, Revision> mostRecentRevisions) {
		tg.add(time,projectName,metricFrom(mostRecentRevisions.values()));
	}
}

class HalsteadReducer extends HalsteadAnalysis{

	TimeGraph<HalsteadMetric> tg = new TimeGraph<HalsteadMetric>();
	
	public HalsteadReducer(Writer out) {
		super(out);
	}
	
	protected void print(Timestamp time, String project, Map<String, Revision> mostRecentRevisions) {
		tg.add(time,project,metricFrom(mostRecentRevisions.values()));
	}
}

class TimeGraph<Metric> implements Iterable<List<Metric>>{

	public Map<String,SortedMap<Timestamp,Metric>> totals = new HashMap<String,SortedMap<Timestamp,Metric>>();
	
	public void add(Timestamp time, String projectName, Metric total) {
		SortedMap<Timestamp, Metric> map = totals.get(projectName);
		if(map==null)
			map = new TreeMap<Timestamp, Metric>();
		map.put(time, total);
		totals.put(projectName,map);
	}

	public void sample(int n, String projectName,String type, Predicate<Metric> castIncreasePredicate) {
		
		ArrayList<Timestamp> castIncrease = subset(projectName,castIncreasePredicate);
		
		Random r = new Random();
		Set<Timestamp> stamps = new HashSet<Timestamp>();
		if(castIncrease.size()<n){
			stamps.addAll(castIncrease);
		}else{
			for(int i = 0; stamps.size() < n ; i++){
				int rand = r.nextInt(castIncrease.size());
				stamps.add(castIncrease.get(rand));					
			}	
		}
		
		
		try {
			Connection conn = Analysis.getDatabaseConnection();
			//much faster when there's an index: CREATE INDEX time_proj_index ON revisions (DateTime,project);
			PreparedStatement statement = conn.prepareStatement("SELECT DISTINCT module from revisions where DateTime = ? AND project = ?");
			for(Timestamp s : stamps){
				statement.setTimestamp(1, s);
				statement.setString(2, projectName);
				ResultSet rs = statement.executeQuery();
				while(rs.next()){
					String module = rs.getString(1);
					System.out.println(projectName+","+module+","+ type + "," + castIncrease.size() + ","+s);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Iterator<List<Metric>> iterator() {
		
		
		return new Iterator<List<Metric>>() {

			Iterator<SortedMap<Timestamp, Metric>> values = totals.values().iterator();
			
			@Override
			public boolean hasNext() {
				return values.hasNext();
			}

			@Override
			public List<Metric> next() {
				
				SortedMap<Timestamp, Metric> next = values.next();
				
				return new ArrayList<Metric>(next.values());
			}

			@Override
			public void remove() {
				throw new RuntimeException();
			}
		};
	}
	
	public ArrayList<Timestamp> subset(String projectName,
			Predicate<Metric> castIncreasePredicate) {
		SortedMap<Timestamp, Metric> metrics = totals.get(projectName);
		@SuppressWarnings("unchecked")
		SortedMap<Timestamp, Metric> castIncrease = PredicatedSortedMap.decorate(new TreeMap<Timestamp, Metric>(), TruePredicate.INSTANCE, castIncreasePredicate);
		
		for(Map.Entry<Timestamp, Metric> e : metrics.entrySet()){
			try {
				castIncrease.put(e.getKey(), e.getValue());
			} catch (Exception _) {
			}
		}
		return new ArrayList<Timestamp>(castIncrease.keySet());
	}
}

class Pair{
	Timestamp ts;
	String projectName;
	
	public int hashCode(){
		return ts.hashCode()+projectName.hashCode();
	}
}

class CombinedMetric{
	
	public int genericDelta;
	public int castDelta;
	BasicMetric genericMetric;
	BasicMetric castMetric;
	HalsteadMetric hm;
	
	double relativeGenericDelta = 0.0;
	double relativeCastDelta = 0.0;
	
	public double normalizedCasts(){
		
		if(hm.totalOperands==0)
			return 0.0;
		
		return castCount() / (double)(hm.totalOperands+hm.totalOperands);
	}
	
	public int castCount(){
		return castMetric == null ? 0 : castMetric.count;
	}
	
	public int genericCount(){
		return genericMetric == null ? 0 : genericMetric.count;
	}
	
	public double normalizedGenerics(){
		
		if(hm.totalOperands==0)
			return 0.0;
		
		return genericCount() / (double)(hm.totalOperands+hm.totalOperands);
	}
}