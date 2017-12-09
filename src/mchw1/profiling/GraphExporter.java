package mchw1.profiling;

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
	export_gexf(Graph<NodeData> graph, File file)
	{
		Comparator<Node<NodeData>> compare_by_id = Comparator.comparingLong(node -> node.data.time_begin_computation);
		List<Node<NodeData>> nodes = new ArrayList<>(graph.get_nodes());
		nodes.sort(compare_by_id);
		
		
		try
		{
			Map<NodeData.Type, String[]> colors = new HashMap<>();
			colors.put(NodeData.Type.SPLIT, new String[] {"255", "0", "0", "1.0"});
			colors.put(NodeData.Type.SORT, new String[] {"0", "255", "0", "1.0"});
			colors.put(NodeData.Type.MERGE, new String[] {"0", "0", "255", "1.0"});
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			doc.setXmlStandalone(false);
			
			
			Element elem_root = doc.createElementNS("http://www.gexf.net/1.3draft", "gexf");
			elem_root.setAttribute("xmlns:xsi", "”http://www.w3.org/2001/XMLSchema-instance");
			elem_root.setAttribute("xsi:schemaLocation", "”http://www.gexf.net/1.3draft http://www.gexf.net/1.3draft/gexf.xsd");
			elem_root.setAttribute("xmlns:viz", "”http://www.gexf.net/1.2draft/viz");
			elem_root.setAttribute("version", "1.3");
			doc.appendChild(elem_root);
			
			
			Element elem_graph = doc.createElement("graph");
			elem_graph.setAttribute("defaultedgetype", "directed");
			elem_graph.setAttribute("idtype", "long");
			elem_graph.setAttribute("mode", "dynamic" /*slice*/);
			elem_graph.setAttribute("timeformat", "integer");
			elem_root.appendChild(elem_graph);
			
			
			Element elem_attributes = doc.createElement("attributes");
			{
				elem_attributes.setAttribute("class", "node");
				elem_attributes.setAttribute("mode", "static");
				
				Element elem_attribute = doc.createElement("attribute");
				elem_attribute.setAttribute("id", "0");
				elem_attribute.setAttribute("title", "time_execution");
				elem_attribute.setAttribute("type", "integer");
				elem_attributes.appendChild(elem_attribute);
			}
			elem_graph.appendChild(elem_attributes);
			
			
			int nodes_count = nodes.size();
			Element elem_nodes = doc.createElement("nodes");
			elem_nodes.setAttribute("count", Integer.toString(nodes_count));
			elem_graph.appendChild(elem_nodes);
			
			for(int i = 0; i < nodes_count; ++i)
			{
				Node<NodeData> node = nodes.get(i);
				
				Element elem_node = doc.createElement("node");
				elem_node.setAttribute("id", Long.toString(node.id));
				elem_node.setAttribute("label", Long.toString(i+1));
				elem_node.setAttribute("start", Long.toString(i));
				elem_nodes.appendChild(elem_node);
				
				Element elem_attvalues = doc.createElement("attvalues");
				{
					long time_execution = node.data.time_end_computation - node.data.time_begin_computation;
					Element elem_attvalue = doc.createElement("attvalue");
					elem_attvalue.setAttribute("for", "0");
					elem_attvalue.setAttribute("value", Long.toString(time_execution));
					elem_attvalues.appendChild(elem_attvalue);
				}
				elem_node.appendChild(elem_attvalues);
				
				Element elem_node_color = doc.createElement("viz:color");
				String[] color = colors.get(node.data.type);
				elem_node_color.setAttribute("r", color[0]);
				elem_node_color.setAttribute("g", color[1]);
				elem_node_color.setAttribute("b", color[2]);
				elem_node_color.setAttribute("a", color[3]);
				elem_node.appendChild(elem_node_color);
			}
			
			
			int edges_count = nodes.stream().mapToInt(n -> n.adjacents.size()).sum();
			Element elem_edges = doc.createElement("edges");
			elem_edges.setAttribute("count", Integer.toString(edges_count));
			elem_graph.appendChild(elem_edges);
			
			for(Node<NodeData> tail : nodes)
			{
				for(Node<NodeData> head : tail.adjacents)
				{
					Element elem_edge = doc.createElement("edge");
					long edge_id = (tail.id << 32) | (head.id & 0xFFFFFFFFL);
					elem_edge.setAttribute("id", Long.toString(edge_id));
					elem_edge.setAttribute("source", Long.toString(tail.id));
					elem_edge.setAttribute("target", Long.toString(head.id));
					elem_edges.appendChild(elem_edge);
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
}
