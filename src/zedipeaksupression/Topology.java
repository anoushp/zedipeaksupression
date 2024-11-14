package zedipeaksupression;
import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.GmlImporter;
import org.jgrapht.io.ImportException;
import org.jgrapht.io.VertexProvider;
import org.jgrapht.util.SupplierUtil;
import zedipeaksupression.common.Constants;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.*;
//this class is responsible for controlling network topology. At the mopment creating random Erdos Reyni graph with number of vertices and edges.
public class Topology {
	Graph<String, DefaultEdge> completeGraph;
	GraphGenerator graphGenerator;
	//Map<DefaultEdge, Double> weightMap1;
	public Topology(String netName, int vertexNum, int edgesNum, int degree) {
		if (netName.equals("Random")){
			GnmRandomGraphGenerator <String, DefaultEdge> randomGraphGenerator =
					new GnmRandomGraphGenerator<>(vertexNum,edgesNum);
			generateGraph((GraphGenerator)randomGraphGenerator);
		}
		if (netName.equals("RandomSparse")){
			GnpRandomGraphGenerator <String, DefaultEdge> randomGraphGenerator =
					new GnpRandomGraphGenerator<>(100,0.02);
			//0.04
			generateGraph((GraphGenerator)randomGraphGenerator);
			
		}
		if (netName.equals("RandomDense")){
			GnpRandomGraphGenerator <String, DefaultEdge> randomGraphGenerator =
					new GnpRandomGraphGenerator<>(100,0.1);
			generateGraph((GraphGenerator)randomGraphGenerator);
		}
		if (netName.equals("Random")){
			double p=0;
			if (degree ==2)
				p=0.02;
			if (degree ==4)
				p=0.04;
			else if (degree ==8)
				p=0.09;
			
			GnpRandomGraphGenerator <String, DefaultEdge> randomGraphGenerator =
					new GnpRandomGraphGenerator<>(100,p);
			//0.04
			generateGraph((GraphGenerator)randomGraphGenerator);
			
		}
		else if (netName.equals("Partition"))
		{
			System.out.println("AAAAAAAAAAA");
			//PlantedPartitionGraphGenerator <String, DefaultEdge> partGraphGenerator =
					//new PlantedPartitionGraphGenerator<>(4, 25, 0.1,0.09);
			//generateGraph((GraphGenerator)partGraphGenerator);
			PlantedPartitionGraphGenerator <String, DefaultEdge> partGraphGenerator =
					new PlantedPartitionGraphGenerator<>(99/degree, degree, 1,0);
			generateGraph((GraphGenerator)partGraphGenerator);
			
		}
		//aaaaaa
		else if (netName.equals("WS"))
		{
			System.out.println("WS");
			WattsStrogatzGraphGenerator <String, DefaultEdge> wsGraphGenerator =
					new WattsStrogatzGraphGenerator<>(100, degree , 0);
			generateGraph((GraphGenerator)wsGraphGenerator);
			
		}
		else if (netName.equals("WS1"))
		{
			System.out.println("WS1");
			WattsStrogatzGraphGenerator <String, DefaultEdge> wsGraphGenerator =
					new WattsStrogatzGraphGenerator<>(100, degree , 1);
			generateGraph((GraphGenerator)wsGraphGenerator);
			
		}
		else if (netName.equals("CM"))
		{
			System.out.println("Confnet");
			String template = ".\\data\\ConfigNet_%s.gml";
			String filename = String.format(template, String.valueOf(degree));
			Supplier<String> vSupplier = new Supplier<String>()
			{
				private int id = 1;

				@Override
				public String get()
				{
					return "F" + id++;
				}
			};
			
			this.completeGraph =
					new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);
			  VertexProvider<String> vp = (label, attributes) -> "F" + String.valueOf((Integer.valueOf(label).intValue())+1);
		        EdgeProvider<String, DefaultEdge> ep =
		            (from, to, label, attributes) -> this.completeGraph.getEdgeSupplier().get();


	        GmlImporter<String, DefaultEdge> importer = new GmlImporter<String, DefaultEdge>(vp, ep);
	        try {
	        importer.importGraph(this.completeGraph, new File(filename));}
	        catch (ImportException e) {
	        	System.out.println("Exception in read gml");
	        }
			
		}
			//''new WattsStrogatzGraphGenerator<>(100, 4, 0.3);
	//		Set<DefaultEdge> edges=completeGraph.edgesOf("F1");
		//	System.out.println(completeGraph.edgesOf("F1"));
		//	for (DefaultEdge e : edges) {
		//		System.out.println(completeGraph.getEdgeSource(e));
		//	};
		
			
			//completeGraph.vertexSet();
			/*Set<DefaultEdge> edges = g.edgeSet();
         completeGraph.
	    for (DefaultEdge e : edges) {
	        gv.addln(String.format("\"%s\" -> \"%s\"", g.getEdgeSource(e), g.getEdgeTarget(e)));            
	    }*/
		

	}
	public void changeTopology(String netName) {
		if (netName.equals("Random")){
			GnmRandomGraphGenerator <String, DefaultEdge> randomGraphGenerator =
					new GnmRandomGraphGenerator<>(12,24);
			generateGraph((GraphGenerator)randomGraphGenerator);
		}
		else if (netName.equals("Partition"))
		{
						PlantedPartitionGraphGenerator <String, DefaultEdge> partGraphGenerator =
					new PlantedPartitionGraphGenerator<>(3, 4, 0.1,0.2);
			generateGraph((GraphGenerator)partGraphGenerator);
			
		}
	}
	private void generateGraph(GraphGenerator<String, DefaultEdge, Object> graphGenerator){
		System.out.println("GENERATE");
		Supplier<String> vSupplier = new Supplier<String>()
		{
			private int id = 1;

			@Override
			public String get()
			{
				return "F" + id++;
			}
		};
		
		this.completeGraph =
				new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);
		graphGenerator.generateGraph(completeGraph);
		
	}
	public Set<String> getInfluenceNodes(String id){
		Set<String> set_ids = new HashSet<String>();
		Set<DefaultEdge> edges=completeGraph.edgesOf(id);
		for (DefaultEdge e : edges) {
			set_ids.add(completeGraph.getEdgeSource(e));
		}
		Set<String> neighb = new HashSet<String>(Graphs.neighborListOf(completeGraph, id));
		
	    //Graphs.neighborListOf(completeGraph, id)
		return neighb;
	}
	
}
