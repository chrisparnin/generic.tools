/*
    This is an example of using the Ohloh API from Java.
    Detailed information can be found at the Ohloh website:

     http://www.ohloh.net/api

    This examples retrieves a account and simply shows the name associated.

    Pass your Ohloh API key as the first parameter to this example.
    Ohloh API keys are free. If you do not have one, you can obtain one
    at the Ohloh website:

     http://www.ohloh.net/api_keys/new

    Pass the email address of the account as the second parameter to this script.
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class GetTop100Projects {

	public GetTop100Projects() {
		initiate();
	}

	public void initiate() {

		try {
			System.out.println("Initialising request.");

	        try
	        {
	            // Request XML file.
	        	
	        	List<ProjectEntry> projects = new ArrayList<ProjectEntry>(100);
	        	while(projects.size()<101){

	        		int pageNum = projects.size()/10+1;	
		            Document doc =getDoc("p.xml?page="+pageNum+"&q=language%3Ajava&sort=users&");
	
	                for(Element element : new NodeIterator(doc,"response")){
	
		                Element resultElement = (Element)element.getElementsByTagName("result").item(0);
		                
		                for(Element projectNode : new NodeIterator(resultElement)){
		                	String projectId = elementIn(projectNode, "id");
		                	String projectName = elementIn(projectNode, "name");
		                	projects.add(getProjectInfoFor(projectId,projectName));	
		                	if(projects.size()>=101){
		                		break;
		                	}
		                }
	                }
		        }
	        	
	        	print(projects);
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void print(List<ProjectEntry> projects) {
		for(ProjectEntry pe : projects){
			System.out.print(pe);
		}
		System.out.println("done");
	}

	private ProjectEntry getProjectInfoFor(String projectId, String projectName) throws Exception {
		 
		ProjectEntry p = new ProjectEntry();
		p.projectId = Integer.parseInt(projectId);
		p.projectName = projectName;
		
		boolean complete = false;
		int page = 0;
		
		while(!complete){
		
			String pageString;
			if(page>0){
				pageString = "?page=" + page + "&";
			}else{
				pageString = "?";
			}
			Document doc = getDoc("p/" + projectId+ "/enlistments.xml"+pageString);
	
	        for(Element response : new NodeIterator(doc, "response")){	        	
	        	
	        	int itemsReturned = Integer.parseInt(elementIn(response, "items_returned"));
	        	int itemsAvailable = Integer.parseInt(elementIn(response, "items_available"));
	        	int firstItemPosition = Integer.parseInt(elementIn(response, "first_item_position"));
	
	            Element resultElement = (Element)response.getElementsByTagName("result").item(0);
	            
				for(Element enlistmentNode : new NodeIterator(resultElement)){
		
	            	for(Element repo : new NodeIterator(enlistmentNode,"repository")){
	            		
	            		String type = elementIn(repo, "type");
	            		String repoURL = elementIn(repo, "url");
	            		String module = elementIn(repo, "module_name");
	            		String username = elementIn(repo, "username");
	            		String password = elementIn(repo, "password");
	            		String status = elementIn(repo, "ohloh_job_status");
	            		
	            		Repo r = new Repo(type,repoURL,module,username,password,status);
	            		p.repos.add(r);
	            	}
				}
				
				page++;
				complete = itemsReturned+firstItemPosition >= itemsAvailable;
	        }
	        
	        
		}
		
		return p;
	}	

	private Document getDoc(String string) throws MalformedURLException,
			IOException, ParserConfigurationException, SAXException {
		
		String pre = "https://www.ohloh.net/";
		String post = "api_key=waDryU9cuiDEO64aJLYMA";
		
		URL url = new URL(pre+string+post);
		URLConnection con = url.openConnection();		

		// Check for status OK.
		if (!con.getHeaderField("Status").startsWith("200"))
		{
		    System.out.println("Request failed. Possibly wrong API key?");
		}
         
		 // Create a document from the URL's input stream, and parse.
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(con.getInputStream());
		return doc;
	}	

	private String elementIn(Element parent, String s) {
		NodeList elem = parent.getElementsByTagName(s);
		if(elem==null){
			System.err.println("No element named " + s);
		}else if(elem.getLength()<1){
			System.err.println("No children named " + s);
		}else if(elem.getLength()>=1){
			if(elem.getLength()>1)
				System.err.println("More than 1 element named " + s + ". Picking the first.");		
			return elem.item(0).getTextContent();
		}
		return null;
	}

	public static void main(String[] args) {
		
		new GetTop100Projects();
	}
}

class ProjectEntry{
	
	String projectName;
	int projectId;
	String name;
	
	List<Repo> repos = new ArrayList<Repo>();
	
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		
		for(Repo r : repos){
			sb.append(projectName);
			sb.append('\t');
			sb.append(r.type);
			sb.append('\t');
			sb.append(r.status);
			sb.append('\t');
			sb.append(r.url);
			sb.append('\t');
			sb.append(r.module);
			sb.append('\t');
			sb.append(r.username);
			sb.append('\t');
			sb.append(r.password);
			sb.append('\n');
		}
			
		return sb.toString();
	}
}

class NodeIterator implements Iterable<Element>,Iterator<Element>{
	
	private NodeList coll;
	private int pointer = 0;
	private Element next;
	
	public NodeIterator(NodeList coll){
		this.coll = coll; 
		setNext();
	}

	private void setNext() {
		while(next==null && coll.getLength()>pointer)
			next = internalNext();
	}
	
	public NodeIterator(Element parent){
		this(parent.getChildNodes());
	}
	
	public NodeIterator(Element parent, String name){
		this(parent.getElementsByTagName(name));
	}
	
	public NodeIterator(Document parent, String name){
		this(parent.getElementsByTagName(name));
	}
	
	@Override
	public boolean hasNext() {
		return next!=null;
	}	
	
	@Override
	public Element next() {
		Element temp = next;
		next = null;
		setNext();
		return temp;
	}

	private Element internalNext() {
		Node item = coll.item(pointer++);
		if(item==null){
			return null;
			
		}else if(item instanceof Text){
			String wholeText = ((Text)item).getWholeText();
			for(char c : wholeText.toCharArray()){
				if(!Character.isWhitespace(c)){
					System.out.println("Found unexpected nonwhitespace: "+wholeText);
					break;
				}
			}
			return null;
		}else if(!(item instanceof Element)){
			System.err.println("Expected Element but got something else: " + item);
			return null;
		}
		Element elem = (Element) item;
        checkStatus(elem);
		return elem;
	}
	
	
	@Override
	public void remove() {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public Iterator<Element> iterator() {
		return this;
	}
	
	private void checkStatus(Element element) {
		NodeList statusList = element.getElementsByTagName("status");
		if (statusList.getLength() == 1)
		{
		    Node statusNode = statusList.item(0);

		    // Check if the text inidicates that the request was
		    //  successful.
		    if (!statusNode.getTextContent().equals("success"))
		    {
		        System.out.println("Failed. " + statusNode.getTextContent());
		    }
		}
	}
}
