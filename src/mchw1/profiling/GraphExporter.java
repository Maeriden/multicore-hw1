package mchw1.profiling;

import mchw1.profiling.graph.Edge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import mchw1.profiling.graph.Graph;
import mchw1.profiling.graph.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.*;

public class GraphExporter
{
	static public
	void
	export_gexf(Graph<NodeData, EdgeData> graph, File file)
	{
		Comparator<Node<NodeData, EdgeData>> order_by_time = Comparator.comparingLong(node -> node.data.time_begin_computation);
		List<Node<NodeData, EdgeData>> nodes = new ArrayList<>(graph.get_nodes());
		nodes.sort(order_by_time);
		
		
		try
		{
			Map<NodeData.Type, String[]> node_colors = new HashMap<>();
			node_colors.put(NodeData.Type.SPLIT,          new String[] {"255",   "0",   "0", "1.0"});
			node_colors.put(NodeData.Type.SORT,           new String[] {  "0", "255",   "0", "1.0"});
			node_colors.put(NodeData.Type.MERGE,          new String[] {  "0",   "0", "255", "1.0"});
			node_colors.put(NodeData.Type.MERGE_PARALLEL, new String[] {"100", "100", "255", "1.0"});
			
			Map<EdgeData.Type, String> edge_shapes = new HashMap<>();
			edge_shapes.put(EdgeData.Type.CALL, "solid");
			edge_shapes.put(EdgeData.Type.DATA, "dotted");
			
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			doc.setXmlStandalone(false);
			
			
			Element elem_root = doc.createElementNS("http://www.gexf.net/1.3draft", "gexf");
			elem_root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			elem_root.setAttribute("xsi:schemaLocation", "http://www.gexf.net/1.3draft http://www.gexf.net/1.3draft/gexf.xsd");
			elem_root.setAttribute("xmlns:viz", "http://www.gexf.net/1.2draft/viz");
			elem_root.setAttribute("version", "1.3");
			doc.appendChild(elem_root);
			
			
			Element elem_graph = doc.createElement("graph");
			elem_graph.setAttribute("defaultedgetype", "directed");
			elem_graph.setAttribute("idtype", "long");
			elem_graph.setAttribute("mode", "dynamic");
			elem_graph.setAttribute("timeformat", "integer");
			elem_root.appendChild(elem_graph);
			
			
			Element elem_attributes = doc.createElement("attributes");
			{
				elem_attributes.setAttribute("class", "node");
				elem_attributes.setAttribute("mode", "static");
				
				Element elem_attribute = doc.createElement("attribute");
				elem_attribute.setAttribute("id", "0");
				elem_attribute.setAttribute("type", "integer");
				elem_attribute.setAttribute("title", "time_execution");
				elem_attributes.appendChild(elem_attribute);
				
				elem_attribute = doc.createElement("attribute");
				elem_attribute.setAttribute("id", "1");
				elem_attribute.setAttribute("type", "integer");
				elem_attribute.setAttribute("title", "thread_id");
				
				elem_attribute = doc.createElement("attribute");
				elem_attribute.setAttribute("id", "2");
				elem_attribute.setAttribute("type", "string");
				elem_attribute.setAttribute("title", "slice");
				
				elem_attribute = doc.createElement("attribute");
				elem_attribute.setAttribute("id", "3");
				elem_attribute.setAttribute("type", "integer");
				elem_attribute.setAttribute("title", "fork_count");
				elem_attributes.appendChild(elem_attribute);
			}
			elem_graph.appendChild(elem_attributes);
			
			
			int nodes_count = nodes.size();
			Element elem_nodes = doc.createElement("nodes");
			elem_nodes.setAttribute("count", Integer.toString(nodes_count));
			elem_graph.appendChild(elem_nodes);
			
			Map<Node<NodeData, EdgeData>, Long> nodes_ids = new HashMap<>();
			for(int i = 0; i < nodes_count; ++i)
			{
				Node<NodeData, EdgeData> node = nodes.get(i);
				nodes_ids.put(node, (long)i);
				
				Element elem_node = doc.createElement("node");
				elem_node.setAttribute("id",    get_node_id(node, i));
				elem_node.setAttribute("label", get_node_label(node, i));
				elem_node.setAttribute("start", get_node_start(node, i));
				elem_nodes.appendChild(elem_node);
				
				Element elem_attvalues = doc.createElement("attvalues");
				{
					Element elem_attvalue = doc.createElement("attvalue");
					elem_attvalue.setAttribute("for", "0"); // time_execution
					elem_attvalue.setAttribute("value", get_node_time_execution(node, i));
					elem_attvalues.appendChild(elem_attvalue);
					
					elem_attvalue = doc.createElement("attvalue");
					elem_attvalue.setAttribute("for", "1"); // thread_id
					elem_attvalue.setAttribute("value", get_node_thread_id(node, i));
					elem_attvalues.appendChild(elem_attvalue);
					
					elem_attvalue = doc.createElement("attvalue");
					elem_attvalue.setAttribute("for", "2"); // slice
					elem_attvalue.setAttribute("value", get_node_slice(node, i));
					elem_attvalues.appendChild(elem_attvalue);
					
					elem_attvalue = doc.createElement("attvalue");
					elem_attvalue.setAttribute("for", "3"); // fork_count
					elem_attvalue.setAttribute("value", get_node_fork_count(node, i));
					elem_attvalues.appendChild(elem_attvalue);
				}
				elem_node.appendChild(elem_attvalues);
				
				Element elem_node_color = doc.createElement("viz:color");
				String[] color = node_colors.get(node.data.type);
				elem_node_color.setAttribute("r", color[0]);
				elem_node_color.setAttribute("g", color[1]);
				elem_node_color.setAttribute("b", color[2]);
				elem_node_color.setAttribute("a", color[3]);
				elem_node.appendChild(elem_node_color);
			}
			
			
			int edges_count = nodes.stream().mapToInt(n -> n.edges.size()).sum();
			Element elem_edges = doc.createElement("edges");
			elem_edges.setAttribute("count", Integer.toString(edges_count));
			elem_graph.appendChild(elem_edges);
			
			for(Node<NodeData, EdgeData> tail : nodes)
			{
				long tail_id = nodes_ids.get(tail);
				for(Edge<EdgeData, NodeData> edge : tail.edges)
				{
					long head_id = nodes_ids.get(edge.head);
					long edge_id = (tail_id << 32) | (head_id & 0xFFFFFFFFL);
					
					Element elem_edge = doc.createElement("edge");
					elem_edge.setAttribute("id", Long.toString(edge_id));
					elem_edge.setAttribute("source", Long.toString(tail_id));
					elem_edge.setAttribute("target", Long.toString(head_id));
					elem_edges.appendChild(elem_edge);
					
					// TODO: Gephi does not yet implement edge shape visualization
					Element elem_edge_shape = doc.createElement("viz:shape");
					elem_edge_shape.setAttribute("value", edge_shapes.get(edge.data.type));
					elem_edge.appendChild(elem_edge_shape);
					
					// TODO: For now use a different color for "dotted" edges
					if(edge.data.type == EdgeData.Type.DATA)
					{
						Element elem_edge_color = doc.createElement("viz:color");
						elem_edge_color.setAttribute("r", "255");
						elem_edge_color.setAttribute("g",   "0");
						elem_edge_color.setAttribute("b", "255");
						elem_edge_color.setAttribute("a", "0.5");
						elem_edge.appendChild(elem_edge_color);
					}
				}
			}
			
			
			DOMSource dom = new DOMSource(doc);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(dom, new StreamResult(file));
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
	
	static private
	String
	get_node_id(Node<NodeData, EdgeData> node, int i)
	{
		return Long.toString(i);
	}
	
	
	static private
	String
	get_node_label(Node<NodeData, EdgeData> node, int i)
	{
		return Long.toString(node.data.thread_id);
	}
	
	
	static private
	String
	get_node_start(Node<NodeData, EdgeData> node, int i)
	{
		return Long.toString(i);
	}
	
	
	static private
	String
	get_node_time_execution(Node<NodeData, EdgeData> node, int i)
	{
		return Long.toString(node.data.time_end_computation - node.data.time_begin_computation);
	}
	
	
	static private
	String
	get_node_thread_id(Node<NodeData, EdgeData> node, int i)
	{
		return Long.toString(node.data.thread_id);
	}
	
	
	static private
	String
	get_node_slice(Node<NodeData, EdgeData> node, int i)
	{
		return String.format("[%d, %d)", node.data.slice_1_begin, node.data.slice_1_end);
	}
	
	
	static private
	String
	get_node_fork_count(Node<NodeData, EdgeData> node, int i)
	{
		return node.data.fork_count > 0 ? Long.toString(node.data.fork_count) : "";
	}
}
