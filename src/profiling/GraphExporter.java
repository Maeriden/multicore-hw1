package profiling;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import profiling.graph.Graph;
import profiling.graph.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
		Comparator<Node<NodeData>> compare_by_id = Comparator.comparingInt(node -> node.data.id);
		List<Node<NodeData>> nodes = new ArrayList<>(graph.get_nodes());
		nodes.sort(compare_by_id);
		
		
		try
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			
			
			Element elem_root = doc.createElement("gexf");
			elem_root.setAttribute("xmlns", "http://www.gexf.net/1.2draft");
			elem_root.setAttribute("version", "1.2");
			doc.appendChild(elem_root);
			
			
			Element elem_nodes = doc.createElement("nodes");
			elem_root.appendChild(elem_nodes);
			
			for(Node<NodeData> node : nodes)
			{
				Element elem_node = doc.createElement("node");
				elem_node.setAttribute("id", Integer.toString(node.data.id));
				elem_node.setAttribute("label", "");
				elem_nodes.appendChild(elem_node);
			}
			
			
			Element elem_edges = doc.createElement("edges");
			elem_root.appendChild(elem_edges);
			
			for(Node<NodeData> tail : nodes)
			{
				for(Node<NodeData> head : tail.adjacents)
				{
					Element elem_edge = doc.createElement("edge");
					elem_edge.setAttribute("id", String.format("%d,%d", tail.data.id, head.data.id));
					elem_edge.setAttribute("source", Integer.toString(tail.data.id));
					elem_edge.setAttribute("source", Integer.toString(head.data.id));
					elem_edges.appendChild(elem_edge);
				}
			}
			
			
			DOMSource dom = new DOMSource(doc);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(dom, new StreamResult(file));
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}
}
